package dev.nettools.android.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.domain.model.TransferHistoryEntry
import dev.nettools.android.domain.repository.TransferHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Transfer History screen.
 * Exposes history entries as a [StateFlow] and provides a clear-all action.
 *
 * @property repository The source of persisted transfer history.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TransferHistoryRepository,
) : ViewModel() {

    /** All recorded transfers, newest first. */
    val history: StateFlow<List<TransferHistoryEntry>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Deletes all history entries from the database. */
    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }
}
