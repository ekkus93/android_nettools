package dev.nettools.android.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// ── ConnectionProfileDao ──────────────────────────────────────────────────────

/**
 * Data Access Object for [ConnectionProfileEntity].
 */
@Dao
interface ConnectionProfileDao {

    /** Observes all connection profiles ordered by name. */
    @Query("SELECT * FROM connection_profiles ORDER BY name ASC")
    fun getAll(): Flow<List<ConnectionProfileEntity>>

    /**
     * Returns the profile with the given [id], or null.
     *
     * @param id UUID of the profile.
     */
    @Query("SELECT * FROM connection_profiles WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ConnectionProfileEntity?

    /**
     * Inserts or replaces a profile.
     *
     * @param entity Entity to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ConnectionProfileEntity)

    /**
     * Deletes the profile with the given [id].
     *
     * @param id UUID of the profile to remove.
     */
    @Query("DELETE FROM connection_profiles WHERE id = :id")
    suspend fun deleteById(id: String)
}

// ── TransferHistoryDao ────────────────────────────────────────────────────────

/**
 * Data Access Object for [TransferHistoryEntity].
 */
@Dao
interface TransferHistoryDao {

    /** Observes all history entries ordered by timestamp descending. */
    @Query("SELECT * FROM transfer_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TransferHistoryEntity>>

    /**
     * Inserts a new history record.
     *
     * @param entity Entity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransferHistoryEntity)

    /** Deletes all history records. */
    @Query("DELETE FROM transfer_history")
    suspend fun clearAll()
}

// ── KnownHostDao ──────────────────────────────────────────────────────────────

/**
 * Data Access Object for [KnownHostEntity].
 */
@Dao
interface KnownHostDao {

    /**
     * Returns the known-host record for the given host and port, or null.
     *
     * @param host Hostname or IP address.
     * @param port SSH port number.
     */
    @Query("SELECT * FROM known_hosts WHERE host = :host AND port = :port LIMIT 1")
    suspend fun getByHostAndPort(host: String, port: Int): KnownHostEntity?

    /**
     * Inserts or replaces a known-host record.
     *
     * @param entity Entity to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: KnownHostEntity)

    /**
     * Deletes the known-host record for the given host and port.
     *
     * @param host Hostname or IP address.
     * @param port SSH port number.
     */
    @Query("DELETE FROM known_hosts WHERE host = :host AND port = :port")
    suspend fun deleteByHostAndPort(host: String, port: Int)
}
