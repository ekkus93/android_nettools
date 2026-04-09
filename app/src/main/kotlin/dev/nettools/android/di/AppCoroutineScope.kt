package dev.nettools.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier for a [CoroutineScope] tied to the application's lifetime.
 * Use this scope for work that must outlive individual ViewModels or components.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

/**
 * Hilt module that provides the application-scoped [CoroutineScope].
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {

    /**
     * Provides a [CoroutineScope] that lives for the entire application lifetime.
     * Uses [SupervisorJob] so that child failures do not cancel the scope.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
