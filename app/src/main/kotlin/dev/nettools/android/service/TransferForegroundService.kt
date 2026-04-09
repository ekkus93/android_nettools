package dev.nettools.android.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.nettools.android.data.security.KnownHostsManager
import dev.nettools.android.data.ssh.ErrorMapper
import dev.nettools.android.data.ssh.ScpClient
import dev.nettools.android.data.ssh.SftpClient
import dev.nettools.android.data.ssh.SshConnectionManager
import dev.nettools.android.data.ssh.TransferProgress
import dev.nettools.android.domain.model.HistoryStatus
import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.domain.model.TransferError
import dev.nettools.android.domain.model.TransferHistoryEntry
import dev.nettools.android.domain.model.TransferStatus
import dev.nettools.android.domain.repository.TransferHistoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import net.schmizz.sshj.xfer.FileSystemFile
import net.schmizz.sshj.xfer.LocalSourceFile
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Foreground service that executes SCP file transfers in the background.
 * Transfer parameters are dequeued from [TransferProgressHolder]; progress is
 * streamed back through the same holder so that ViewModels can observe it
 * without binding to the service.
 */
@AndroidEntryPoint
class TransferForegroundService : LifecycleService() {

    companion object {
        const val TRANSFER_CHANNEL_ID = "transfer_channel"
        const val ACTION_CANCEL = "dev.nettools.android.CANCEL_TRANSFER"
        const val EXTRA_JOB_ID = "job_id"
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val TAG = "TransferFgService"
    }

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var progressHolder: TransferProgressHolder
    @Inject lateinit var sshConnectionManager: SshConnectionManager
    @Inject lateinit var scpClient: ScpClient
    @Inject lateinit var sftpClient: SftpClient
    @Inject lateinit var knownHostsManager: KnownHostsManager
    @Inject lateinit var historyRepository: TransferHistoryRepository

    /** Tracks coroutine Jobs by jobId for cancellation support. */
    private val jobCoroutines = ConcurrentHashMap<String, Job>()

    /** Guards against double-processing the same job ID. */
    private val runningJobIds: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private var queueWorkerJob: Job? = null

    private val binder = TransferServiceBinder()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_CANCEL -> {
                val jobId = intent.getStringExtra(EXTRA_JOB_ID)
                if (jobId != null) cancelTransfer(jobId)
            }

