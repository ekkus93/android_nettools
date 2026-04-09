package dev.nettools.android.data.ssh

import dev.nettools.android.domain.model.TransferError

/**
 * Maps low-level SSH and IO exceptions to [TransferError] sealed class values,
 * so that the UI layer never has to handle raw exceptions.
 */
object ErrorMapper {

    /**
     * Converts any [Exception] to the most specific [TransferError] subclass.
     *
     * @param e The exception to map.
     * @return A [TransferError] representing the failure.
     */
    fun mapException(e: Exception): TransferError = when {
        isAuthFailure(e) ->
            TransferError.AuthFailure(e.message ?: "Authentication failed")

        isHostUnreachable(e) ->
            TransferError.HostUnreachable(e.message ?: "Host unreachable")

        isPermissionDenied(e) ->
            TransferError.PermissionDenied(e.message ?: "Permission denied")

        isDiskFull(e) ->
            TransferError.DiskFull(e.message ?: "No space left on device")

        else ->
            TransferError.Unknown(e)
    }

    private fun isAuthFailure(e: Exception): Boolean {
        val className = e.javaClass.name
        return className.contains("UserAuthException") ||
            className.contains("userauth") ||
            e.message?.contains("Auth fail", ignoreCase = true) == true
    }

    private fun isHostUnreachable(e: Exception): Boolean =
        e is java.net.ConnectException ||
            e is java.net.UnknownHostException ||
            e is java.net.SocketTimeoutException ||
            e.message?.contains("Connection refused", ignoreCase = true) == true

    private fun isPermissionDenied(e: Exception): Boolean {
        val className = e.javaClass.name
        return (className.contains("RemoteAccessException") ||
            className.contains("SFTPException")) &&
            e.message?.contains("Permission denied", ignoreCase = true) == true
    }

    private fun isDiskFull(e: Exception): Boolean =
        e is java.io.IOException &&
            (e.message?.contains("No space left", ignoreCase = true) == true ||
                e.message?.contains("disk full", ignoreCase = true) == true)
}
