package dev.nettools.android.domain.usecase.curl

import dev.nettools.android.data.curl.CurlCommandWorkspaceAdapter
import dev.nettools.android.domain.model.CurlRunRecord
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.model.CurlRunSummary
import dev.nettools.android.domain.repository.CurlRunRepository
import dev.nettools.android.domain.repository.CurlSettingsRepository
import dev.nettools.android.domain.repository.WorkspaceRepository
import dev.nettools.android.service.PendingCurlRunParams
import java.util.UUID
import javax.inject.Inject

/**
 * Validates input, persists an initial curl run record, and prepares service parameters.
 */
class StartCurlRunUseCase @Inject constructor(
    private val validateCurlCommand: ValidateCurlCommandUseCase,
    private val workspaceAdapter: CurlCommandWorkspaceAdapter,
    private val settingsRepository: CurlSettingsRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val runRepository: CurlRunRepository,
) {

    /**
     * Prepares a new curl run from raw [input].
     */
    suspend operator fun invoke(input: String): CurlStartResult {
        val parsed = validateCurlCommand(input)
        if (!parsed.isValid) {
            return CurlStartResult(errors = parsed.errors)
        }

        val parsedCommand = requireNotNull(parsed.command)
        val pathErrors = workspaceAdapter.validate(parsedCommand)
        if (pathErrors.isNotEmpty()) {
            return CurlStartResult(errors = pathErrors)
        }
        val settings = settingsRepository.getSettings()
        val runId = UUID.randomUUID().toString()
        val shouldPersistHistory = settings.saveHistoryEnabled
        val summary = CurlRunSummary(
            id = runId,
            commandText = if (shouldPersistHistory) input else "",
            effectiveCommandText = null,
            normalizedCommandText = if (shouldPersistHistory) parsedCommand.normalizedText else "",
            startedAt = System.currentTimeMillis(),
            status = CurlRunStatus.QUEUED,
            loggingEnabled = settings.loggingEnabled,
        )
        runRepository.upsert(CurlRunRecord(summary = summary))

        return CurlStartResult(
            pendingRun = PendingCurlRunParams(
                runId = runId,
                rawCommandText = input,
                parsedCommand = parsedCommand,
                workspaceRootPath = workspaceRepository.getWorkspaceRootPath(),
                loggingEnabled = settings.loggingEnabled,
                stdoutByteCap = settings.stdoutBytesCap,
                stderrByteCap = settings.stderrBytesCap,
            ),
        )
    }
}

/**
 * Start-run result object used by the curl runner UI.
 */
data class CurlStartResult(
    val pendingRun: PendingCurlRunParams? = null,
    val errors: List<dev.nettools.android.domain.model.CurlValidationError> = emptyList(),
) {
    /** True when a run is ready to be dispatched to the service. */
    val isReady: Boolean
        get() = pendingRun != null && errors.isEmpty()
}
