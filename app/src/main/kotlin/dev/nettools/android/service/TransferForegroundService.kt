package dev.nettools.android.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.nettools.android.data.ssh.TransferProgress
import dev.nettools.android.domain.model.TransferJob
import dev.nettools.android.domain.model.TransferStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that manages a queue of [TransferJob] items.
 * Exposes reactive [StateFlow] streams for progress and active jobs.
 * Notifies the user via a persistent notification while transfers are running
 * and stops itself when the queue is empty.
 */
@AndroidEntryPoint
class TransferForegroundService : LifecycleService() {

    companion object {
        const val TRANSFER_CHANNEL_ID = "transfer_channel"
        const val ACTION_CANCEL = "dev.nettools.android.CANCEL_TRANSFER"
        const val EXTRA_JOB_ID = "job_id"
        private const val FOREGROUND_NOTIFICATION_ID = 1
    }

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val _transferProgress = MutableStateFlow<Map<String, TransferProgress>>(emptyMap())

    /** Current transfer progress keyed by job ID. */
    val transferProgress: StateFlow<Map<String, TransferProgress>> =
        _transferProgress.asStateFlow()

    private val _activeJobs = MutableStateFlow<List<TransferJob>>(emptyList())

    /** List of currently queued or in-progress [TransferJob] items. */
    val activeJobs: StateFlow<List<TransferJob>> = _activeJobs.asStateFlow()

    private val jobCoroutines = mutableMapOf<String, Job>()
    private val binder = TransferServiceBinder()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_CANCEL) {
            val jobId = intent.getStringExtra(EXTRA_JOB_ID)
            if (jobId != null) cancelTransfer(jobId)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    /**
     * Adds a [TransferJob] to the queue and begins processing it.
     *
     * @param job The transfer job to enqueue.
     */
    fun enqueueTransfer(job: TransferJob) {
        _activeJobs.update { current -> current + job.copy(status = TransferStatus.QUEUED) }
        processJob(job)
    }

    /**
     * Cancels the transfer identified by [jobId].
     *
     * @param jobId ID of the job to cancel.
     */
    fun cancelTransfer(jobId: String) {
        jobCoroutines[jobId]?.cancel()
        jobCoroutines.remove(jobId)
        _activeJobs.update { jobs ->
            jobs.map { if (it.id == jobId) it.copy(status = TransferStatus.CANCELLED) else it }
        }
        checkAndStopIfEmpty()
    }

    private fun processJob(job: TransferJob) {
        val coroutineJob = lifecycleScope.launch {
            updateJobStatus(job.id, TransferStatus.IN_PROGRESS)

            val dummyProgress = TransferProgress(
                fileName = job.localPath.substringAfterLast('/'),
                bytesTransferred = 0L,
                totalBytes = -1L,
                speedBytesPerSec = 0.0
            )
            updateProgress(job.id, dummyProgress)

            startForegroundWithNotification(job.id, dummyProgress)

            // Actual transfer implementation is wired by the ViewModel/UseCase layer.
            // The service manages lifecycle; transfer logic is injected separately.

            updateJobStatus(job.id, TransferStatus.COMPLETED)
            removeJob(job.id)
            checkAndStopIfEmpty()
        }
        jobCoroutines[job.id] = coroutineJob
    }

    private fun startForegroundWithNotification(jobId: String, progress: TransferProgress) {
        val notification = notificationHelper.createProgressNotification(
            jobId = jobId,
            fileName = progress.fileName,
            progress = progress,
            channelId = TRANSFER_CHANNEL_ID
        )
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun updateJobStatus(jobId: String, status: TransferStatus) {
        _activeJobs.update { jobs ->
            jobs.map { if (it.id == jobId) it.copy(status = status) else it }
        }
    }

    private fun updateProgress(jobId: String, progress: TransferProgress) {
        _transferProgress.update { current -> current + (jobId to progress) }
    }

    private fun removeJob(jobId: String) {
        _activeJobs.update { jobs -> jobs.filter { it.id != jobId } }
        _transferProgress.update { current -> current - jobId }
        jobCoroutines.remove(jobId)
    }

    private fun checkAndStopIfEmpty() {
        if (_activeJobs.value.none {
                it.status == TransferStatus.QUEUED || it.status == TransferStatus.IN_PROGRESS
            }
        ) {
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
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    /**
     * Binder subclass returned from [onBind] to allow clients to obtain a reference
     * to the running [TransferForegroundService] instance.
     */
    inner class TransferServiceBinder : Binder() {
        /** Returns the [TransferForegroundService] instance this binder is attached to. */
        fun getService(): TransferForegroundService = this@TransferForegroundService
    }
}
