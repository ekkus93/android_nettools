package dev.nettools.android.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [CurlRunEntity].
 */
@Dao
interface CurlRunDao {

    /** Observes all curl runs ordered by start time descending. */
    @Query("SELECT * FROM curl_runs ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<CurlRunEntity>>

    /** Observes a single curl run by ID. */
    @Query("SELECT * FROM curl_runs WHERE id = :runId LIMIT 1")
    fun observeById(runId: String): Flow<CurlRunEntity?>

    /** Returns the curl run with the given ID, or null. */
    @Query("SELECT * FROM curl_runs WHERE id = :runId LIMIT 1")
    suspend fun getById(runId: String): CurlRunEntity?

    /** Inserts or replaces a curl run summary entity. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CurlRunEntity)

    /** Deletes all stored curl run summaries. */
    @Query("DELETE FROM curl_runs")
    suspend fun clearAll()
}

/**
 * Data Access Object for [CurlRunOutputEntity].
 */
@Dao
interface CurlRunOutputDao {

    /** Observes all retained curl outputs. */
    @Query("SELECT * FROM curl_run_outputs")
    fun observeAll(): Flow<List<CurlRunOutputEntity>>

    /** Observes retained output for a single curl run. */
    @Query("SELECT * FROM curl_run_outputs WHERE runId = :runId LIMIT 1")
    fun observeById(runId: String): Flow<CurlRunOutputEntity?>

    /** Returns retained output for a single curl run, or null. */
    @Query("SELECT * FROM curl_run_outputs WHERE runId = :runId LIMIT 1")
    suspend fun getById(runId: String): CurlRunOutputEntity?

    /** Inserts or replaces retained output for a curl run. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CurlRunOutputEntity)

    /** Deletes all retained curl outputs. */
    @Query("DELETE FROM curl_run_outputs")
    suspend fun clearAll()
}

/**
 * Data Access Object for [AppSettingEntity].
 */
@Dao
interface AppSettingDao {

    /** Observes all app settings rows. */
    @Query("SELECT * FROM app_settings")
    fun observeAll(): Flow<List<AppSettingEntity>>

    /** Returns all settings rows. */
    @Query("SELECT * FROM app_settings")
    suspend fun getAll(): List<AppSettingEntity>

    /** Inserts or replaces a single setting row. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppSettingEntity)

    /** Deletes the setting row with the given key. */
    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun deleteByKey(key: String)
}
