package dev.nettools.android.domain.model

/**
 * Lifecycle states for a curl execution.
 */
enum class CurlRunStatus {
    QUEUED,
    VALIDATING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED,
}

/**
 * Outcome of partial-file cleanup performed after a curl run.
 */
enum class CurlCleanupStatus {
    SKIPPED,
    SUCCEEDED,
    FAILED,
}

/**
 * Output stream kinds emitted by curl.
 */
enum class CurlOutputStream {
    STDOUT,
    STDERR,
}

/**
 * User-configurable settings for the curl feature.
 *
 * @property loggingEnabled Whether persistent curl logging is enabled.
 * @property saveHistoryEnabled Whether command history should be retained.
 * @property workspaceRootPath User-selected global workspace root, or null to use the app default.
 * @property stdoutBytesCap Maximum number of stdout bytes retained per run.
 * @property stderrBytesCap Maximum number of stderr bytes retained per run.
 */
data class CurlSettings(
    val loggingEnabled: Boolean = false,
    val saveHistoryEnabled: Boolean = false,
    val workspaceRootPath: String? = null,
    val stdoutBytesCap: Int = 4 * 1024 * 1024,
    val stderrBytesCap: Int = 1024 * 1024,
)

/**
 * Summary metadata for a curl run.
 *
 * @property id Unique run identifier.
 * @property commandText User-entered command text.
 * @property effectiveCommandText Rewritten command text actually executed after workspace path mapping.
 * @property normalizedCommandText Command text after preprocessing.
 * @property startedAt Epoch-millis start time.
 * @property finishedAt Epoch-millis finish time, if complete.
 * @property status Current run status.
 * @property exitCode Native curl exit code, if available.
 * @property durationMillis Execution duration in milliseconds, if known.
 * @property loggingEnabled Whether this run was recorded with logging enabled.
 * @property cleanupStatus Whether cleanup was skipped, succeeded, or failed.
 * @property cleanupWarning Optional warning when partial-file cleanup failed.
 */
data class CurlRunSummary(
    val id: String,
    val commandText: String,
    val effectiveCommandText: String? = null,
    val normalizedCommandText: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val status: CurlRunStatus,
    val exitCode: Int? = null,
    val durationMillis: Long? = null,
    val loggingEnabled: Boolean = false,
    val cleanupStatus: CurlCleanupStatus? = null,
    val cleanupWarning: String? = null,
)

/**
 * Retained stdout/stderr content for a curl run.
 *
 * @property stdoutText Persisted stdout content.
 * @property stderrText Persisted stderr content.
 * @property stdoutBytes Number of stored stdout bytes.
 * @property stderrBytes Number of stored stderr bytes.
 * @property stdoutTruncated Whether stdout exceeded the configured retention cap.
 * @property stderrTruncated Whether stderr exceeded the configured retention cap.
 */
data class CurlRunOutput(
    val stdoutText: String = "",
    val stderrText: String = "",
    val stdoutBytes: Int = 0,
    val stderrBytes: Int = 0,
    val stdoutTruncated: Boolean = false,
    val stderrTruncated: Boolean = false,
)

/**
 * Fully persisted curl run record, combining summary metadata and retained output.
 */
data class CurlRunRecord(
    val summary: CurlRunSummary,
    val output: CurlRunOutput = CurlRunOutput(),
)

/**
 * Validation error raised before a curl command is executed.
 *
 * @property message Human-readable description of the error.
 * @property token Offending token, when known.
 */
data class CurlValidationError(
    val message: String,
    val token: String? = null,
)

/**
 * Roles for local paths referenced by curl arguments.
 */
enum class CurlPathReferenceRole {
    INPUT_FILE,
    OUTPUT_FILE,
    CONFIG_FILE,
    PAYLOAD_FILE,
    HEADER_FILE,
    COOKIE_JAR,
    STDERR_FILE,
}

/**
 * A path-bearing argument extracted from a parsed curl command.
 *
 * @property originalPath Raw path text from the command.
 * @property normalizedPath Workspace-normalized Unix-style path.
 * @property role How curl intends to use the path.
 */
data class ParsedCurlPathReference(
    val originalPath: String,
    val normalizedPath: String,
    val role: CurlPathReferenceRole,
)

/**
 * Parsed and normalized curl command ready for later execution.
 *
 * @property originalText Original user input.
 * @property normalizedText Input after continuation collapsing and curl-prefix normalization.
 * @property tokens Tokenized command arguments including the leading `curl`.
 * @property pathReferences Local path references extracted from the arguments.
 */
data class ParsedCurlCommand(
    val originalText: String,
    val normalizedText: String,
    val tokens: List<String>,
    val pathReferences: List<ParsedCurlPathReference>,
)

/**
 * Result object returned by the curl command parser.
 *
 * @property command Parsed command when validation succeeds.
 * @property errors Validation errors encountered during preprocessing/tokenization.
 */
data class CurlCommandParseResult(
    val command: ParsedCurlCommand? = null,
    val errors: List<CurlValidationError> = emptyList(),
) {
    /** True when parsing succeeded without validation errors. */
    val isValid: Boolean
        get() = command != null && errors.isEmpty()
}

/**
 * Metadata emitted while a curl run is active.
 */
data class CurlOutputChunk(
    val stream: CurlOutputStream,
    val text: String,
)

/**
 * File or directory entry inside the curl workspace.
 *
 * @property path Unix-style workspace path beginning with `/`.
 * @property name Final path segment.
 * @property isDirectory Whether the entry is a directory.
 * @property sizeBytes Entry size in bytes for files, or 0 for directories.
 * @property modifiedAt Epoch-millis last-modified time.
 */
data class WorkspaceEntry(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val modifiedAt: Long,
)
