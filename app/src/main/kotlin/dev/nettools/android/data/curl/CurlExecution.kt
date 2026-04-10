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
 * Resolved embedded curl runtime assets.
 *
 * @property executablePath Absolute path to the extracted curl executable.
 * @property caCertificatePath Absolute path to the extracted CA bundle, if one should be auto-injected.
 */
data class CurlRuntime(
    val executablePath: String,
    val caCertificatePath: String? = null,
)

/**
 * Snapshot of metadata reported by the bundled curl runtime.
 */
data class CurlRuntimeMetadata(
    val bundledCurlVersion: String,
    val supportedProtocols: List<String>,
    val supportedFeatures: List<String>,
    val http2Supported: Boolean,
)

/**
 * Result of attempting to read bundled curl runtime metadata.
 */
sealed interface CurlRuntimeMetadataResult {
    /**
     * Metadata was read successfully.
     */
    data class Available(val metadata: CurlRuntimeMetadata) : CurlRuntimeMetadataResult

    /**
     * Metadata could not be read.
     */
    data class Unavailable(val message: String) : CurlRuntimeMetadataResult
}

/**
 * Resolves the executable path used to launch curl.
 */
interface CurlBinaryProvider {
    /** Returns the resolved runtime paths needed to launch curl. */
    suspend fun getRuntime(): CurlRuntime
}

/**
 * Reads metadata describing the bundled curl runtime.
 */
interface CurlRuntimeMetadataProvider {
    /** Returns the bundled runtime metadata or an explicit unavailability reason. */
    fun getRuntimeMetadata(): CurlRuntimeMetadataResult
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
