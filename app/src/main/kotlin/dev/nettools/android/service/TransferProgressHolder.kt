package dev.nettools.android.service

import dev.nettools.android.data.ssh.TransferProgress
import dev.nettools.android.domain.model.TransferJob
import dev.nettools.android.domain.model.TransferStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    /** Adds [params] to the pending queue and registers the job as active. */
    fun enqueue(params: PendingTransferParams) {
        _activeJobs.value = _activeJobs.value + params.job
        pendingQueue.add(params)
    }

    /** Removes and returns the next pending transfer, or null if the queue is empty. */
    fun dequeue(): PendingTransferParams? = pendingQueue.poll()

    /** Updates the progress snapshot for a running job. */
    fun updateProgress(jobId: String, p: TransferProgress) {
        _progress.value = _progress.value + (jobId to p)
    }

    /** Updates the status of a job already in [activeJobs]. */
    fun updateJobStatus(jobId: String, status: TransferStatus) {
        _activeJobs.value = _activeJobs.value.map {
            if (it.id == jobId) it.copy(status = status) else it
        }
    }

    /** Removes the job and its progress entry from the active set. */
    fun removeJob(jobId: String) {
        _progress.value = _progress.value - jobId
        _activeJobs.value = _activeJobs.value.filter { it.id != jobId }
    }
}
