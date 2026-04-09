package dev.nettools.android.data.ssh

import dev.nettools.android.domain.model.RemoteFileEntry
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.FileAttributes
import net.schmizz.sshj.sftp.FileMode
import net.schmizz.sshj.sftp.SFTPClient
import javax.inject.Inject

/**
 * Provides SFTP directory and file management operations using SSHJ.
 * Every method opens a dedicated SFTP client and closes it via [use].
 */
class SftpClient @Inject constructor() {

    /**
     * Lists the contents of a remote directory.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param path Absolute remote directory path.
     * @return List of [RemoteFileEntry] objects.
     */
    suspend fun listDirectory(sshClient: SSHClient, path: String): List<RemoteFileEntry> =
        sshClient.newSFTPClient().use { sftp ->
            sftp.ls(path).map { entry ->
                RemoteFileEntry(
                    name = entry.name,
                    path = "$path/${entry.name}",
                    sizeBytes = entry.attributes.size,
                    permissions = entry.attributes.permissions?.toString() ?: "",
                    isDirectory = entry.attributes.type == FileMode.Type.DIRECTORY,
                    modifiedAt = entry.attributes.mtime
                )
            }
        }

    /**
     * Returns metadata for a single remote file or directory, or null if not found.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param path Absolute remote path.
     * @return [RemoteFileEntry] or null.
     */
    suspend fun stat(sshClient: SSHClient, path: String): RemoteFileEntry? =
        runCatching {
            sshClient.newSFTPClient().use { sftp ->
                val attrs: FileAttributes = sftp.stat(path)
                RemoteFileEntry(
                    name = path.substringAfterLast('/'),
                    path = path,
                    sizeBytes = attrs.size,
                    permissions = attrs.permissions?.toString() ?: "",
                    isDirectory = attrs.type == FileMode.Type.DIRECTORY,
                    modifiedAt = attrs.mtime
                )
            }
        }.getOrNull()

    /**
     * Creates a directory on the remote host.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param path Absolute remote path of the directory to create.
     */
    suspend fun mkdir(sshClient: SSHClient, path: String) {
        sshClient.newSFTPClient().use { sftp ->
            sftp.mkdir(path)
        }
    }

    /**
     * Renames (moves) a remote file or directory.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param fromPath Source path.
     * @param toPath Destination path.
     */
    suspend fun rename(sshClient: SSHClient, fromPath: String, toPath: String) {
        sshClient.newSFTPClient().use { sftp ->
            sftp.rename(fromPath, toPath)
        }
    }

    /**
     * Deletes a remote file or directory recursively.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param path Absolute remote path to delete.
     */
    suspend fun delete(sshClient: SSHClient, path: String) {
        sshClient.newSFTPClient().use { sftp ->
            deleteRecursively(sftp, path)
        }
    }

    /**
     * Returns the size of a remote file in bytes, or null if the file does not exist.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param path Absolute remote path.
     * @return File size in bytes, or null.
     */
    suspend fun getFileSize(sshClient: SSHClient, path: String): Long? =
        runCatching {
            sshClient.newSFTPClient().use { sftp ->
                sftp.stat(path).size
            }
        }.getOrNull()

    private fun deleteRecursively(sftp: SFTPClient, path: String) {
        when (sftp.type(path)) {
            FileMode.Type.DIRECTORY -> {
                sftp.ls(path)
                    .filter { it.name != "." && it.name != ".." }
                    .forEach { child ->
                        deleteRecursively(sftp, joinRemotePath(path, child.name))
                    }
                sftp.rmdir(path)
            }
            else -> sftp.rm(path)
        }
    }

    private fun joinRemotePath(parent: String, child: String): String =
        if (parent == "/") "/$child" else "$parent/$child"
}
