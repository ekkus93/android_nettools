package dev.nettools.android.service

import dev.nettools.android.domain.model.ParsedCurlCommand

/**
 * Parameters for a prepared curl run waiting to be executed by [CurlForegroundService].
 */
data class PendingCurlRunParams(
    val runId: String,
    val rawCommandText: String,
    val parsedCommand: ParsedCurlCommand,
    val workspaceRootPath: String,
    val loggingEnabled: Boolean,
    val stdoutByteCap: Int,
    val stderrByteCap: Int,
)
