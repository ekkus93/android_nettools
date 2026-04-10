package dev.nettools.android.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database for Android NetTools.
 * Holds all persistent entities: connection profiles, transfer history, known hosts,
 * and queued transfer jobs.
 */
@Database(
    entities = [
        ConnectionProfileEntity::class,
        TransferHistoryEntity::class,
        KnownHostEntity::class,
        QueuedJobEntity::class,
        CurlRunEntity::class,
        CurlRunOutputEntity::class,
        AppSettingEntity::class,
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** DAO for [ConnectionProfileEntity]. */
    abstract fun connectionProfileDao(): ConnectionProfileDao

    /** DAO for [TransferHistoryEntity]. */
    abstract fun transferHistoryDao(): TransferHistoryDao

    /** DAO for [KnownHostEntity]. */
    abstract fun knownHostDao(): KnownHostDao

    /** DAO for [QueuedJobEntity]. */
    abstract fun queuedJobDao(): QueuedJobDao

    /** DAO for [CurlRunEntity]. */
    abstract fun curlRunDao(): CurlRunDao

    /** DAO for [CurlRunOutputEntity]. */
    abstract fun curlRunOutputDao(): CurlRunOutputDao

    /** DAO for [AppSettingEntity]. */
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        /**
         * Migration from version 1 to 2: adds the `queued_jobs` table for
         * persisting transfer jobs across process death.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS queued_jobs (
                        jobId TEXT NOT NULL PRIMARY KEY,
                        host TEXT NOT NULL,
                        port INTEGER NOT NULL,
                        username TEXT NOT NULL,
                        authType TEXT NOT NULL,
                        keyPath TEXT,
                        profileId TEXT,
                        direction TEXT NOT NULL,
                        localPath TEXT NOT NULL,
                        remotePath TEXT NOT NULL,
                        enqueuedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Migration from version 2 to 3: adds `error_message` column to
         * `transfer_history` for surfacing failure reasons in the history detail dialog.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE transfer_history ADD COLUMN errorMessage TEXT"
                )
            }
        }

        /**
         * Migration from version 3 to 4: adds curl run, output, and app settings tables.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS curl_runs (
                        id TEXT NOT NULL PRIMARY KEY,
                        commandText TEXT NOT NULL,
                        normalizedCommandText TEXT NOT NULL,
                        startedAt INTEGER NOT NULL,
                        finishedAt INTEGER,
                        status TEXT NOT NULL,
                        exitCode INTEGER,
                        durationMillis INTEGER,
                        loggingEnabled INTEGER NOT NULL,
                        cleanupWarning TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS curl_run_outputs (
                        runId TEXT NOT NULL PRIMARY KEY,
                        stdoutText TEXT NOT NULL,
                        stderrText TEXT NOT NULL,
                        stdoutBytes INTEGER NOT NULL,
                        stderrBytes INTEGER NOT NULL,
                        stdoutTruncated INTEGER NOT NULL,
                        stderrTruncated INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS app_settings (
                        `key` TEXT NOT NULL PRIMARY KEY,
                        value TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
