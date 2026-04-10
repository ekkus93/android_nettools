package dev.nettools.android.domain.repository

import dev.nettools.android.domain.model.CurlOutputStream
import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.CurlRunRecord
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.model.CurlRunSummary
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for persisted curl run metadata and retained output.
 */
interface CurlRunRepository {

    /** Observes all stored curl runs ordered by start time descending. */
    fun observeAll(): Flow<List<CurlRunRecord>>

    /** Observes a single curl run by ID. */
    fun observeById(runId: String): Flow<CurlRunRecord?>

    /** Returns the stored curl run with the given ID, or null. */
    suspend fun getById(runId: String): CurlRunRecord?

    /** Inserts or replaces the full run record. */
    suspend fun upsert(record: CurlRunRecord)

    /** Inserts or replaces summary metadata while preserving existing output. */
    suspend fun upsertSummary(summary: CurlRunSummary)

    /** Appends retained output for the given stream, respecting the provided byte cap. */
    suspend fun appendOutput(runId: String, stream: CurlOutputStream, text: String, byteCap: Int)

    /** Updates terminal/non-terminal run status fields. */
    suspend fun updateStatus(
        runId: String,
        status: CurlRunStatus,
        finishedAt: Long? = null,
        exitCode: Int? = null,
        durationMillis: Long? = null,
        cleanupWarning: String? = null,
        effectiveCommandText: String? = null,
        cleanupStatus: CurlCleanupStatus? = null,
    )

    /** Deletes all stored curl runs and retained output. */
    suspend fun clearAll()
}
