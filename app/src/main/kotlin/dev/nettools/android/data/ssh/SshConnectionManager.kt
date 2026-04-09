package dev.nettools.android.data.ssh

import dev.nettools.android.data.security.KnownHostsManager
import dev.nettools.android.data.security.KnownHostsManager.VerificationResult
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.TransferError
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import java.security.PublicKey
import javax.inject.Inject

/**
 * Manages SSHJ [SSHClient] connections.
 * Handles host key verification via [KnownHostsManager] and supports
 * both password and private-key authentication.
 */
class SshConnectionManager @Inject constructor() {

    companion object {
        private const val CONNECT_TIMEOUT_MS = 30_000
    }

    /**
     * Opens and authenticates an SSH connection.
     *
     * The caller is responsible for closing the returned [SSHClient] in a
     * `finally` block or `use {}` pattern.
     *
     * @param host Remote hostname or IP address.
     * @param port SSH port number.
     * @param username Remote account username.
     * @param authType Authentication method.
     * @param password Password string; required when [authType] is [AuthType.PASSWORD].
     * @param keyPath Path to a private key file; required when [authType] is [AuthType.PRIVATE_KEY].
     * @param knownHostsManager Manager used to verify the remote host key.
     * @return An authenticated, connected [SSHClient].
     * @throws TransferError on authentication, host-key, or connectivity failure.
     */
    @Throws(TransferError::class)
    fun connect(
        host: String,
        port: Int,
        username: String,
        authType: AuthType,
        password: String? = null,
        keyPath: String? = null,
        knownHostsManager: KnownHostsManager
    ): SSHClient {
        val client = SSHClient()
        client.connectTimeout = CONNECT_TIMEOUT_MS
        client.addHostKeyVerifier(buildHostKeyVerifier(host = host, port = port, knownHostsManager = knownHostsManager))

        try {
            client.connect(host, port)

            when (authType) {
                AuthType.PASSWORD -> {
                    requireNotNull(password) { "Password required for PASSWORD auth" }
                    client.authPassword(username, password)
                }
                AuthType.PRIVATE_KEY -> {
                    requireNotNull(keyPath) { "Key path required for PRIVATE_KEY auth" }
                    val keyProvider = client.loadKeys(keyPath)
                    client.authPublickey(username, keyProvider)
                }
            }

            return client
        } catch (e: Exception) {
            try { client.close() } catch (_: Exception) {}
            throw ErrorMapper.mapException(e)
        }
    }

    /**
     * Builds a [HostKeyVerifier] that delegates trust decisions to [KnownHostsManager].
     *
     * @param host Remote hostname.
     * @param port SSH port.
     * @param knownHostsManager The manager holding persisted fingerprints.
     */
    private fun buildHostKeyVerifier(
        host: String,
        port: Int,
        knownHostsManager: KnownHostsManager
    ): HostKeyVerifier = object : HostKeyVerifier {

        override fun verify(hostname: String, portNumber: Int, key: PublicKey): Boolean {
            val result = knownHostsManager.checkAndVerify(
                host = host,
                port = port,
                keyType = key.algorithm,
                keyBytes = key.encoded
            )
            return when (result) {
                is VerificationResult.Trusted -> true
                is VerificationResult.Rejected -> false
                // FirstConnect and KeyChanged require interactive resolution — reject for now;
                // callers should call checkAndVerify before connecting to handle these cases.
                is VerificationResult.FirstConnect -> false
                is VerificationResult.KeyChanged -> false
            }
        }

        override fun findExistingAlgorithms(hostname: String, portNumber: Int): List<String> =
            emptyList()
    }
}
