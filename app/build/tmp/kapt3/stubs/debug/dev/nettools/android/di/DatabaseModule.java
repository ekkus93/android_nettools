package dev.nettools.android.di;

import android.content.Context;
import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import dev.nettools.android.data.db.AppDatabase;
import dev.nettools.android.data.db.ConnectionProfileDao;
import dev.nettools.android.data.db.KnownHostDao;
import dev.nettools.android.data.db.TransferHistoryDao;
import dev.nettools.android.data.repository.ConnectionProfileRepositoryImpl;
import dev.nettools.android.data.repository.KnownHostRepositoryImpl;
import dev.nettools.android.data.repository.TransferHistoryRepositoryImpl;
import dev.nettools.android.domain.repository.ConnectionProfileRepository;
import dev.nettools.android.domain.repository.KnownHostRepository;
import dev.nettools.android.domain.repository.TransferHistoryRepository;
import javax.inject.Singleton;

/**
 * Hilt DI module that provides the Room [AppDatabase], all DAOs,
 * and repository implementations as singletons.
 */
@dagger.Module()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0005H\u0007J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\n\u001a\u00020\u0005H\u0007J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\n\u001a\u00020\u0005H\u0007J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u0007J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0011\u001a\u00020\u0015H\u0007J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0011\u001a\u00020\u0018H\u0007\u00a8\u0006\u0019"}, d2 = {"Ldev/nettools/android/di/DatabaseModule;", "", "<init>", "()V", "provideAppDatabase", "Ldev/nettools/android/data/db/AppDatabase;", "context", "Landroid/content/Context;", "provideConnectionProfileDao", "Ldev/nettools/android/data/db/ConnectionProfileDao;", "db", "provideTransferHistoryDao", "Ldev/nettools/android/data/db/TransferHistoryDao;", "provideKnownHostDao", "Ldev/nettools/android/data/db/KnownHostDao;", "provideConnectionProfileRepository", "Ldev/nettools/android/domain/repository/ConnectionProfileRepository;", "impl", "Ldev/nettools/android/data/repository/ConnectionProfileRepositoryImpl;", "provideTransferHistoryRepository", "Ldev/nettools/android/domain/repository/TransferHistoryRepository;", "Ldev/nettools/android/data/repository/TransferHistoryRepositoryImpl;", "provideKnownHostRepository", "Ldev/nettools/android/domain/repository/KnownHostRepository;", "Ldev/nettools/android/data/repository/KnownHostRepositoryImpl;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class DatabaseModule {
    @org.jetbrains.annotations.NotNull()
    public static final dev.nettools.android.di.DatabaseModule INSTANCE = null;
    
    private DatabaseModule() {
        super();
    }
    
    /**
     * Provides the singleton [AppDatabase] instance.
     *
     * @param context Application context.
     */
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.data.db.AppDatabase provideAppDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    /**
     * Provides the [ConnectionProfileDao] from the database.
     *
     * @param db The [AppDatabase] instance.
     */
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.data.db.ConnectionProfileDao provideConnectionProfileDao(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.db.AppDatabase db) {
        return null;
    }
    
    /**
     * Provides the [TransferHistoryDao] from the database.
     *
     * @param db The [AppDatabase] instance.
     */
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.data.db.TransferHistoryDao provideTransferHistoryDao(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.db.AppDatabase db) {
        return null;
    }
    
    /**
     * Provides the [KnownHostDao] from the database.
     *
     * @param db The [AppDatabase] instance.
     */
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.data.db.KnownHostDao provideKnownHostDao(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.db.AppDatabase db) {
        return null;
    }
    
    /**
     * Binds [ConnectionProfileRepositoryImpl] as the [ConnectionProfileRepository] implementation.
     *
     * @param impl The concrete implementation.
     */
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.repository.ConnectionProfileRepository provideConnectionProfileRepository(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.repository.ConnectionProfileRepositoryImpl impl) {
        return null;
    }
    
    /**
     * Binds [TransferHistoryRepositoryImpl] as the [TransferHistoryRepository] implementation.
     *
     * @param impl The concrete implementation.
     */
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.repository.TransferHistoryRepository provideTransferHistoryRepository(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.repository.TransferHistoryRepositoryImpl impl) {
        return null;
    }
    
    /**
     * Binds [KnownHostRepositoryImpl] as the [KnownHostRepository] implementation.
     *
     * @param impl The concrete implementation.
     */
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.repository.KnownHostRepository provideKnownHostRepository(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.repository.KnownHostRepositoryImpl impl) {
        return null;
    }
}