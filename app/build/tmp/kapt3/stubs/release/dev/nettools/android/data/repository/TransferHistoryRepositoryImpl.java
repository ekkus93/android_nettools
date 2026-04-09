package dev.nettools.android.data.repository;

import dev.nettools.android.data.db.TransferHistoryDao;
import dev.nettools.android.domain.model.TransferHistoryEntry;
import dev.nettools.android.domain.repository.TransferHistoryRepository;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

/**
 * Room-backed implementation of [TransferHistoryRepository].
 *
 * @param dao The [TransferHistoryDao] used for database access.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0014\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0007H\u0016J\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\tH\u0096@\u00a2\u0006\u0002\u0010\rJ\u000e\u0010\u000e\u001a\u00020\u000bH\u0096@\u00a2\u0006\u0002\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Ldev/nettools/android/data/repository/TransferHistoryRepositoryImpl;", "Ldev/nettools/android/domain/repository/TransferHistoryRepository;", "dao", "Ldev/nettools/android/data/db/TransferHistoryDao;", "<init>", "(Ldev/nettools/android/data/db/TransferHistoryDao;)V", "getAll", "Lkotlinx/coroutines/flow/Flow;", "", "Ldev/nettools/android/domain/model/TransferHistoryEntry;", "insert", "", "entry", "(Ldev/nettools/android/domain/model/TransferHistoryEntry;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearAll", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class TransferHistoryRepositoryImpl implements dev.nettools.android.domain.repository.TransferHistoryRepository {
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.data.db.TransferHistoryDao dao = null;
    
    @javax.inject.Inject()
    public TransferHistoryRepositoryImpl(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.db.TransferHistoryDao dao) {
        super();
    }
    
    /**
     * @inheritDoc
     */
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<dev.nettools.android.domain.model.TransferHistoryEntry>> getAll() {
        return null;
    }
    
    /**
     * @inheritDoc
     */
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferHistoryEntry entry, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * @inheritDoc
     */
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object clearAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}