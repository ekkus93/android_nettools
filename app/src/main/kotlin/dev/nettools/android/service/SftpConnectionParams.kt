package dev.nettools.android.service

import dev.nettools.android.domain.model.AuthType

/**
 * Holds SSH connection parameters needed by [SftpBrowserViewModel] to open the remote
 * file browser. Stored in [TransferProgressHolder] so credentials never appear in
 * navigation arguments or on disk.
 *
 * @property host Remote hostname or IP address.
 * @property port SSH port number.
 * @property username Remote account username.
 * @property authType Authentication method.
 * @property password Plaintext password; non-null only for [AuthType.PASSWORD].
 * @property keyPath Local filesystem path to the private key file.
 */
data class SftpConnectionParams(
    val host: String,
    val port: Int,
    val username: String,
    val authType: AuthType,
    val password: String?,
    val keyPath: String?,
)
