package dev.nettools.android.domain.repository

import dev.nettools.android.domain.model.CurlSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for curl-specific settings and defaults.
 */
interface CurlSettingsRepository {

    /** Observes the effective curl settings. */
    fun observeSettings(): Flow<CurlSettings>

    /** Returns the current effective curl settings snapshot. */
    suspend fun getSettings(): CurlSettings

    /** Enables or disables persistent curl logging. */
    suspend fun setLoggingEnabled(enabled: Boolean)

    /** Enables or disables saved command history retention. */
    suspend fun setSaveHistoryEnabled(enabled: Boolean)

    /** Updates the single global workspace root path, or resets it when null. */
    suspend fun setWorkspaceRootPath(path: String?)
}
