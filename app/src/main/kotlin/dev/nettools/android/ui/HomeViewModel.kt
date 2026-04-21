package dev.nettools.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.domain.model.TransferStatus
import dev.nettools.android.service.TransferProgressHolder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private val ACTIVE_STATUSES = setOf(
    TransferStatus.QUEUED,
    TransferStatus.IN_PROGRESS,
    TransferStatus.PAUSED,
)

/**
 * ViewModel for the Home screen.
 * Observes active transfers from [TransferProgressHolder] and exposes
 * the count and first active job ID for the banner indicator.
 *
 * @property progressHolder Singleton holding in-flight transfer state.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val progressHolder: TransferProgressHolder,
) : ViewModel() {

    /**
     * Number of currently active transfers (queued, in-progress, or paused).
     * Emits 0 when no transfers are active.
     */
    val activeTransferCount: StateFlow<Int> = progressHolder.activeJobs
        .map { jobs -> jobs.count { it.status in ACTIVE_STATUSES } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /**
     * ID of the first active job, or null when no transfers are active.
     * Used to navigate directly to the primary active transfer's progress screen.
     */
    val firstActiveJobId: StateFlow<String?> = progressHolder.activeJobs
        .map { jobs -> jobs.firstOrNull { it.status in ACTIVE_STATUSES }?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
