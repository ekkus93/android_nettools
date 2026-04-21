package dev.nettools.android.data.ssh

import dev.nettools.android.domain.repository.KnownHostRepository

/**
 * In-memory [KnownHostRepository] for integration tests.
 * Stores fingerprints in a mutable map keyed by (host, port).
 */
class FakeKnownHostRepository : KnownHostRepository {
    val store = mutableMapOf<Pair<String, Int>, String>()

    override suspend fun getByHost(host: String, port: Int): String? = store[host to port]

    override suspend fun save(host: String, port: Int, fingerprint: String) {
        store[host to port] = fingerprint
    }

    override suspend fun delete(host: String, port: Int) {
        store.remove(host to port)
    }
}
