package dev.nettools.android.util

import java.io.FileNotFoundException

/**
 * Converts common curl and workspace failures into concise user-facing messages.
 */
object CurlUserMessageFormatter {

    /** Returns the standard user-facing message for a cancelled curl run. */
    fun executionCancelled(): String = "Curl run cancelled by user."

    /** Returns the standard user-facing message when runtime metadata cannot be loaded. */
    fun runtimeMetadataUnavailable(): String = "Bundled curl runtime metadata is unavailable on this build."

    /** Returns a user-facing warning for partial remote cleanup failures. */
    fun remoteCleanupFailure(details: String): String {
        val suffix = details.takeIf { it.isNotBlank() }?.let { " $it" }.orEmpty()
        return "Curl could not confirm remote partial-upload cleanup.$suffix"
    }

    /**
     * Maps an execution-layer [error] into a user-facing message.
     */
    fun executionFailure(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("Cannot run program", ignoreCase = true) &&
                message.contains("curl", ignoreCase = true) ->
                "The embedded curl runtime is unavailable on this build."

            error is FileNotFoundException ||
                message.contains("No such file or directory", ignoreCase = true) ->
                "A referenced workspace file or directory could not be found."

            message.contains("Permission denied", ignoreCase = true) ->
                "Curl could not access one of the requested files."

            message.contains("ssl", ignoreCase = true) ||
                message.contains("tls", ignoreCase = true) ||
                message.contains("certificate", ignoreCase = true) ->
                "Curl reported a TLS or certificate error."

            message.isBlank() -> "Curl execution failed."
            else -> message
        }
    }

    /**
     * Maps a workspace-layer [error] for the given [action] into a user-facing message.
     */
    fun workspaceFailure(action: String, error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("already exists", ignoreCase = true) ->
                "A workspace item with that name already exists."

            message.contains("Permission may have been revoked", ignoreCase = true) ->
                "Android no longer allows access to the selected file or destination."

            message.contains("does not exist", ignoreCase = true) ->
                "The selected workspace item no longer exists."

            message.contains("not a workspace directory", ignoreCase = true) ->
                "Choose an existing workspace directory."

            message.contains("must not be blank", ignoreCase = true) ||
                message.contains("cannot be blank", ignoreCase = true) ->
                "Enter a non-empty name before continuing."

            message.isBlank() -> "Unable to $action."
            else -> message
        }
    }
}
