package dev.nettools.android.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for Android NetTools.
 * Holds all persistent entities: connection profiles, transfer history, and known hosts.
 */
@Database(
    entities = [
        ConnectionProfileEntity::class,
        TransferHistoryEntity::class,
        KnownHostEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** DAO for [ConnectionProfileEntity]. */
    abstract fun connectionProfileDao(): ConnectionProfileDao

    /** DAO for [TransferHistoryEntity]. */
    abstract fun transferHistoryDao(): TransferHistoryDao

    /** DAO for [KnownHostEntity]. */
    abstract fun knownHostDao(): KnownHostDao
}
