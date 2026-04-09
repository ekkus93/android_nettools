package dev.nettools.android.service

import dev.nettools.android.data.db.QueuedJobDao
import dev.nettools.android.data.db.QueuedJobEntity
import dev.nettools.android.data.security.CredentialStore
import dev.nettools.android.data.ssh.TransferProgress
import dev.nettools.android.di.ApplicationScope
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.domain.model.TransferJob
import dev.nettools.android.domain.model.TransferStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that bridges [TransferForegroundService] and ViewModels.
 * ViewModels enqueue jobs and observe progress without requiring direct service binding.
 * Queued jobs are persisted to Room (minus password) so they survive process death.
 */
@Singleton
class TransferProgressHolder @Inject constructor(
    private val queuedJobDao: QueuedJobDao,
    private val credentialStore: CredentialStore,
    @param:ApplicationScope private val appScope: CoroutineScope,
) {

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

    /** Prefix used to key job passwords in [CredentialStore]. */
    private fun queuePasswordKey(jobId: String) = "queue_pw_$jobId"

    /**
     * Adds [params] to the pending queue, registers the job as queued, and
     * persists the job to Room (password stored separately in [CredentialStore]).
     */
    fun enqueue(params: PendingTransferParams) {
        _activeJobs.update { it + params.job.copy(status = TransferStatus.QUEUED) }
        pendingQueue.add(params)
        appScope.launch(Dispatchers.IO) {
            params.password?.let { pw ->
                credentialStore.savePassword(queuePasswordKey(params.job.id), pw)
            }
            queuedJobDao.upsert(params.toEntity())
        }
    }

    /** Removes and returns the next pending transfer, or null if the queue is empty. */
    fun dequeue(): PendingTransferParams? = pendingQueue.poll()

    /** Returns true when transfers are still waiting in the in-memory queue. */
    fun hasPendingJobs(): Boolean = pendingQueue.isNotEmpty()

    /**
     * Restores persisted queued jobs from Room into the in-memory queue.
     * Should be called once by [TransferForegroundService] on start so that
     * jobs survive process death.
     *
     * @return list of restored params (already added to the queue).
     */
    suspend fun restorePersistedJobs(): List<PendingTransferParams> {
        val entities = queuedJobDao.getAll()
        val restoredJobIds = _activeJobs.value.map { it.id }.toSet()
        return entities
            .filter { it.jobId !in restoredJobIds }
            .map { entity ->
                val password = credentialStore.getPassword(queuePasswordKey(entity.jobId))
                entity.toParams(password)
            }
            .also { list ->
                list.forEach { params ->
                    _activeJobs.update { it + params.job.copy(status = TransferStatus.QUEUED) }
                    pendingQueue.add(params)
                }
            }
    }

    /**
     * Removes a completed/failed/cancelled job from Room and clears its queued password.
     * Must be called from the service's `NonCancellable` finally block.
     */
    fun clearPersistedJob(jobId: String) {
        appScope.launch(Dispatchers.IO) {
            credentialStore.deletePassword(queuePasswordKey(jobId))
            queuedJobDao.deleteById(jobId)
        }
    }

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

// ── Conversion helpers ────────────────────────────────────────────────────────

private fun PendingTransferParams.toEntity(): QueuedJobEntity = QueuedJobEntity(
    jobId = job.id,
    host = host,
    port = port,
    username = username,
    authType = authType.name,
    keyPath = keyPath,
    profileId = job.profileId.ifBlank { null },
    direction = job.direction.name,
    localPath = job.localPath,
    remotePath = job.remotePath,
    enqueuedAt = System.currentTimeMillis(),
)

private fun QueuedJobEntity.toParams(password: String?): PendingTransferParams = PendingTransferParams(
    job = TransferJob(
        id = jobId,
        profileId = profileId ?: "",
        direction = TransferDirection.valueOf(direction),
        localPath = localPath,
        remotePath = remotePath,
        status = TransferStatus.QUEUED,
    ),
    host = host,
    port = port,
    username = username,
    authType = AuthType.valueOf(authType),
    password = password,
    keyPath = keyPath,
)
