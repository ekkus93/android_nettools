package dev.nettools.android.ui.curl

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.usecase.curl.ObserveActiveCurlRunUseCase
import dev.nettools.android.domain.usecase.curl.StartCurlRunUseCase
import dev.nettools.android.service.CurlForegroundService
import dev.nettools.android.service.CurlRunHolder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the main curl runner screen.
 */
data class CurlRunnerUiState(
    val commandText: String = "",
    val validationMessages: List<String> = emptyList(),
    val activeRunId: String? = null,
    val activeCommandText: String = "",
    val activeStatus: CurlRunStatus? = null,
    val errorMessage: String? = null,
) {
    /** True when a curl run is already active and another cannot start. */
    val hasActiveRun: Boolean
        get() = activeRunId != null && activeStatus in setOf(
            CurlRunStatus.QUEUED,
            CurlRunStatus.VALIDATING,
            CurlRunStatus.IN_PROGRESS,
        )
}

/**
 * ViewModel for the main curl runner screen.
 */
@HiltViewModel
class CurlRunnerViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val startCurlRun: StartCurlRunUseCase,
    private val observeActiveCurlRun: ObserveActiveCurlRunUseCase,
    private val curlRunHolder: CurlRunHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurlRunnerUiState())
    val uiState: StateFlow<CurlRunnerUiState> = _uiState.asStateFlow()

    private val _navigateToResults = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToResults: SharedFlow<String> = _navigateToResults.asSharedFlow()

    init {
        viewModelScope.launch {
            observeActiveCurlRun().collect { liveState ->
                _uiState.update {
                    it.copy(
                        activeRunId = liveState.runId,
                        activeCommandText = liveState.commandText,
                        activeStatus = liveState.status,
                    )
                }
            }
        }
    }

    /** Updates the draft curl command. */
    fun onCommandChange(value: String) {
        _uiState.update {
            it.copy(
                commandText = value,
                validationMessages = emptyList(),
                errorMessage = null,
            )
        }
    }

    /** Clears the current one-shot error message. */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Starts a new curl run when validation succeeds. */
    fun runCommand() {
        if (_uiState.value.hasActiveRun) {
            _uiState.update { it.copy(errorMessage = "Only one curl run can be active at a time.") }
            return
        }

        viewModelScope.launch {
            val result = startCurlRun(_uiState.value.commandText)
            if (!result.isReady) {
                _uiState.update {
                    it.copy(validationMessages = result.errors.map { error -> error.message })
                }
                return@launch
            }

            val pendingRun = requireNotNull(result.pendingRun)
            curlRunHolder.setPendingRun(pendingRun)
            ContextCompat.startForegroundService(
                context,
                Intent(context, CurlForegroundService::class.java),
            )
            _navigateToResults.tryEmit(pendingRun.runId)
        }
    }
}
