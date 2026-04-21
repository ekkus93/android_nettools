package dev.nettools.android.data.ssh

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nettools.android.data.security.KnownHostsManager
import dev.nettools.android.data.security.KnownHostsManager.VerificationResult
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.TransferError
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.Factory
import net.schmizz.sshj.common.SecurityUtils
import net.schmizz.sshj.transport.kex.KeyExchange
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.PublicKey
import java.util.Base64
import javax.inject.Inject

/**
 * Manages SSHJ [SSHClient] connections.
 * Handles host key verification via [KnownHostsManager] and supports
 * both password and private-key authentication.
 *
 * When a key path begins with `content://`, the key file is read via the
 * [ContentResolver] and staged in a temporary cache file for SSHJ to load.
 *
 * @property context Application context for [ContentResolver] access.
 */
class SshConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val CONNECT_TIMEOUT_MS = 30_000
        private const val TAG = "SshConnectionManager"
        private const val CURVE_25519_PREFIX = "curve25519"
    }

    /**
     * Opens a TCP + SSH transport connection to [host]:[port], captures the server's
     * host key fingerprint, then immediately closes the connection. The host key is
     * **not** authenticated — this is used purely for TOFU pre-flight checks.
     *
     * @param host Remote hostname or IP address.
     * @param port SSH port number.
     * @return The SHA-256 fingerprint of the server's host key (e.g. "SHA256:xxxx"),
     *         or null if the host is unreachable before the key exchange completes.
     */
    fun peekHostKey(host: String, port: Int): String? {
        var captured: String? = null
        val client = createClient()
        client.connectTimeout = CONNECT_TIMEOUT_MS
        client.addHostKeyVerifier(object : HostKeyVerifier {
            override fun verify(hostname: String, portNumber: Int, key: PublicKey): Boolean {
                val digest = MessageDigest.getInstance("SHA-256").digest(key.encoded)
                val b64 = Base64.getEncoder().withoutPadding().encodeToString(digest)
                captured = "SHA256:$b64"
                return false // reject — we only want the fingerprint
            }

            override fun findExistingAlgorithms(hostname: String, portNumber: Int): List<String> =
                emptyList()
        })
        runCatching { client.connect(host, port) }
            .onFailure { Log.d(TAG, "Unable to capture host key fingerprint before SSH auth", it) }
        runCatching { client.close() }
            .onFailure { Log.d(TAG, "Failed to close SSH client after host-key probe", it) }
        return captured
    }

    /**
     * Opens and authenticates an SSH connection.
     *
     * The caller is responsible for closing the returned [SSHClient] in a
     * `finally` block or `use {}` pattern.
     *
     * When [keyPath] begins with `content://`, the key file is opened via
     * [ContentResolver] and staged to a temporary file in [Context.getCacheDir].
     *
     * @param host Remote hostname or IP address.
     * @param port SSH port number.
     * @param username Remote account username.
     * @param authType Authentication method.
     * @param password Password string; required when [authType] is [AuthType.PASSWORD].
     * @param keyPath Path to a private key file or a `content://` URI string;
     *        required when [authType] is [AuthType.PRIVATE_KEY].
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
        val client = createClient()
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
                    val keyProvider = if (keyPath.startsWith("content://")) {
                        loadKeyFromContentUri(Uri.parse(keyPath), client)
                    } else {
                        client.loadKeys(keyPath)
                    }
                    client.authPublickey(username, keyProvider)
                }
            }

            return client
        } catch (e: Exception) {
            try {
                client.close()
            } catch (closeError: Exception) {
                Log.d(TAG, "Failed to close SSH client after connection error", closeError)
            }
            throw ErrorMapper.mapException(e)
        }
    }

    /**
     * Loads a private key from a `content://` URI by copying the key bytes to a
     * temporary file in the app cache directory and delegating to [SSHClient.loadKeys].
     * The temporary file is always deleted after the key provider is constructed.
     *
     * @param uri The content URI referencing the private key file.
     * @param client The [SSHClient] used to parse and load the key.
     * @return A [KeyProvider] backed by the private key data.
     * @throws IOException if the [ContentResolver] cannot open the URI.
     */
    private fun loadKeyFromContentUri(uri: Uri, client: SSHClient): KeyProvider {
        val hashCode = uri.lastPathSegment?.hashCode() ?: "key"
        val tempFile = File(context.cacheDir, "tmp_key_$hashCode")
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("ContentResolver returned null for URI: $uri")
            inputStream.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            return client.loadKeys(tempFile.absolutePath)
        } finally {
            tempFile.delete()
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

    /**
     * Creates an SSHJ client configured to use the platform crypto provider and removes
     * Curve25519 key exchanges, which depend on X25519 support not provided on this device.
     */
    private fun createClient(): SSHClient {
        SecurityUtils.setRegisterBouncyCastle(false)
        SecurityUtils.setSecurityProvider(null)

        val config = DefaultConfig()
        config.setKeyExchangeFactories(filterUnsupportedKeyExchangeFactories(config.getKeyExchangeFactories()))
        return SSHClient(config)
    }

    internal fun filterUnsupportedKeyExchangeFactories(
        factories: List<Factory.Named<KeyExchange>>
    ): List<Factory.Named<KeyExchange>> =
        factories.filterNot { factory ->
            factory.getName().startsWith(CURVE_25519_PREFIX, ignoreCase = true)
        }
}
