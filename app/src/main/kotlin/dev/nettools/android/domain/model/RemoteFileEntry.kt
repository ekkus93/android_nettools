package dev.nettools.android.domain.model

/**
 * Represents a file or directory entry on the remote SFTP filesystem.
 *
 * @property name File or directory name (not full path).
 * @property path Absolute remote path.
 * @property sizeBytes Size in bytes; 0 for directories.
 * @property permissions Unix permission string (e.g. "rwxr-xr-x").
 * @property isDirectory Whether this entry is a directory.
 * @property modifiedAt Unix epoch seconds of last modification.
 */
data class RemoteFileEntry(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val permissions: String,
    val isDirectory: Boolean,
    val modifiedAt: Long
)
