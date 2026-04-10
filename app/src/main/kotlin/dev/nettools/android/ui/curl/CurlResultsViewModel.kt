package dev.nettools.android.ui.curl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.CurlRunRecord
import dev.nettools.android.domain.model.CurlRunOutput
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.repository.CurlRunRepository
import dev.nettools.android.domain.usecase.curl.CancelActiveCurlRunUseCase
import dev.nettools.android.domain.usecase.curl.ObserveActiveCurlRunUseCase
import dev.nettools.android.domain.usecase.curl.SaveCurlOutputUseCase
import dev.nettools.android.util.CurlUserMessageFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the curl results screen.
 */
data class CurlResultsUiState(
    val runId: String = "",
    val commandText: String = "",
    val effectiveCommandText: String? = null,
    val status: CurlRunStatus? = null,
    val stdoutText: String = "",
    val stderrText: String = "",
    val stdoutTruncated: Boolean = false,
    val stderrTruncated: Boolean = false,
    val exitCode: Int? = null,
    val durationMillis: Long? = null,
    val cleanupStatus: CurlCleanupStatus? = null,
    val cleanupWarning: String? = null,
    val isMissing: Boolean = false,
    val saveMessage: String? = null,
) {
    /** True when the run is still cancellable. */
    val canCancel: Boolean
        get() = status in setOf(CurlRunStatus.QUEUED, CurlRunStatus.VALIDATING, CurlRunStatus.IN_PROGRESS)
}

/**
 * ViewModel for the curl results screen.
 */
@HiltViewModel
class CurlResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: CurlRunRepository,
    observeActiveCurlRun: ObserveActiveCurlRunUseCase,
    private val cancelActiveCurlRun: CancelActiveCurlRunUseCase,
    private val saveCurlOutput: SaveCurlOutputUseCase,
) : ViewModel() {

    private val runId: String = checkNotNull(savedStateHandle["runId"])
    private val _uiState = MutableStateFlow(CurlResultsUiState(runId = runId))
    val uiState: StateFlow<CurlResultsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeById(runId),
                observeActiveCurlRun(),
            ) { record, liveState ->
                record.toUiState(runId = runId, liveState = liveState)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /** Cancels the displayed curl run when it is still active. */
    fun cancelRun() {
        if (_uiState.value.canCancel) {
            cancelActiveCurlRun(runId)
            _uiState.update { it.copy(status = CurlRunStatus.CANCELLED) }
        }
    }

    /** Saves the current output snapshot into the workspace. */
    fun saveOutput() {
        val output = CurlRunOutput(
            stdoutText = _uiState.value.stdoutText,
            stderrText = _uiState.value.stderrText,
            stdoutTruncated = _uiState.value.stdoutTruncated,
            stderrTruncated = _uiState.value.stderrTruncated,
        )
        viewModelScope.launch {
            runCatching {
                saveCurlOutput(runId, output)
            }.onSuccess { savedPath ->
                _uiState.update { it.copy(saveMessage = "Saved output to $savedPath") }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(saveMessage = CurlUserMessageFormatter.workspaceFailure("save the output", error))
                }
            }
        }
    }

    /** Clears the transient save message after the UI displays it. */
    fun clearSaveMessage() {
        _uiState.update { it.copy(saveMessage = null) }
    }
}

private fun CurlRunRecord?.toUiState(
    runId: String,
    liveState: dev.nettools.android.service.CurlLiveRunState,
): CurlResultsUiState {
    val isLive = liveState.runId == runId
    val summary = this?.summary
    val output = this?.output
    val commandText = when {
        isLive && liveState.commandText.isNotBlank() -> liveState.commandText
        !summary?.commandText.isNullOrBlank() -> requireNotNull(summary).commandText
        else -> "Command hidden because saved command history is disabled."
    }
    val effectiveCommandText = when {
        isLive && !liveState.effectiveCommandText.isNullOrBlank() -> liveState.effectiveCommandText
        !summary?.effectiveCommandText.isNullOrBlank() -> requireNotNull(summary).effectiveCommandText
        else -> null
    }

    if (summary == null && !isLive) {
        return CurlResultsUiState(
            runId = runId,
            commandText = commandText,
            effectiveCommandText = effectiveCommandText,
            isMissing = true,
        )
    }

    return CurlResultsUiState(
        runId = runId,
        commandText = commandText,
        effectiveCommandText = effectiveCommandText,
        status = if (isLive) liveState.status ?: summary?.status else summary?.status,
        stdoutText = if (isLive) liveState.stdoutText else output?.stdoutText.orEmpty(),
        stderrText = if (isLive) liveState.stderrText else output?.stderrText.orEmpty(),
        stdoutTruncated = output?.stdoutTruncated ?: false,
        stderrTruncated = output?.stderrTruncated ?: false,
        exitCode = if (isLive) liveState.exitCode ?: summary?.exitCode else summary?.exitCode,
        durationMillis = summary?.durationMillis,
        cleanupStatus = if (isLive) liveState.cleanupStatus ?: summary?.cleanupStatus else summary?.cleanupStatus,
        cleanupWarning = if (isLive) liveState.cleanupWarning ?: summary?.cleanupWarning else summary?.cleanupWarning,
        isMissing = false,
    )
}
