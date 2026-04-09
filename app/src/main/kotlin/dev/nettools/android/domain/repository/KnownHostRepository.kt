package dev.nettools.android.domain.repository

/**
 * Repository interface for known SSH host key fingerprints (TOFU store).
 * Fingerprints are stored as "SHA256:xxxx" strings keyed by host+port.
 */
interface KnownHostRepository {

    /**
     * Returns the stored fingerprint for the given host and port, or null if unknown.
     *
     * @param host Hostname or IP address.
     * @param port SSH port number.
     */
    suspend fun getByHost(host: String, port: Int): String?

    /**
     * Persists a trusted fingerprint for the given host and port.
     *
     * @param host Hostname or IP address.
     * @param port SSH port number.
     * @param fingerprint SHA-256 fingerprint string.
     */
    suspend fun save(host: String, port: Int, fingerprint: String)

    /**
     * Removes the known-host entry for the given host and port.
     *
     * @param host Hostname or IP address.
     * @param port SSH port number.
     */
    suspend fun delete(host: String, port: Int)
}
