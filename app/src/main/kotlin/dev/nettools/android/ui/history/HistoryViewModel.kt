package dev.nettools.android.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.domain.model.HistoryStatus
import dev.nettools.android.domain.model.TransferHistoryEntry
import dev.nettools.android.domain.repository.TransferHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Transfer History screen.
 *
 * Exposes a filtered list of history entries driven by [searchQuery] and [statusFilter].
 * Tracks which entry is currently selected for the detail dialog.
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

    /** Optional status filter; null means all statuses are shown. */
    private val _statusFilter = MutableStateFlow<HistoryStatus?>(null)
    val statusFilter: StateFlow<HistoryStatus?> = _statusFilter.asStateFlow()

    /** Currently selected entry for the detail dialog; null when no dialog is shown. */
    private val _selectedEntry = MutableStateFlow<TransferHistoryEntry?>(null)
    val selectedEntry: StateFlow<TransferHistoryEntry?> = _selectedEntry.asStateFlow()

    /**
     * History entries filtered by [searchQuery] and [statusFilter], newest first.
     * When [searchQuery] is blank and [statusFilter] is null, all entries are returned.
     */
    val history: StateFlow<List<TransferHistoryEntry>> =
        combine(repository.getAll(), _searchQuery, _statusFilter) { entries, query, filter ->
            entries.filter { e ->
                val matchesQuery = if (query.isBlank()) true
                    else {
                        val lower = query.trim().lowercase()
                        e.fileName.lowercase().contains(lower) ||
                            e.host.lowercase().contains(lower) ||
                            e.remoteDir.lowercase().contains(lower)
                    }
                val matchesFilter = filter == null || e.status == filter
                matchesQuery && matchesFilter
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Updates the search/filter query. */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Sets the status filter. Pass null to show all statuses.
     *
     * @param status The [HistoryStatus] to filter by, or null to clear the filter.
     */
    fun onStatusFilterChange(status: HistoryStatus?) {
        _statusFilter.value = status
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
