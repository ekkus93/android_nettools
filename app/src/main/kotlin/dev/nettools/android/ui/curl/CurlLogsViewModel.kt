package dev.nettools.android.ui.curl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.domain.model.CurlRunRecord
import dev.nettools.android.domain.usecase.curl.ClearCurlLogsUseCase
import dev.nettools.android.domain.repository.CurlRunRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the stored curl logs/history screen.
 */
@HiltViewModel
class CurlLogsViewModel @Inject constructor(
    repository: CurlRunRepository,
    private val clearCurlLogs: ClearCurlLogsUseCase,
) : ViewModel() {

    /** Persisted curl runs, newest first. */
    val runs: StateFlow<List<CurlRunRecord>> = repository.observeAll()
        .map { records -> records.sortedByDescending { it.summary.startedAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Clears all persisted curl log entries. */
    fun clearAll() {
        viewModelScope.launch { clearCurlLogs() }
    }
}
