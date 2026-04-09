package dev.nettools.android.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.nettools.android.data.security.KnownHostsManager
import dev.nettools.android.data.ssh.ErrorMapper
import dev.nettools.android.data.ssh.ScpClient
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
import kotlinx.coroutines.withContext
import java.io.File
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
    }

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var progressHolder: TransferProgressHolder
    @Inject lateinit var sshConnectionManager: SshConnectionManager
    @Inject lateinit var scpClient: ScpClient
    @Inject lateinit var knownHostsManager: KnownHostsManager
    @Inject lateinit var historyRepository: TransferHistoryRepository

    /** Tracks coroutine Jobs by jobId for cancellation support. */
    private val jobCoroutines = ConcurrentHashMap<String, Job>()

    /** Guards against double-processing the same job ID. */
    private val runningJobIds: MutableSet<String> = ConcurrentHashMap.newKeySet()

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
            else -> lifecycleScope.launch {
                progressHolder.restorePersistedJobs()
                processAllQueued()
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

    private fun processAllQueued() {
        while (true) {
            val params = progressHolder.dequeue() ?: break
            if (runningJobIds.add(params.job.id)) {
                processJob(params)
            }
        }
    }

    private fun processJob(params: PendingTransferParams) {
        val jobId = params.job.id

        // Start foreground immediately to satisfy the 5-second ANR window.
        val initialNotification = notificationHelper.createProgressNotification(
            jobId = jobId,
            fileName = params.job.remotePath.substringAfterLast('/'),
            progress = TransferProgress("", 0L, -1L, 0.0),
            channelId = TRANSFER_CHANNEL_ID,
        )
        startForeground(FOREGROUND_NOTIFICATION_ID, initialNotification)
        progressHolder.updateJobStatus(jobId, TransferStatus.IN_PROGRESS)

        val coroutineJob = lifecycleScope.launch {
            var bytesTransferred = 0L
            var finalStatus = TransferStatus.COMPLETED
            var errorMsg: String? = null
            var uploadTempFile: File? = null
            var downloadTempFile: File? = null

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
                                val (file, temp) = resolveUploadFile(params.job.localPath, jobId)
                                uploadTempFile = temp
                                scpClient.upload(sshClient, file, params.job.remotePath)
                                    .collect { progress ->
                                        bytesTransferred = progress.bytesTransferred
                                        progressHolder.updateProgress(jobId, progress)
                                        updateNotification(jobId, progress)
                                    }
                            }
                            TransferDirection.DOWNLOAD -> {
                                val remoteFileName = params.job.remotePath.substringAfterLast('/')
                                val (file, temp) = resolveDownloadFile(
                                    params.job.localPath, remoteFileName, jobId
                                )
                                downloadTempFile = temp

                                // Check for existing partial file for resume support.
                                val resumeOffset = if (file.exists()) file.length() else 0L
                                val flow = if (resumeOffset > 0L) {
                                    scpClient.downloadResumable(
                                        sshClient, params.job.remotePath, file, resumeOffset
                                    )
                                } else {
                                    scpClient.download(sshClient, params.job.remotePath, file)
                                }

                                flow.collect { progress ->
                                    bytesTransferred = progress.bytesTransferred
                                    progressHolder.updateProgress(jobId, progress)
                                    updateNotification(jobId, progress)
                                }

                                // If download target was a temp file, copy to SAF destination.
                                if (temp != null) {
                                    copySafDownload(params.job.localPath, remoteFileName, temp)
                                }
                            }
                        }
                    } finally {
                        runCatching { sshClient.close() }
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
                    uploadTempFile?.delete()
                    downloadTempFile?.delete()

                    if (finalStatus == TransferStatus.COMPLETED) {
                        progressHolder.updateJobStatus(jobId, TransferStatus.COMPLETED)
                    }

                    recordHistory(params, finalStatus, bytesTransferred, errorMsg)
                    progressHolder.clearPersistedJob(jobId)

                    val remoteFileName = params.job.remotePath.substringAfterLast('/')
                    val nm = getSystemService(NotificationManager::class.java)
                    when (finalStatus) {
                        TransferStatus.COMPLETED ->
                            nm.notify(
                                jobId.hashCode(),
                                notificationHelper.createSuccessNotification(remoteFileName, bytesTransferred, TRANSFER_CHANNEL_ID)
                            )
                        TransferStatus.FAILED ->
                            nm.notify(
                                jobId.hashCode(),
                                notificationHelper.createFailureNotification(remoteFileName, errorMsg ?: "Unknown error", TRANSFER_CHANNEL_ID)
                            )
                        else -> {}
                    }

                    runningJobIds.remove(jobId)
                    jobCoroutines.remove(jobId)
                    checkAndStopIfEmpty()
                }
            }
        }

        jobCoroutines[jobId] = coroutineJob
    }

    /**
     * Resolves an upload source path to a [File], copying from a SAF [Uri] to a
     * temporary cache file when necessary.
     *
     * @param localPath Either a SAF `content://` URI string or a file-system path.
     * @param jobId Used to name the temp file uniquely.
     * @return Pair of (file to upload, temp file to delete after upload or null).
     */
    private fun resolveUploadFile(localPath: String, jobId: String): Pair<File, File?> {
        return if (localPath.startsWith("content://")) {
            val uri = Uri.parse(localPath)
            val fileName = getFileNameFromUri(uri)
            val temp = File(cacheDir, "upload_${jobId}_$fileName")
            contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output -> input.copyTo(output) }
            }
            Pair(temp, temp)
        } else {
            Pair(File(localPath), null)
        }
    }

    /**
     * Resolves a download destination path.
     * - SAF tree URI → download to a temp file (caller must copy to SAF afterwards).
     * - File-system path → download directly (appends [remoteFileName] if path is a directory).
     *
     * @param localPath Either a SAF `content://` URI string or a directory/file path.
     * @param remoteFileName Name of the remote file being downloaded.
     * @param jobId Used to name the temp file uniquely.
     * @return Pair of (destination file, temp file to delete or null).
     */
    private fun resolveDownloadFile(
        localPath: String,
        remoteFileName: String,
        jobId: String,
    ): Pair<File, File?> {
        return if (localPath.startsWith("content://")) {
            val temp = File(cacheDir, "download_${jobId}_$remoteFileName")
            Pair(temp, temp)
        } else {
            val dest = File(localPath).let {
                if (it.isDirectory) File(it, remoteFileName) else it
            }
            Pair(dest, null)
        }
    }

    /**
     * Copies [tempFile] into the SAF tree at [destinationUri], creating a new document
     * named [remoteFileName] inside the tree.
     */
    private fun copySafDownload(destinationUri: String, remoteFileName: String, tempFile: File) {
        val treeUri = Uri.parse(destinationUri)
        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri),
        )
        val newDocUri = DocumentsContract.createDocument(
            contentResolver,
            docUri,
            "application/octet-stream",
            remoteFileName,
        ) ?: return
        tempFile.inputStream().use { input ->
            contentResolver.openOutputStream(newDocUri)?.use { output ->
                input.copyTo(output)
            }
        }
    }

    /** Returns the display name for a SAF [Uri], or a fallback based on the URI path. */
    private fun getFileNameFromUri(uri: Uri): String =
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && idx >= 0) cursor.getString(idx) else null
        } ?: uri.lastPathSegment ?: "upload"

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
        runCatching {
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
                )
            )
        }
    }

    private fun checkAndStopIfEmpty() {
        if (runningJobIds.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            TRANSFER_CHANNEL_ID,
            getString(dev.nettools.android.R.string.transfer_channel_name),
            NotificationManager.IMPORTANCE_LOW
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
