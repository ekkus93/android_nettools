package dev.nettools.android.domain.repository

import dev.nettools.android.domain.model.ConnectionProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for [ConnectionProfile] persistence.
 * All database operations are abstracted behind this interface
 * to keep the domain layer independent of Room.
 */
interface ConnectionProfileRepository {

    /** Observes all saved connection profiles as a reactive [Flow]. */
    fun getAll(): Flow<List<ConnectionProfile>>

    /**
     * Returns the profile with the given [id], or null if not found.
     *
     * @param id UUID of the profile.
     */
    suspend fun getById(id: String): ConnectionProfile?

    /**
     * Inserts or updates a [ConnectionProfile].
     *
     * @param profile The profile to persist.
     */
    suspend fun save(profile: ConnectionProfile)

    /**
     * Permanently removes the profile identified by [id].
     *
     * @param id UUID of the profile to delete.
     */
    suspend fun delete(id: String)
}
