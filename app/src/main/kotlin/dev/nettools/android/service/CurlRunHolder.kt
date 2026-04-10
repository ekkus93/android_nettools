package dev.nettools.android.service

import dev.nettools.android.di.ApplicationScope
import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.CurlRunStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory holder for the current active curl run and live output.
 */
@Singleton
class CurlRunHolder @Inject constructor(
    @param:ApplicationScope private val appScope: CoroutineScope,
) {

    @Volatile
    private var pendingRunParams: PendingCurlRunParams? = null

    private val _liveState = MutableStateFlow(CurlLiveRunState())

    /** Live state for the current or most recent curl run. */
    val liveState: StateFlow<CurlLiveRunState> = _liveState.asStateFlow()

    private val _activeRunId = MutableStateFlow<String?>(null)

    /** Active curl run ID, or null when no run is active. */
    val activeRunId: StateFlow<String?> = _activeRunId.asStateFlow()

    private val _cancelRequestedRunId = MutableStateFlow<String?>(null)

    /** Run ID that has been requested for cancellation. */
    val cancelRequestedRunId: StateFlow<String?> = _cancelRequestedRunId.asStateFlow()

    /** Stores pending run parameters to be consumed by the foreground service. */
    fun setPendingRun(params: PendingCurlRunParams) {
        pendingRunParams = params
    }

    /** Consumes and clears pending run parameters. */
    fun consumePendingRun(): PendingCurlRunParams? =
        pendingRunParams.also { pendingRunParams = null }

    /** Marks a curl run as active and clears previous live output. */
    fun startRun(runId: String, commandText: String, effectiveCommandText: String) {
        _activeRunId.value = runId
        _cancelRequestedRunId.value = null
        _liveState.value = CurlLiveRunState(
            runId = runId,
            commandText = commandText,
            effectiveCommandText = effectiveCommandText,
            status = CurlRunStatus.IN_PROGRESS,
        )
    }

    /** Appends live output to the appropriate in-memory stream. */
    fun appendOutput(isStdout: Boolean, chunk: String) {
        _liveState.update { current ->
            if (isStdout) {
                current.copy(stdoutText = current.stdoutText + chunk)
            } else {
                current.copy(stderrText = current.stderrText + chunk)
            }
        }
    }

    /** Updates live status fields for the active run. */
    fun updateStatus(
        status: CurlRunStatus,
        exitCode: Int? = null,
        cleanupWarning: String? = null,
        cleanupStatus: CurlCleanupStatus? = null,
    ) {
        _liveState.update { current ->
            current.copy(
                status = status,
                exitCode = exitCode ?: current.exitCode,
                cleanupWarning = cleanupWarning ?: current.cleanupWarning,
                cleanupStatus = cleanupStatus ?: current.cleanupStatus,
            )
        }
        if (status != CurlRunStatus.IN_PROGRESS && status != CurlRunStatus.QUEUED && status != CurlRunStatus.VALIDATING) {
            _activeRunId.value = null
            _cancelRequestedRunId.value = null
        }
    }

    /** Requests cancellation for the given run ID. */
    fun requestCancel(runId: String) {
        _cancelRequestedRunId.value = runId
    }
}

/**
 * Live UI-facing state for a curl run while the foreground service is active.
 */
data class CurlLiveRunState(
    val runId: String? = null,
    val commandText: String = "",
    val effectiveCommandText: String? = null,
    val status: CurlRunStatus? = null,
    val stdoutText: String = "",
    val stderrText: String = "",
    val exitCode: Int? = null,
    val cleanupStatus: CurlCleanupStatus? = null,
    val cleanupWarning: String? = null,
)
