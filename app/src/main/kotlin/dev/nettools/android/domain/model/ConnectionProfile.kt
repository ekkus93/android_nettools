package dev.nettools.android.domain.model

/**
 * Represents a saved SSH connection profile used to establish SCP/SFTP connections.
 *
 * @property id Unique UUID identifier.
 * @property name Human-readable name for the profile.
 * @property host Hostname or IP address of the remote machine.
 * @property port SSH port number (default 22).
 * @property username Remote account username.
 * @property authType Authentication method to use.
 * @property keyPath Path to the private key file on the device, if using key auth.
 * @property savePassword Whether to persist the password in encrypted storage.
 */
data class ConnectionProfile(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: AuthType,
    val keyPath: String? = null,
    val savePassword: Boolean = false
)

/** Authentication method for an SSH connection. */
enum class AuthType {
    PASSWORD,
    PRIVATE_KEY
}
