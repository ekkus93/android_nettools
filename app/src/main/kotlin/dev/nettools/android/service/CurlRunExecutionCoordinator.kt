package dev.nettools.android.service

import dev.nettools.android.data.curl.CurlCommandWorkspaceAdapter
import dev.nettools.android.data.curl.CurlExecutionRequest
import dev.nettools.android.data.curl.CurlExecutor
import dev.nettools.android.data.curl.CurlRemoteCleanupExecutor
import dev.nettools.android.data.curl.CurlRemoteCleanupPlanner
import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.CurlOutputStream
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.repository.CurlRunRepository
import dev.nettools.android.util.CurlUserMessageFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes a prepared curl run while updating in-memory and persisted state.
 */
@Singleton
class CurlRunExecutionCoordinator @Inject constructor(
    private val curlRunHolder: CurlRunHolder,
    private val runRepository: CurlRunRepository,
    private val workspaceAdapter: CurlCommandWorkspaceAdapter,
    private val curlExecutor: CurlExecutor,
    private val remoteCleanupPlanner: CurlRemoteCleanupPlanner,
    private val remoteCleanupExecutor: CurlRemoteCleanupExecutor,
) {

    /**
     * Runs [params] to completion and returns the terminal execution outcome.
     */
    suspend fun execute(
        params: PendingCurlRunParams,
        onStarted: suspend () -> Unit = {},
    ): CurlRunExecutionOutcome {
        val preparedCommand = workspaceAdapter.prepareForExecution(params.parsedCommand)
        val effectiveCommandText = preparedCommand.effectiveCommandText
        curlRunHolder.startRun(
            runId = params.runId,
            commandText = params.rawCommandText,
            effectiveCommandText = effectiveCommandText,
        )
        runRepository.updateStatus(
            runId = params.runId,
            status = CurlRunStatus.IN_PROGRESS,
            effectiveCommandText = effectiveCommandText,
        )
        onStarted()

        try {
            val result = curlExecutor.execute(
                request = CurlExecutionRequest(
                    runId = params.runId,
                    parsedCommand = preparedCommand.command,
                    workspaceDirectory = params.workspaceRootPath,
                ),
            ) { chunk ->
                val isStdout = chunk.stream == CurlOutputStream.STDOUT
                curlRunHolder.appendOutput(isStdout = isStdout, chunk = chunk.text)
                if (params.loggingEnabled) {
                    runRepository.appendOutput(
                        runId = params.runId,
                        stream = chunk.stream,
                        text = chunk.text,
                        byteCap = if (isStdout) params.stdoutByteCap else params.stderrByteCap,
                    )
                }
            }

            return if (result.exitCode == 0) {
                runRepository.updateStatus(
                    runId = params.runId,
                    status = CurlRunStatus.COMPLETED,
                    finishedAt = System.currentTimeMillis(),
                    exitCode = result.exitCode,
                    durationMillis = result.durationMillis,
                    effectiveCommandText = effectiveCommandText,
                    cleanupStatus = CurlCleanupStatus.SKIPPED,
                )
                curlRunHolder.updateStatus(
                    status = CurlRunStatus.COMPLETED,
                    exitCode = result.exitCode,
                    cleanupStatus = CurlCleanupStatus.SKIPPED,
                )
                CurlRunExecutionOutcome(
                    status = CurlRunStatus.COMPLETED,
                    exitCode = result.exitCode,
                )
            } else {
                val cleanupResult = performCleanup(
                    command = preparedCommand.command,
                    preparedCommand = preparedCommand,
                    workspaceRootPath = params.workspaceRootPath,
                )
                runRepository.updateStatus(
                    runId = params.runId,
                    status = CurlRunStatus.FAILED,
                    finishedAt = System.currentTimeMillis(),
                    exitCode = result.exitCode,
                    durationMillis = result.durationMillis,
                    cleanupWarning = cleanupResult.warning,
                    effectiveCommandText = effectiveCommandText,
                    cleanupStatus = cleanupResult.status,
                )
                curlRunHolder.updateStatus(
                    status = CurlRunStatus.FAILED,
                    exitCode = result.exitCode,
                    cleanupWarning = cleanupResult.warning,
                    cleanupStatus = cleanupResult.status,
                )
                CurlRunExecutionOutcome(
                    status = CurlRunStatus.FAILED,
                    exitCode = result.exitCode,
                    failureReason = "Exit code: ${result.exitCode}",
                )
            }
        } catch (error: CancellationException) {
            val cancellationMessage = CurlUserMessageFormatter.executionCancelled()
            curlRunHolder.appendOutput(isStdout = false, chunk = cancellationMessage)
            if (params.loggingEnabled) {
                runRepository.appendOutput(
                    runId = params.runId,
                    stream = CurlOutputStream.STDERR,
                    text = cancellationMessage,
                    byteCap = params.stderrByteCap,
                )
            }
            val cleanupResult = withContext(NonCancellable) {
                performCleanup(
                    command = preparedCommand.command,
                    preparedCommand = preparedCommand,
                    workspaceRootPath = params.workspaceRootPath,
                )
            }
            runRepository.updateStatus(
                runId = params.runId,
                status = CurlRunStatus.CANCELLED,
                finishedAt = System.currentTimeMillis(),
                cleanupWarning = cleanupResult.warning,
                effectiveCommandText = effectiveCommandText,
                cleanupStatus = cleanupResult.status,
            )
            curlRunHolder.updateStatus(
                status = CurlRunStatus.CANCELLED,
                cleanupWarning = cleanupResult.warning,
                cleanupStatus = cleanupResult.status,
            )
            return CurlRunExecutionOutcome(status = CurlRunStatus.CANCELLED)
        } catch (error: Exception) {
            val failureReason = CurlUserMessageFormatter.executionFailure(error)
            val cleanupResult = performCleanup(
                command = preparedCommand.command,
                preparedCommand = preparedCommand,
                workspaceRootPath = params.workspaceRootPath,
            )
            curlRunHolder.appendOutput(isStdout = false, chunk = failureReason)
            if (params.loggingEnabled) {
                runRepository.appendOutput(
                    runId = params.runId,
                    stream = CurlOutputStream.STDERR,
                    text = failureReason,
                    byteCap = params.stderrByteCap,
                )
            }
            runRepository.updateStatus(
                runId = params.runId,
                status = CurlRunStatus.FAILED,
                finishedAt = System.currentTimeMillis(),
                cleanupWarning = cleanupResult.warning,
                effectiveCommandText = effectiveCommandText,
                cleanupStatus = cleanupResult.status,
            )
            curlRunHolder.updateStatus(
                status = CurlRunStatus.FAILED,
                cleanupWarning = cleanupResult.warning,
                cleanupStatus = cleanupResult.status,
            )
            return CurlRunExecutionOutcome(
                status = CurlRunStatus.FAILED,
                failureReason = failureReason,
            )
        }
    }

    private suspend fun performCleanup(
        command: dev.nettools.android.domain.model.ParsedCurlCommand,
        preparedCommand: dev.nettools.android.data.curl.PreparedCurlCommand,
        workspaceRootPath: String,
    ): dev.nettools.android.data.curl.CurlCleanupResult {
        val localCleanup = workspaceAdapter.cleanupPartialOutputs(preparedCommand)
        val remoteCleanup = remoteCleanupPlanner.plan(command)?.let { plan ->
            remoteCleanupExecutor.execute(plan = plan, workspaceDirectory = workspaceRootPath)
        } ?: dev.nettools.android.data.curl.CurlCleanupResult(status = CurlCleanupStatus.SKIPPED)

        val warnings = listOfNotNull(localCleanup.warning, remoteCleanup.warning)
        val status = when {
            localCleanup.status == CurlCleanupStatus.FAILED ||
                remoteCleanup.status == CurlCleanupStatus.FAILED -> CurlCleanupStatus.FAILED

            localCleanup.status == CurlCleanupStatus.SUCCEEDED ||
                remoteCleanup.status == CurlCleanupStatus.SUCCEEDED -> CurlCleanupStatus.SUCCEEDED

            else -> CurlCleanupStatus.SKIPPED
        }

        return dev.nettools.android.data.curl.CurlCleanupResult(
            status = status,
            warning = warnings.takeIf { it.isNotEmpty() }?.joinToString(separator = "\n"),
        )
    }
}

/**
 * Terminal result returned after executing a curl run.
 */
data class CurlRunExecutionOutcome(
    val status: CurlRunStatus,
    val exitCode: Int? = null,
    val failureReason: String? = null,
)
