package dev.nettools.android.data.repository

import dev.nettools.android.data.db.KnownHostDao
import dev.nettools.android.data.db.KnownHostEntity
import dev.nettools.android.domain.repository.KnownHostRepository
import javax.inject.Inject

/**
 * Room-backed implementation of [KnownHostRepository].
 *
 * @param dao The [KnownHostDao] used for database access.
 */
class KnownHostRepositoryImpl @Inject constructor(
    private val dao: KnownHostDao
) : KnownHostRepository {

    /** @inheritDoc */
    override suspend fun getByHost(host: String, port: Int): String? =
        dao.getByHostAndPort(host = host, port = port)?.fingerprint

    /** @inheritDoc */
    override suspend fun save(host: String, port: Int, fingerprint: String) {
        dao.upsert(
            KnownHostEntity(
                id = "$host:$port",
                host = host,
                port = port,
                fingerprint = fingerprint
            )
        )
    }

    /** @inheritDoc */
    override suspend fun delete(host: String, port: Int) {
        dao.deleteByHostAndPort(host = host, port = port)
    }
}
