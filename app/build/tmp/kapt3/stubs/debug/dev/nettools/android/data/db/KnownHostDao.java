package dev.nettools.android.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import kotlinx.coroutines.flow.Flow;

/**
 * Data Access Object for [KnownHostEntity].
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J \u0010\u0002\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\fJ\u001e\u0010\r\u001a\u00020\n2\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\b\u00a8\u0006\u000e\u00c0\u0006\u0003"}, d2 = {"Ldev/nettools/android/data/db/KnownHostDao;", "", "getByHostAndPort", "Ldev/nettools/android/data/db/KnownHostEntity;", "host", "", "port", "", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsert", "", "entity", "(Ldev/nettools/android/data/db/KnownHostEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteByHostAndPort", "app_debug"})
@androidx.room.Dao()
public abstract interface KnownHostDao {
    
    /**
     * Returns the known-host record for the given host and port, or null.
     *
     * @param host Hostname or IP address.
     * @param port SSH port number.
     */
    @androidx.room.Query(value = "SELECT * FROM known_hosts WHERE host = :host AND port = :port LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getByHostAndPort(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super dev.nettools.android.data.db.KnownHostEntity> $completion);
    
    /**
     * Inserts or replaces a known-host record.
     *
     * @param entity Entity to persist.
     */
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsert(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.db.KnownHostEntity entity, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Deletes the known-host record for the given host and port.
     *
     * @param host Hostname or IP address.
     * @param port SSH port number.
     */
    @androidx.room.Query(value = "DELETE FROM known_hosts WHERE host = :host AND port = :port")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteByHostAndPort(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}