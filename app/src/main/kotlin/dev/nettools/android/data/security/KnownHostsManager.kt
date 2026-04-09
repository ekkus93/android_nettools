package dev.nettools.android.data.security

import dev.nettools.android.domain.repository.KnownHostRepository
import kotlinx.coroutines.runBlocking
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages SSH host key verification using a Trust-On-First-Use (TOFU) policy.
 * Fingerprints are stored via [KnownHostRepository] and compared on subsequent connections.
 *
 * @param repository Persistent storage for known host fingerprints.
 */
@Singleton
class KnownHostsManager @Inject constructor(
    private val repository: KnownHostRepository
) {

    /**
     * Result of a host key verification attempt.
     */
    sealed class VerificationResult {
        /** The key matches a previously trusted fingerprint. */
        data object Trusted : VerificationResult()

        /**
         * No key has been seen for this host before.
         * @property fingerprint SHA-256 fingerprint of the presented key.
         */
        data class FirstConnect(val fingerprint: String) : VerificationResult()

        /**
         * The key has changed since the last trusted connection — possible MITM attack.
         * @property old Previously trusted fingerprint.
         * @property new Fingerprint of the currently presented key.
         */
        data class KeyChanged(val old: String, val new: String) : VerificationResult()

        /** The key was explicitly rejected. */
        data object Rejected : VerificationResult()
    }

    /**
     * Checks whether the presented host key should be trusted.
     *
     * @param host Remote hostname or IP address.
     * @param port SSH port number.
     * @param keyType Key algorithm (e.g. "RSA", "EC").
     * @param keyBytes Raw encoded public key bytes.
     * @return A [VerificationResult] indicating the trust decision.
     */
    fun checkAndVerify(
        host: String,
        port: Int,
        keyType: String,
        keyBytes: ByteArray
    ): VerificationResult {
        val newFingerprint = computeFingerprint(keyBytes)
        val stored = runBlocking { repository.getByHost(host = host, port = port) }

        return when {
            stored == null -> VerificationResult.FirstConnect(newFingerprint)
            stored == newFingerprint -> VerificationResult.Trusted
            else -> VerificationResult.KeyChanged(old = stored, new = newFingerprint)
        }
    }

    /**
     * Persists a trusted fingerprint for the given host, accepting it for future connections.
     *
     * @param host Remote hostname or IP address.
     * @param port SSH port number.
     * @param fingerprint SHA-256 fingerprint to trust.
     */
    fun acceptHost(host: String, port: Int, fingerprint: String) {
        runBlocking { repository.save(host = host, port = port, fingerprint = fingerprint) }
    }

    /**
     * Rejects the host key — no-op, nothing is persisted.
     */
    fun rejectHost() {
        // No fingerprint is saved; the connection will fail on next verification.
    }

    /**
     * Returns the stored fingerprint for [host]:[port], or null if not yet trusted.
     *
     * @param host Remote hostname or IP address.
     * @param port SSH port number.
     */
    fun getStoredFingerprint(host: String, port: Int): String? =
        runBlocking { repository.getByHost(host = host, port = port) }

    /**
     * Computes the SHA-256 fingerprint of a public key in "SHA256:xxxx" format,
     * matching the OpenSSH fingerprint style.
     *
     * @param keyBytes Raw encoded public key bytes.
     * @return Fingerprint string prefixed with "SHA256:".
     */
    fun computeFingerprint(keyBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(keyBytes)
        val base64 = Base64.getEncoder().withoutPadding().encodeToString(digest)
        return "SHA256:$base64"
    }
}
