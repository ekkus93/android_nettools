package dev.nettools.android.data.repository

import dev.nettools.android.data.db.ConnectionProfileDao
import dev.nettools.android.data.db.toEntity
import dev.nettools.android.domain.model.ConnectionProfile
import dev.nettools.android.domain.repository.ConnectionProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room-backed implementation of [ConnectionProfileRepository].
 *
 * @param dao The [ConnectionProfileDao] used for database access.
 */
class ConnectionProfileRepositoryImpl @Inject constructor(
    private val dao: ConnectionProfileDao
) : ConnectionProfileRepository {

    /** @inheritDoc */
    override fun getAll(): Flow<List<ConnectionProfile>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    /** @inheritDoc */
    override suspend fun getById(id: String): ConnectionProfile? =
        dao.getById(id)?.toDomain()

    /** @inheritDoc */
    override suspend fun save(profile: ConnectionProfile) {
        dao.upsert(profile.toEntity())
    }

    /** @inheritDoc */
    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }
}
