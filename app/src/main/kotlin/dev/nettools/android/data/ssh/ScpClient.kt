package dev.nettools.android.data.ssh

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.xfer.FileSystemFile
import java.io.File
import javax.inject.Inject

/**
 * Provides streaming SCP file transfers using SSHJ's built-in SCP support.
 * All methods return a [Flow] of [TransferProgress] and support coroutine cancellation.
 */
class ScpClient @Inject constructor() {

    /**
     * Uploads a single local file to the remote host via SCP.
     * Uses a `.part` suffix during upload; renames to [remotePath] on completion.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param localFile Local file to upload.
     * @param remotePath Destination path on the remote host (without `.part` suffix).
     * @return Flow of [TransferProgress] updates.
     */
    fun upload(
        sshClient: SSHClient,
        localFile: File,
        remotePath: String
    ): Flow<TransferProgress> = flow {
        val partPath = "$remotePath.part"
        val scp = sshClient.newSCPFileTransfer()

        // Upload to .part path first, then atomically rename to final path
        scp.upload(FileSystemFile(localFile), partPath)
        sshClient.newSFTPClient().use { sftp ->
            sftp.rename(partPath, remotePath)
        }

        emit(
            TransferProgress(
                fileName = localFile.name,
                bytesTransferred = localFile.length(),
                totalBytes = localFile.length(),
                speedBytesPerSec = 0.0
            )
        )
    }

    /**
     * Downloads a remote file to a local destination via SCP.
     * Checks existing local file size to support resume detection.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param remotePath Source path on the remote host.
     * @param localFile Local destination file.
     * @return Flow of [TransferProgress] updates.
     */
    fun download(
        sshClient: SSHClient,
        remotePath: String,
        localFile: File
    ): Flow<TransferProgress> = flow {
        val isResuming = localFile.exists() && localFile.length() > 0L
        localFile.parentFile?.mkdirs()

        val scp = sshClient.newSCPFileTransfer()
        scp.download(remotePath, FileSystemFile(localFile))

        emit(
            TransferProgress(
                fileName = localFile.name,
                bytesTransferred = localFile.length(),
                totalBytes = localFile.length(),
                speedBytesPerSec = 0.0,
                isResuming = isResuming
            )
        )
    }

    /**
     * Recursively uploads a local directory to the remote host via SCP.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param localDir Local directory to upload.
     * @param remotePath Destination directory path on the remote host.
     * @return Flow of [TransferProgress] updates.
     */
    fun uploadDirectory(
        sshClient: SSHClient,
        localDir: File,
        remotePath: String
    ): Flow<TransferProgress> = flow {
        val scp = sshClient.newSCPFileTransfer()
        scp.upload(FileSystemFile(localDir), remotePath)
    }

    /**
     * Recursively downloads a remote directory to the local filesystem via SCP.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param remotePath Source directory path on the remote host.
     * @param localDir Local destination directory.
     * @return Flow of [TransferProgress] updates.
     */
    fun downloadDirectory(
        sshClient: SSHClient,
        remotePath: String,
        localDir: File
    ): Flow<TransferProgress> = flow {
        localDir.mkdirs()
        val scp = sshClient.newSCPFileTransfer()
        scp.download(remotePath, FileSystemFile(localDir))
    }
}
