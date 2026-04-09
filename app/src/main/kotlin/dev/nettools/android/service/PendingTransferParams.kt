package dev.nettools.android.service

import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.TransferJob

/**
 * Holds all parameters required to execute an SSH transfer.
 * Stored in memory (never serialised to disk) so that credentials
 * never leave the process boundary.
 *
 * @property job The [TransferJob] descriptor.
 * @property host Remote hostname or IP address.
 * @property port SSH port number.
 * @property username Remote account username.
 * @property authType Authentication method.
 * @property password Plaintext password; present only for [AuthType.PASSWORD].
 * @property keyPath Local filesystem path to the private key file.
 */
data class PendingTransferParams(
    val job: TransferJob,
    val host: String,
    val port: Int,
    val username: String,
    val authType: AuthType,
    val password: String?,
    val keyPath: String?,
)