            else -> {
                ensureForegroundStarted()
                ensureQueueWorkerRunning()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    /**
     * Cancels the transfer identified by [jobId] and records a CANCELLED history entry.
     *
     * @param jobId ID of the job to cancel.
     */
    fun cancelTransfer(jobId: String) {
        jobCoroutines[jobId]?.cancel()
        jobCoroutines.remove(jobId)
        progressHolder.updateJobStatus(jobId, TransferStatus.CANCELLED)
        checkAndStopIfEmpty()
    }

    // ── Private implementation ────────────────────────────────────────────────

    private fun ensureQueueWorkerRunning() {
        if (queueWorkerJob?.isActive == true) return

        queueWorkerJob = lifecycleScope.launch {
            try {
                progressHolder.restorePersistedJobs()
                drainQueueSequentially(
                    dequeue = { progressHolder.dequeue() },
                    shouldProcess = { params -> runningJobIds.add(params.job.id) },
                ) { params ->
                    supervisorScope {
                        val transferJob = launch { executeJob(params) }
                        jobCoroutines[params.job.id] = transferJob
                        transferJob.join()
                    }
                }
            } finally {
                queueWorkerJob = null
                checkAndStopIfEmpty()
            }
        }
    }

    private suspend fun executeJob(params: PendingTransferParams) {
        val jobId = params.job.id
        val remoteFileName = params.job.remotePath.substringAfterLast('/')

        startForeground(
            FOREGROUND_NOTIFICATION_ID,
            notificationHelper.createProgressNotification(
                jobId = jobId,
                fileName = remoteFileName,
                progress = TransferProgress(fileName = remoteFileName, bytesTransferred = 0L, totalBytes = -1L, speedBytesPerSec = 0.0),
                channelId = TRANSFER_CHANNEL_ID,
            ),
        )
        progressHolder.updateJobStatus(jobId, TransferStatus.IN_PROGRESS)

        var bytesTransferred = 0L
        var finalStatus = TransferStatus.COMPLETED
        var errorMsg: String? = null
        var downloadTarget: DownloadTarget? = null
        var safFinalizationAttempted = false
        var safFinalizationSucceeded = false

        try {
            withContext(Dispatchers.IO) {
                val sshClient = sshConnectionManager.connect(
                    host = params.host,
                    port = params.port,
                    username = params.username,
                    authType = params.authType,
                    password = params.password,
                    keyPath = params.keyPath,
                    knownHostsManager = knownHostsManager,
                )

                try {
                    when (params.job.direction) {
                        TransferDirection.UPLOAD -> {
                            val uploadSource = resolveUploadSource(params.job.localPath)
                            scpClient.upload(sshClient, uploadSource, params.job.remotePath)
                                .collect { progress ->
                                    bytesTransferred = progress.bytesTransferred
                                    progressHolder.updateProgress(jobId, progress)
                                    updateNotification(jobId, progress)
                                }
                        }

                        TransferDirection.DOWNLOAD -> {
                            val remoteSize = sftpClient.getFileSize(sshClient, params.job.remotePath)
                            downloadTarget = resolveDownloadTarget(
                                localPath = params.job.localPath,
                                remotePath = params.job.remotePath,
                                remoteFileName = remoteFileName,
                            )

                            val resumeOffset = prepareResumeOffset(
                                workingFile = downloadTarget!!.workingFile,
                                remoteSizeBytes = remoteSize,
                            )

                            val flow = if (resumeOffset > 0L) {
                                scpClient.downloadResumable(
                                    sshClient = sshClient,
                                    remotePath = params.job.remotePath,
                                    localFile = downloadTarget!!.workingFile,
                                    resumeOffset = resumeOffset,
                                )
                            } else {
                                scpClient.download(
                                    sshClient = sshClient,
                                    remotePath = params.job.remotePath,
                                    localFile = downloadTarget!!.workingFile,
                                )
                            }

                            flow.collect { progress ->
                                bytesTransferred = progress.bytesTransferred
                                progressHolder.updateProgress(jobId, progress)
                                updateNotification(jobId, progress)
                            }

                            if (downloadTarget!!.isSafBacked) {
                                safFinalizationAttempted = true
                                copySafDownload(
                                    destinationUri = downloadTarget!!.safDestinationUri!!,
                                    remoteFileName = remoteFileName,
                                    tempFile = downloadTarget!!.workingFile,
                                )
                                safFinalizationSucceeded = true
                            }

                            if (remoteSize != null) {
                                bytesTransferred = maxOf(bytesTransferred, remoteSize)
                            }
                        }
                    }
                } finally {
                    try {
                        sshClient.close()
                    } catch (e: Exception) {
                        Log.d(TAG, "Failed to close SSH client cleanly", e)
                    }
                }
            }
        } catch (e: CancellationException) {
            finalStatus = TransferStatus.CANCELLED
            progressHolder.updateJobStatus(jobId, TransferStatus.CANCELLED)
            throw e
        } catch (e: TransferError) {
            finalStatus = TransferStatus.FAILED
            errorMsg = e.message ?: "Transfer failed"
            progressHolder.setJobFailed(jobId, errorMsg!!)
        } catch (e: Exception) {
            finalStatus = TransferStatus.FAILED
            errorMsg = ErrorMapper.mapException(e).message ?: "Transfer failed"
            progressHolder.setJobFailed(jobId, errorMsg!!)
        } finally {
            withContext(NonCancellable) {
                if (shouldDeleteWorkingFile(downloadTarget, finalStatus, safFinalizationAttempted, safFinalizationSucceeded)) {
                    downloadTarget?.workingFile?.delete()
                }

                if (finalStatus == TransferStatus.COMPLETED) {
                    progressHolder.updateJobStatus(jobId, TransferStatus.COMPLETED)
                }

                recordHistory(params, finalStatus, bytesTransferred, errorMsg)
                progressHolder.clearPersistedJob(jobId)

                val notificationManager = getSystemService(NotificationManager::class.java)
                when (finalStatus) {
                    TransferStatus.COMPLETED -> notificationManager.notify(
                        jobId.hashCode(),
                        notificationHelper.createSuccessNotification(remoteFileName, bytesTransferred, TRANSFER_CHANNEL_ID),
                    )

                    TransferStatus.FAILED -> notificationManager.notify(
                        jobId.hashCode(),
                        notificationHelper.createFailureNotification(remoteFileName, errorMsg ?: "Unknown error", TRANSFER_CHANNEL_ID),
                    )

                    else -> Unit
                }

                runningJobIds.remove(jobId)
                jobCoroutines.remove(jobId)
                checkAndStopIfEmpty()
            }
        }
    }

    /**
     * Resolves an upload source path to a [LocalSourceFile].
     *
     * For SAF-backed `content://` URIs this returns a streaming source so uploads no
     * longer require a full temporary copy in app cache.
     */
    private fun resolveUploadSource(localPath: String): LocalSourceFile {
        if (!localPath.startsWith("content://")) {
            return FileSystemFile(File(localPath))
        }

        val uri = Uri.parse(localPath)
        val fileName = getFileNameFromUri(uri)
        val fileSize = getFileSizeFromUri(uri)
            ?: throw IOException("Unable to determine the selected file size")

        return StreamSourceFile(
            sourceName = fileName,
            sourceLength = fileSize,
            openStream = {
                contentResolver.openInputStream(uri)
                    ?: throw IOException("Unable to open the selected file")
            },
        )
    }

    /**
     * Resolves a download destination path.
     * - SAF tree URI -> download to a stable temp file so retries can resume.
     * - File-system path -> download directly (appends [remoteFileName] if path is a directory).
     */
    private fun resolveDownloadTarget(
        localPath: String,
        remotePath: String,
        remoteFileName: String,
    ): DownloadTarget {
        return if (localPath.startsWith("content://")) {
            DownloadTarget(
                workingFile = buildStableSafTempFile(cacheDir, localPath, remotePath, remoteFileName),
                safDestinationUri = localPath,
            )
        } else {
            val destination = File(localPath).let {
                if (it.isDirectory) File(it, remoteFileName) else it
            }
            DownloadTarget(workingFile = destination)
        }
    }

    /**
     * Copies [tempFile] into the SAF tree at [destinationUri], creating a new document
     * named [remoteFileName] inside the tree.
     */
    private fun copySafDownload(
        destinationUri: String,
        remoteFileName: String,
        tempFile: File,
    ) {
        val treeUri = Uri.parse(destinationUri)
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri),
        )
        val newDocumentUri = DocumentsContract.createDocument(
            contentResolver,
            documentUri,
            "application/octet-stream",
            remoteFileName,
        ) ?: throw IOException("Unable to create destination document")

