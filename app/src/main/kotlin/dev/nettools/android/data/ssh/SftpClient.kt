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
     * Resolves [path] to a canonical remote path.
     *
     * `~` and `~/...` are expanded relative to the authenticated user's home directory because
     * SFTP paths are not shell-expanded by the server.
     */
    suspend fun resolvePath(sshClient: SSHClient, path: String): String =
        sshClient.newSFTPClient().use { sftp ->
            resolvePathInternal(sftp, path)
        }

    /**
     * Lists the contents of a remote directory.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param path Absolute remote directory path.
     * @return List of [RemoteFileEntry] objects.
     */
    suspend fun listDirectory(sshClient: SSHClient, path: String): List<RemoteFileEntry> =
        sshClient.newSFTPClient().use { sftp ->
            val resolvedPath = resolvePathInternal(sftp, path)
            sftp.ls(resolvedPath)
                .filter { it.name != "." && it.name != ".." }
                .map { entry ->
                RemoteFileEntry(
                    name = entry.name,
                    path = joinRemotePath(resolvedPath, entry.name),
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
                val resolvedPath = resolvePathInternal(sftp, path)
                val attrs: FileAttributes = sftp.stat(resolvedPath)
                RemoteFileEntry(
                    name = resolvedPath.substringAfterLast('/'),
                    path = resolvedPath,
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
            sftp.mkdir(resolvePathInternal(sftp, path))
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
            sftp.rename(resolvePathInternal(sftp, fromPath), resolvePathInternal(sftp, toPath))
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
            deleteRecursively(sftp, resolvePathInternal(sftp, path))
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
                sftp.stat(resolvePathInternal(sftp, path)).size
            }
        }.getOrNull()

    /**
     * Resolves the effective upload destination path.
     *
     * If [remotePath] names an existing directory, [fileName] is appended so uploads behave like
     * a file picker targeting that folder. Otherwise [remotePath] is treated as the full file path.
     */
    suspend fun resolveUploadDestination(
        sshClient: SSHClient,
        remotePath: String,
        fileName: String,
    ): String = sshClient.newSFTPClient().use { sftp ->
        val resolvedPath = resolvePathInternal(sftp, remotePath)
        if (resolvedPath.endsWith("/")) {
            return@use joinRemotePath(resolvedPath.removeSuffix("/").ifBlank { "/" }, fileName)
        }

        val remoteType = runCatching { sftp.type(resolvedPath) }.getOrNull()
        if (remoteType == FileMode.Type.DIRECTORY) {
            joinRemotePath(resolvedPath, fileName)
        } else {
            resolvedPath
        }
    }

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

    private fun resolvePathInternal(sftp: SFTPClient, path: String): String {
        val trimmed = path.trim()
        val homeDirectory = sftp.canonicalize(".")
        return when {
            trimmed.isBlank() || trimmed == "~" -> homeDirectory
            trimmed == "." -> homeDirectory
            trimmed.startsWith("~/") -> joinRemotePath(homeDirectory, trimmed.removePrefix("~/"))
            else -> runCatching { sftp.canonicalize(trimmed) }.getOrElse { trimmed }
        }
    }

    private fun joinRemotePath(parent: String, child: String): String =
        if (parent == "/") "/$child" else "${parent.removeSuffix("/")}/$child"
}
