package dev.nettools.android.domain.model

/**
 * Sealed hierarchy of transfer errors, mapped from low-level SSH/IO exceptions
 * so that the UI layer never needs to handle raw exceptions.
 */
sealed class TransferError : Exception() {

    /** Authentication failed (wrong password or rejected key). */
    data class AuthFailure(override val message: String) : TransferError()

    /** Remote host could not be reached (DNS failure, connection refused, timeout). */
    data class HostUnreachable(override val message: String) : TransferError()

    /** Insufficient permissions on the remote or local filesystem. */
    data class PermissionDenied(override val message: String) : TransferError()

    /** No space left on the target device. */
    data class DiskFull(override val message: String) : TransferError()

    /** The remote host's key was not previously seen (first-connect). */
    data class UnknownHostKey(val fingerprint: String, val host: String) : TransferError()

    /** The remote host's key changed since the last recorded connection. */
    data class HostKeyChanged(
        val host: String,
        val oldFingerprint: String,
        val newFingerprint: String
    ) : TransferError()

    /** The transfer was cancelled by the user. */
    data object TransferCancelled : TransferError()

    /** Any other unexpected error. */
    data class Unknown(val rootCause: Throwable) : TransferError()
}
