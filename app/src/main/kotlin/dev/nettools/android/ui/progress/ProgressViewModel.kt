package dev.nettools.android.ui.progress

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.data.ssh.TransferProgress
import dev.nettools.android.domain.model.TransferJob
import dev.nettools.android.service.TransferProgressHolder
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel for the Transfer Progress screen.
 * Observes live progress from [TransferProgressHolder] without requiring
 * direct service binding.
 *
 * @property holder Singleton that holds in-flight transfer state.
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val holder: TransferProgressHolder,
) : ViewModel() {

    /** Per-job progress keyed by job ID. */
    val progress: StateFlow<Map<String, TransferProgress>> = holder.progress

    /** List of currently active or queued jobs. */
    val activeJobs: StateFlow<List<TransferJob>> = holder.activeJobs
}
