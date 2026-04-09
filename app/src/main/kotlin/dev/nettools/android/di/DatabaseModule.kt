package dev.nettools.android.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.nettools.android.data.db.AppDatabase
import dev.nettools.android.data.db.ConnectionProfileDao
import dev.nettools.android.data.db.KnownHostDao
import dev.nettools.android.data.db.TransferHistoryDao
import dev.nettools.android.data.repository.ConnectionProfileRepositoryImpl
import dev.nettools.android.data.repository.KnownHostRepositoryImpl
import dev.nettools.android.data.repository.TransferHistoryRepositoryImpl
import dev.nettools.android.domain.repository.ConnectionProfileRepository
import dev.nettools.android.domain.repository.KnownHostRepository
import dev.nettools.android.domain.repository.TransferHistoryRepository
import javax.inject.Singleton

/**
 * Hilt DI module that provides the Room [AppDatabase], all DAOs,
 * and repository implementations as singletons.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the singleton [AppDatabase] instance.
     *
     * @param context Application context.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = "nettools.db"
        ).build()

    /**
     * Provides the [ConnectionProfileDao] from the database.
     *
     * @param db The [AppDatabase] instance.
     */
    @Provides
    fun provideConnectionProfileDao(db: AppDatabase): ConnectionProfileDao =
        db.connectionProfileDao()

    /**
     * Provides the [TransferHistoryDao] from the database.
     *
     * @param db The [AppDatabase] instance.
     */
    @Provides
    fun provideTransferHistoryDao(db: AppDatabase): TransferHistoryDao =
        db.transferHistoryDao()

    /**
     * Provides the [KnownHostDao] from the database.
     *
     * @param db The [AppDatabase] instance.
     */
    @Provides
    fun provideKnownHostDao(db: AppDatabase): KnownHostDao =
        db.knownHostDao()

    /**
     * Binds [ConnectionProfileRepositoryImpl] as the [ConnectionProfileRepository] implementation.
     *
     * @param impl The concrete implementation.
     */
    @Provides
    @Singleton
    fun provideConnectionProfileRepository(
        impl: ConnectionProfileRepositoryImpl
    ): ConnectionProfileRepository = impl

    /**
     * Binds [TransferHistoryRepositoryImpl] as the [TransferHistoryRepository] implementation.
     *
     * @param impl The concrete implementation.
     */
    @Provides
    @Singleton
    fun provideTransferHistoryRepository(
        impl: TransferHistoryRepositoryImpl
    ): TransferHistoryRepository = impl

    /**
     * Binds [KnownHostRepositoryImpl] as the [KnownHostRepository] implementation.
     *
     * @param impl The concrete implementation.
     */
    @Provides
    @Singleton
    fun provideKnownHostRepository(
        impl: KnownHostRepositoryImpl
    ): KnownHostRepository = impl
}
