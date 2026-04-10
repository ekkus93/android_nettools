package dev.nettools.android.data.curl

import dev.nettools.android.domain.model.CurlOutputChunk
import dev.nettools.android.domain.model.ParsedCurlCommand

/**
 * Prepared curl execution request ready for native/tool invocation.
 *
 * @property runId Unique run identifier.
 * @property parsedCommand Parsed curl command tokens.
 * @property workspaceDirectory Absolute workspace directory used as the process working directory.
 */
data class CurlExecutionRequest(
    val runId: String,
    val parsedCommand: ParsedCurlCommand,
    val workspaceDirectory: String,
)

/**
 * Final execution result for a curl run.
 *
 * @property exitCode Process exit code.
 * @property durationMillis Total execution duration in milliseconds.
 */
data class CurlExecutionResult(
    val exitCode: Int,
    val durationMillis: Long,
)

/**
 * Resolves the executable path used to launch curl.
 */
interface CurlBinaryProvider {
    /** Returns the executable path or command name to launch curl. */
    suspend fun getExecutablePath(): String
}

/**
 * Executes prepared curl requests and streams output back to Kotlin.
 */
interface CurlExecutor {
    /**
     * Executes [request], streaming stdout/stderr chunks to [onOutput].
     */
    suspend fun execute(
        request: CurlExecutionRequest,
        onOutput: suspend (CurlOutputChunk) -> Unit,
    ): CurlExecutionResult
}
