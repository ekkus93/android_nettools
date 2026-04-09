package dev.nettools.android.data.repository;

import dev.nettools.android.data.db.KnownHostDao;
import dev.nettools.android.data.db.KnownHostEntity;
import dev.nettools.android.domain.repository.KnownHostRepository;
import javax.inject.Inject;

/**
 * Room-backed implementation of [KnownHostRepository].
 *
 * @param dao The [KnownHostDao] used for database access.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J \u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0006\u0010\b\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\nH\u0096@\u00a2\u0006\u0002\u0010\u000bJ&\u0010\f\u001a\u00020\r2\u0006\u0010\b\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000e\u001a\u00020\u0007H\u0096@\u00a2\u0006\u0002\u0010\u000fJ\u001e\u0010\u0010\u001a\u00020\r2\u0006\u0010\b\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\nH\u0096@\u00a2\u0006\u0002\u0010\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Ldev/nettools/android/data/repository/KnownHostRepositoryImpl;", "Ldev/nettools/android/domain/repository/KnownHostRepository;", "dao", "Ldev/nettools/android/data/db/KnownHostDao;", "<init>", "(Ldev/nettools/android/data/db/KnownHostDao;)V", "getByHost", "", "host", "port", "", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "save", "", "fingerprint", "(Ljava/lang/String;ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "app_debug"})
public final class KnownHostRepositoryImpl implements dev.nettools.android.domain.repository.KnownHostRepository {
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.data.db.KnownHostDao dao = null;
    
    @javax.inject.Inject()
    public KnownHostRepositoryImpl(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.db.KnownHostDao dao) {
        super();
    }
    
    /**
     * @inheritDoc
     */
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getByHost(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * @inheritDoc
     */
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object save(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    java.lang.String fingerprint, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * @inheritDoc
     */
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}