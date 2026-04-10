package dev.nettools.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.nettools.android.data.curl.BundledCurlBinaryProvider
import dev.nettools.android.data.curl.BundledCurlOptionCatalog
import dev.nettools.android.data.curl.CurlBinaryProvider
import dev.nettools.android.data.curl.CurlExecutor
import dev.nettools.android.data.curl.CurlOptionCatalog
import dev.nettools.android.data.curl.ProcessCurlExecutor
import dev.nettools.android.data.workspace.WorkspaceManager
import dev.nettools.android.domain.repository.WorkspaceRepository
import javax.inject.Singleton

/**
 * Hilt module for curl-related bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
object CurlModule {

    /**
     * Binds [BundledCurlOptionCatalog] as the active [CurlOptionCatalog].
     */
    @Provides
    @Singleton
    fun provideCurlOptionCatalog(
        impl: BundledCurlOptionCatalog,
    ): CurlOptionCatalog = impl

    /**
     * Binds [WorkspaceManager] as the active [WorkspaceRepository].
     */
    @Provides
    @Singleton
    fun provideWorkspaceRepository(
        impl: WorkspaceManager,
    ): WorkspaceRepository = impl

    /**
     * Binds [BundledCurlBinaryProvider] as the active [CurlBinaryProvider].
     */
    @Provides
    @Singleton
    fun provideCurlBinaryProvider(
        impl: BundledCurlBinaryProvider,
    ): CurlBinaryProvider = impl

    /**
     * Binds [ProcessCurlExecutor] as the active [CurlExecutor].
     */
    @Provides
    @Singleton
    fun provideCurlExecutor(
        impl: ProcessCurlExecutor,
    ): CurlExecutor = impl
}
