package dev.nettools.android.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import kotlinx.coroutines.flow.Flow;

/**
 * Data Access Object for [ConnectionProfileEntity].
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H\'J\u0018\u0010\u0006\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\t\u00a8\u0006\u000f\u00c0\u0006\u0003"}, d2 = {"Ldev/nettools/android/data/db/ConnectionProfileDao;", "", "getAll", "Lkotlinx/coroutines/flow/Flow;", "", "Ldev/nettools/android/data/db/ConnectionProfileEntity;", "getById", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsert", "", "entity", "(Ldev/nettools/android/data/db/ConnectionProfileEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteById", "app_debug"})
@androidx.room.Dao()
public abstract interface ConnectionProfileDao {
    
    /**
     * Observes all connection profiles ordered by name.
     */
    @androidx.room.Query(value = "SELECT * FROM connection_profiles ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<dev.nettools.android.data.db.ConnectionProfileEntity>> getAll();
    
    /**
     * Returns the profile with the given [id], or null.
     *
     * @param id UUID of the profile.
     */
    @androidx.room.Query(value = "SELECT * FROM connection_profiles WHERE id = :id LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super dev.nettools.android.data.db.ConnectionProfileEntity> $completion);
    
    /**
     * Inserts or replaces a profile.
     *
     * @param entity Entity to persist.
     */
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsert(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.db.ConnectionProfileEntity entity, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Deletes the profile with the given [id].
     *
     * @param id UUID of the profile to remove.
     */
    @androidx.room.Query(value = "DELETE FROM connection_profiles WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}