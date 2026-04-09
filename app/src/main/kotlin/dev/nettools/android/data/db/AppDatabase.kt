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
    ],
    version = 3,
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
    }
}
