package dev.nettools.android.domain.repository

import dev.nettools.android.domain.model.TransferHistoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for [TransferHistoryEntry] persistence.
 */
interface TransferHistoryRepository {

    /** Observes all transfer history entries ordered by timestamp descending. */
    fun getAll(): Flow<List<TransferHistoryEntry>>

    /**
     * Inserts a new history entry.
     *
     * @param entry The entry to record.
     */
    suspend fun insert(entry: TransferHistoryEntry)

    /** Deletes all history entries. */
    suspend fun clearAll()
}
