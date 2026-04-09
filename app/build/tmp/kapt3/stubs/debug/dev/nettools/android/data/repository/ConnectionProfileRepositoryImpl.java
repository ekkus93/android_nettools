package dev.nettools.android.data.repository;

import dev.nettools.android.data.db.ConnectionProfileDao;
import dev.nettools.android.domain.model.ConnectionProfile;
import dev.nettools.android.domain.repository.ConnectionProfileRepository;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

/**
 * Room-backed implementation of [ConnectionProfileRepository].
 *
 * @param dao The [ConnectionProfileDao] used for database access.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0014\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0007H\u0016J\u0018\u0010\n\u001a\u0004\u0018\u00010\t2\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\tH\u0096@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Ldev/nettools/android/data/repository/ConnectionProfileRepositoryImpl;", "Ldev/nettools/android/domain/repository/ConnectionProfileRepository;", "dao", "Ldev/nettools/android/data/db/ConnectionProfileDao;", "<init>", "(Ldev/nettools/android/data/db/ConnectionProfileDao;)V", "getAll", "Lkotlinx/coroutines/flow/Flow;", "", "Ldev/nettools/android/domain/model/ConnectionProfile;", "getById", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "save", "", "profile", "(Ldev/nettools/android/domain/model/ConnectionProfile;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "app_debug"})
public final class ConnectionProfileRepositoryImpl implements dev.nettools.android.domain.repository.ConnectionProfileRepository {
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.data.db.ConnectionProfileDao dao = null;
    
    @javax.inject.Inject()
    public ConnectionProfileRepositoryImpl(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.db.ConnectionProfileDao dao) {
        super();
    }
    
    /**
     * @inheritDoc
     */
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<dev.nettools.android.domain.model.ConnectionProfile>> getAll() {
        return null;
    }
    
    /**
     * @inheritDoc
     */
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super dev.nettools.android.domain.model.ConnectionProfile> $completion) {
        return null;
    }
    
    /**
     * @inheritDoc
     */
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object save(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.ConnectionProfile profile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * @inheritDoc
     */
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}