package dev.nettools.android.ui.progress

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nettools.android.data.ssh.TransferProgress
import dev.nettools.android.domain.model.TransferJob
import dev.nettools.android.domain.model.TransferStatus
import dev.nettools.android.service.TransferForegroundService
import dev.nettools.android.service.TransferProgressHolder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Transfer Progress screen.
 * Provides live transfer progress and active job state from [TransferProgressHolder].
 * Supports in-app job cancellation via [cancelJob].
 *
 * @property context Application context used to send cancel intents to the service.
 * @property holder Shared in-memory holder bridging the service and UI.
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val holder: TransferProgressHolder,
) : ViewModel() {

    /** Per-job transfer progress keyed by job ID. */
    val progress: StateFlow<Map<String, TransferProgress>> = holder.progress

    /** Currently active (queued / in-progress / paused) jobs. */
    val activeJobs: StateFlow<List<TransferJob>> = holder.activeJobs

    private val _navigateBack = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * One-shot event that fires when the primary job is cancelled and no other
     * active jobs remain — the UI should pop back to the previous screen.
     */
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    /**
     * Cancels the transfer job identified by [jobId].
     *
     * Optimistically updates the job status to [TransferStatus.CANCELLED] and
     * sends a cancel intent to [TransferForegroundService]. When [isPrimary] is
     * true and no other active jobs remain, emits a [navigateBack] event.
     *
     * @param jobId The ID of the job to cancel.
     * @param isPrimary Whether this is the primary job for the current screen.
     */
    fun cancelJob(jobId: String, isPrimary: Boolean) {
        holder.updateJobStatus(jobId, TransferStatus.CANCELLED)
        val intent = Intent(context, TransferForegroundService::class.java).apply {
            action = TransferForegroundService.ACTION_CANCEL
            putExtra(TransferForegroundService.EXTRA_JOB_ID, jobId)
        }
        context.startService(intent)

        if (isPrimary) {
            val activeStatuses = setOf(
                TransferStatus.QUEUED,
                TransferStatus.IN_PROGRESS,
                TransferStatus.PAUSED,
            )
            val hasOtherActive = holder.activeJobs.value
                .any { it.id != jobId && it.status in activeStatuses }
            if (!hasOtherActive) {
                viewModelScope.launch { _navigateBack.emit(Unit) }
            }
        }
    }
}
