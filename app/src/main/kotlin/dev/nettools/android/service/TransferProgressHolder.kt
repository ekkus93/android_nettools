package dev.nettools.android.service

import dev.nettools.android.data.ssh.TransferProgress
import dev.nettools.android.domain.model.TransferJob
import dev.nettools.android.domain.model.TransferStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that bridges the [TransferForegroundService] and ViewModels.
 * ViewModels enqueue jobs and observe progress without requiring direct
 * service binding.
 */
@Singleton
class TransferProgressHolder @Inject constructor() {

    private val _progress = MutableStateFlow<Map<String, TransferProgress>>(emptyMap())

    /** Per-job transfer progress keyed by job ID. */
    val progress: StateFlow<Map<String, TransferProgress>> = _progress.asStateFlow()

    private val _activeJobs = MutableStateFlow<List<TransferJob>>(emptyList())

    /** Currently queued or in-progress jobs. */
    val activeJobs: StateFlow<List<TransferJob>> = _activeJobs.asStateFlow()

    private val pendingQueue = ConcurrentLinkedQueue<PendingTransferParams>()

    /**
     * Holds connection params for the SFTP browser screen.
     * Set by [TransferViewModel] immediately before navigating; consumed and cleared by
     * [SftpBrowserViewModel] on creation. Volatile because it is written on the main
     * thread and read on the ViewModel scope dispatcher.
     */
    @Volatile
    var pendingSftpConnectionParams: SftpConnectionParams? = null

    /** Adds [params] to the pending queue and registers the job as queued. */
    fun enqueue(params: PendingTransferParams) {
        _activeJobs.update { it + params.job.copy(status = TransferStatus.QUEUED) }
        pendingQueue.add(params)
    }

    /** Removes and returns the next pending transfer, or null if the queue is empty. */
    fun dequeue(): PendingTransferParams? = pendingQueue.poll()

    /** Updates the progress snapshot for a running job. */
    fun updateProgress(jobId: String, p: TransferProgress) {
        _progress.update { it + (jobId to p) }
    }

    /** Updates the status of a job already in [activeJobs]. */
    fun updateJobStatus(jobId: String, status: TransferStatus) {
        _activeJobs.update { jobs ->
            jobs.map { if (it.id == jobId) it.copy(status = status) else it }
        }
    }

    /**
     * Marks a job as failed and stores a human-readable [errorMessage] on the job entry.
     *
     * @param jobId ID of the failed job.
     * @param errorMessage User-facing description of the failure.
     */
    fun setJobFailed(jobId: String, errorMessage: String) {
        _activeJobs.update { jobs ->
            jobs.map {
                if (it.id == jobId) it.copy(status = TransferStatus.FAILED, errorMessage = errorMessage) else it
            }
        }
    }

    /** Removes the job and its progress entry from the active set. */
    fun removeJob(jobId: String) {
        _progress.update { it - jobId }
        _activeJobs.update { jobs -> jobs.filter { it.id != jobId } }
    }
}