        tempFile.inputStream().use { input ->
            val output = contentResolver.openOutputStream(newDocumentUri)
                ?: throw IOException("Unable to open destination document")
            output.use { input.copyTo(it) }
        }
    }

    /** Returns the display name for a SAF [Uri], or a fallback based on the URI path. */
    private fun getFileNameFromUri(uri: Uri): String =
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) cursor.getString(index) else null
        } ?: uri.lastPathSegment ?: "upload"

    /** Returns the best available size for a SAF [Uri], or null if the provider omits it. */
    private fun getFileSizeFromUri(uri: Uri): Long? {
        val queriedSize = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && index >= 0 && !cursor.isNull(index)) cursor.getLong(index) else null
        }
        if (queriedSize != null && queriedSize >= 0L) return queriedSize

        val descriptorLength = contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
        return descriptorLength?.takeIf { it >= 0L }
    }

    private fun ensureForegroundStarted() {
        startForeground(
            FOREGROUND_NOTIFICATION_ID,
            notificationHelper.createQueueNotification(TRANSFER_CHANNEL_ID),
        )
    }

    private fun updateNotification(jobId: String, progress: TransferProgress) {
        val notification = notificationHelper.createProgressNotification(
            jobId = jobId,
            fileName = progress.fileName,
            progress = progress,
            channelId = TRANSFER_CHANNEL_ID,
        )
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private suspend fun recordHistory(
        params: PendingTransferParams,
        status: TransferStatus,
        bytesTransferred: Long,
        errorMsg: String? = null,
    ) {
        val historyStatus = when (status) {
            TransferStatus.COMPLETED -> HistoryStatus.SUCCESS
            TransferStatus.CANCELLED -> HistoryStatus.CANCELLED
            else -> HistoryStatus.FAILED
        }
        val remoteFileName = params.job.remotePath.substringAfterLast('/')
        val remoteDir = params.job.remotePath.substringBeforeLast('/', "")

        try {
            historyRepository.insert(
                TransferHistoryEntry(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    direction = params.job.direction,
                    host = params.host,
                    username = params.username,
                    fileName = remoteFileName,
                    remoteDir = remoteDir,
                    fileSizeBytes = bytesTransferred,
                    status = historyStatus,
                    errorMessage = errorMsg,
                ),
            )
        } catch (e: Exception) {
            Log.d(TAG, "Failed to record transfer history", e)
        }
    }

    private fun checkAndStopIfEmpty() {
        if (runningJobIds.isEmpty() && !progressHolder.hasPendingJobs() && queueWorkerJob?.isActive != true) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            TRANSFER_CHANNEL_ID,
            getString(dev.nettools.android.R.string.transfer_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(dev.nettools.android.R.string.transfer_channel_desc)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Binder returned from [onBind] for optional client access to this service. */
    inner class TransferServiceBinder : Binder() {
        /** Returns the running [TransferForegroundService] instance. */
        fun getService(): TransferForegroundService = this@TransferForegroundService
    }
}
