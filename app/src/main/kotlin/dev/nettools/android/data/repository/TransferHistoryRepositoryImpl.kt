package dev.nettools.android.data.repository

import dev.nettools.android.data.db.TransferHistoryDao
import dev.nettools.android.data.db.toEntity
import dev.nettools.android.domain.model.TransferHistoryEntry
import dev.nettools.android.domain.repository.TransferHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room-backed implementation of [TransferHistoryRepository].
 *
 * @param dao The [TransferHistoryDao] used for database access.
 */
class TransferHistoryRepositoryImpl @Inject constructor(
    private val dao: TransferHistoryDao
) : TransferHistoryRepository {

    /** @inheritDoc */
    override fun getAll(): Flow<List<TransferHistoryEntry>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    /** @inheritDoc */
    override suspend fun insert(entry: TransferHistoryEntry) {
        dao.insert(entry.toEntity())
    }

    /** @inheritDoc */
    override suspend fun clearAll() {
        dao.clearAll()
    }
}
