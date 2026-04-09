package dev.nettools.android.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import kotlinx.coroutines.flow.Flow;

/**
 * Data Access Object for [TransferHistoryEntity].
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\bg\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H\'J\u0016\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\n\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u000b\u00a8\u0006\f\u00c0\u0006\u0003"}, d2 = {"Ldev/nettools/android/data/db/TransferHistoryDao;", "", "getAll", "Lkotlinx/coroutines/flow/Flow;", "", "Ldev/nettools/android/data/db/TransferHistoryEntity;", "insert", "", "entity", "(Ldev/nettools/android/data/db/TransferHistoryEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearAll", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
@androidx.room.Dao()
public abstract interface TransferHistoryDao {
    
    /**
     * Observes all history entries ordered by timestamp descending.
     */
    @androidx.room.Query(value = "SELECT * FROM transfer_history ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<dev.nettools.android.data.db.TransferHistoryEntity>> getAll();
    
    /**
     * Inserts a new history record.
     *
     * @param entity Entity to insert.
     */
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.db.TransferHistoryEntity entity, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Deletes all history records.
     */
    @androidx.room.Query(value = "DELETE FROM transfer_history")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}