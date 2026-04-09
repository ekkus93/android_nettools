package dev.nettools.android.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.domain.model.TransferHistoryEntry
import dev.nettools.android.domain.repository.TransferHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Transfer History screen.
 *
 * Exposes a filtered list of history entries driven by [searchQuery],
 * and tracks which entry is currently selected for the detail dialog.
 *
 * @property repository The source of persisted transfer history.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TransferHistoryRepository,
) : ViewModel() {

    /** Live search/filter query entered by the user. */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Currently selected entry for the detail dialog; null when no dialog is shown. */
    private val _selectedEntry = MutableStateFlow<TransferHistoryEntry?>(null)
    val selectedEntry: StateFlow<TransferHistoryEntry?> = _selectedEntry.asStateFlow()

    /** History entries filtered by [searchQuery], newest first. */
    val history: StateFlow<List<TransferHistoryEntry>> =
        combine(repository.getAll(), _searchQuery) { entries, query ->
            if (query.isBlank()) entries
            else {
                val lower = query.trim().lowercase()
                entries.filter { e ->
                    e.fileName.lowercase().contains(lower) ||
                        e.host.lowercase().contains(lower) ||
                        e.remoteDir.lowercase().contains(lower)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Updates the search/filter query. */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** Shows the detail dialog for [entry]. */
    fun onEntrySelected(entry: TransferHistoryEntry) {
        _selectedEntry.value = entry
    }

    /** Closes the detail dialog. */
    fun onDetailDismissed() {
        _selectedEntry.value = null
    }

    /** Deletes all history entries from the database. */
    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }
}
