package dev.nettools.android.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Room database for Android NetTools.
 * Holds all persistent entities: connection profiles, transfer history, and known hosts.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H&J\b\u0010\u0006\u001a\u00020\u0007H&J\b\u0010\b\u001a\u00020\tH&\u00a8\u0006\n"}, d2 = {"Ldev/nettools/android/data/db/AppDatabase;", "Landroidx/room/RoomDatabase;", "<init>", "()V", "connectionProfileDao", "Ldev/nettools/android/data/db/ConnectionProfileDao;", "transferHistoryDao", "Ldev/nettools/android/data/db/TransferHistoryDao;", "knownHostDao", "Ldev/nettools/android/data/db/KnownHostDao;", "app_debug"})
@androidx.room.Database(entities = {dev.nettools.android.data.db.ConnectionProfileEntity.class, dev.nettools.android.data.db.TransferHistoryEntity.class, dev.nettools.android.data.db.KnownHostEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    
    public AppDatabase() {
        super();
    }
    
    /**
     * DAO for [ConnectionProfileEntity].
     */
    @org.jetbrains.annotations.NotNull()
    public abstract dev.nettools.android.data.db.ConnectionProfileDao connectionProfileDao();
    
    /**
     * DAO for [TransferHistoryEntity].
     */
    @org.jetbrains.annotations.NotNull()
    public abstract dev.nettools.android.data.db.TransferHistoryDao transferHistoryDao();
    
    /**
     * DAO for [KnownHostEntity].
     */
    @org.jetbrains.annotations.NotNull()
    public abstract dev.nettools.android.data.db.KnownHostDao knownHostDao();
}