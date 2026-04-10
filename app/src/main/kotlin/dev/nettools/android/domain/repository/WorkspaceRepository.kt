package dev.nettools.android.domain.repository

import dev.nettools.android.domain.model.WorkspaceEntry

/**
 * Repository interface for the curl workspace file model.
 */
interface WorkspaceRepository {

    /** Returns the absolute filesystem path of the effective workspace root. */
    suspend fun getWorkspaceRootPath(): String

    /** Lists entries in the given workspace directory path. */
    suspend fun list(path: String = "/"): List<WorkspaceEntry>

    /** Creates a new directory at the given workspace path. */
    suspend fun createDirectory(path: String)

    /** Renames the entry at [path] within its current parent directory. */
    suspend fun rename(path: String, newName: String): WorkspaceEntry

    /** Moves the entry at [path] into [destinationDirectoryPath]. */
    suspend fun move(path: String, destinationDirectoryPath: String): WorkspaceEntry

    /** Deletes the file or directory at [path]. */
    suspend fun delete(path: String)

    /** Normalizes a user-entered Unix-style workspace path. */
    fun normalizePath(path: String): String

    /** Resolves a workspace path to an absolute local filesystem path. */
    suspend fun resolveLocalPath(path: String): String
}
