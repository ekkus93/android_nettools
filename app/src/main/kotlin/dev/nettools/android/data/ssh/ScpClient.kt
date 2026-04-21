package dev.nettools.android.data.ssh

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.StreamCopier
import net.schmizz.sshj.xfer.FileSystemFile
import net.schmizz.sshj.xfer.LocalSourceFile
import net.schmizz.sshj.xfer.TransferListener
import java.io.File
import javax.inject.Inject

/**
 * Provides streaming SCP/SFTP file transfers using SSHJ.
 * Every method returns a cold [Flow] of [TransferProgress] snapshots and fully supports
 * coroutine cancellation. Progress is reported via SSHJ's [TransferListener] callback.
 */
class ScpClient @Inject constructor() {

    /**
     * Uploads [localFile] to [remotePath] on the remote host.
     * The file is first written to a `<remotePath>.part` temporary path and then
     * atomically renamed to [remotePath] on success, ensuring no partial file is left
     * visible on an interrupted transfer.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param localFile Local source file.
     * @param remotePath Destination path on the remote host (without `.part` suffix).
     * @return Flow of [TransferProgress] updates.
     */
    fun upload(
        sshClient: SSHClient,
        localFile: File,
        remotePath: String,
    ): Flow<TransferProgress> = upload(sshClient, FileSystemFile(localFile), remotePath)

    /**
     * Uploads [localSource] to [remotePath] on the remote host.
     */
    fun upload(
        sshClient: SSHClient,
        localSource: LocalSourceFile,
        remotePath: String,
    ): Flow<TransferProgress> = transferFlow(localSource.name, localSource.length) { listener ->
        val partPath = "$remotePath.part"
        val scp = sshClient.newSCPFileTransfer()
        scp.transferListener = listener
        scp.upload(localSource, partPath)
        sshClient.newSFTPClient().use { sftp -> sftp.rename(partPath, remotePath) }
    }

    /**
     * Downloads [remotePath] from the remote host to [localFile].
     * For a resumable download see [downloadResumable].
     *
     * @param sshClient An authenticated [SSHClient].
     * @param remotePath Source path on the remote host.
     * @param localFile Local destination file. Parent directories are created if absent.
     * @return Flow of [TransferProgress] updates.
     */
    fun download(
        sshClient: SSHClient,
        remotePath: String,
        localFile: File,
    ): Flow<TransferProgress> = downloadViaSftp(
        sshClient = sshClient,
        remotePath = remotePath,
        localFile = localFile,
        resumeOffset = 0L,
    )

    /**
     * Downloads [remotePath] from the remote host with byte-offset resume support.
     * Uses SFTP so that the transfer can start at [resumeOffset] bytes into the file,
     * appending to an existing partial [localFile].
     *
     * @param sshClient An authenticated [SSHClient].
     * @param remotePath Source path on the remote host.
     * @param localFile Local destination file. Must already contain [resumeOffset] bytes.
     * @param resumeOffset Number of bytes already present locally; transfer begins here.
     * @return Flow of [TransferProgress] updates.
     */
    fun downloadResumable(
        sshClient: SSHClient,
        remotePath: String,
        localFile: File,
        resumeOffset: Long,
    ): Flow<TransferProgress> = downloadViaSftp(
        sshClient = sshClient,
        remotePath = remotePath,
        localFile = localFile,
        resumeOffset = resumeOffset,
    )

    private fun downloadViaSftp(
        sshClient: SSHClient,
        remotePath: String,
        localFile: File,
        resumeOffset: Long,
    ): Flow<TransferProgress> = callbackFlow {
        val fileName = remotePath.substringAfterLast('/')
        val startTime = System.currentTimeMillis()

        val ioJob = launch(Dispatchers.IO) {
            fun emitProgress(bytesTransferred: Long, totalBytes: Long) {
                val elapsedSec = (System.currentTimeMillis() - startTime).coerceAtLeast(1L) / 1000.0
                val speed = (bytesTransferred - resumeOffset).coerceAtLeast(0L) / elapsedSec
                trySend(
                    TransferProgress(
                        fileName = fileName,
                        bytesTransferred = bytesTransferred,
                        totalBytes = totalBytes,
                        speedBytesPerSec = speed,
                        isResuming = resumeOffset > 0L,
                        resumeOffsetBytes = resumeOffset,
                    )
                )
            }

            try {
                localFile.parentFile?.mkdirs()
                runInterruptible {
                    sshClient.newSFTPClient().use { sftp ->
                        val remoteFile = sftp.open(remotePath)
                        try {
                            val totalSize = remoteFile.fetchAttributes().size
                            java.io.RandomAccessFile(localFile, "rw").use { raf ->
                                raf.seek(resumeOffset)
                                emitProgress(resumeOffset, totalSize)
                                val buf = ByteArray(DEFAULT_BUFFER_SIZE)
                                var offset = resumeOffset
                                while (offset < totalSize && isActive) {
                                    val read = remoteFile.read(offset, buf, 0, buf.size)
                                    if (read <= 0) break
                                    raf.write(buf, 0, read)
                                    offset += read
                                    emitProgress(offset, totalSize)
                                }
                            }
                        } finally {
                            remoteFile.close()
                        }
                    }
                }
            } catch (e: Exception) {
                close(e)
                return@launch
            }
            withContext(NonCancellable) { close() }
        }

        awaitClose {
            if (isActive) ioJob.cancel()
        }
    }

    /**
     * Recursively uploads [localDir] to [remotePath] on the remote host.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param localDir Local source directory.
     * @param remotePath Destination directory path on the remote host.
     * @return Flow of [TransferProgress] updates.
     */
    fun uploadDirectory(
        sshClient: SSHClient,
        localDir: File,
        remotePath: String,
    ): Flow<TransferProgress> = transferFlow(localDir.name, localDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }) { listener ->
        val scp = sshClient.newSCPFileTransfer()
        scp.transferListener = listener
        scp.upload(FileSystemFile(localDir), remotePath)
    }

    /**
     * Recursively downloads [remotePath] (a remote directory) to [localDir].
     *
     * @param sshClient An authenticated [SSHClient].
     * @param remotePath Source directory path on the remote host.
     * @param localDir Local destination directory.
     * @return Flow of [TransferProgress] updates.
     */
    fun downloadDirectory(
        sshClient: SSHClient,
        remotePath: String,
        localDir: File,
    ): Flow<TransferProgress> = transferFlow(remotePath.substringAfterLast('/'), -1L) { listener ->
        localDir.mkdirs()
        val scp = sshClient.newSCPFileTransfer()
        scp.transferListener = listener
        scp.download(remotePath, FileSystemFile(localDir))
    }

    /**
     * Builds a [callbackFlow] that wires a [TransferListener] into [block] and emits
     * [TransferProgress] snapshots as bytes are transferred. The blocking [block] runs
     * on [Dispatchers.IO] so the calling coroutine is not blocked.
     *
     * Speed is reported as a cumulative bytes-per-second average over the elapsed time
     * since the transfer started.
     *
     * @param fileName Display name used for progress snapshots.
     * @param totalBytes Known total size, or -1 if unknown at call time.
     * @param block Synchronous SCP/SFTP operation that accepts the wired listener.
     */
    private fun transferFlow(
        fileName: String,
        totalBytes: Long,
        block: (TransferListener) -> Unit,
    ): Flow<TransferProgress> = callbackFlow {
        val startTime = System.currentTimeMillis()

        val listener = object : TransferListener {
            override fun directory(name: String): TransferListener = this

            override fun file(name: String, size: Long): StreamCopier.Listener {
                val effectiveTotal = if (totalBytes >= 0) totalBytes else size
                return StreamCopier.Listener { transferred ->
                    val elapsedSec = (System.currentTimeMillis() - startTime).coerceAtLeast(1L) / 1000.0
                    val speed = transferred / elapsedSec
                    trySend(
                        TransferProgress(
                            fileName = name.ifBlank { fileName },
                            bytesTransferred = transferred,
                            totalBytes = effectiveTotal,
                            speedBytesPerSec = speed,
                        )
                    )
                }
            }
        }

        val ioJob = launch(Dispatchers.IO) {
            try {
                runInterruptible { block(listener) }
            } catch (e: Exception) {
                close(e)
                return@launch
            }
            withContext(NonCancellable) { close() }
        }

        awaitClose {
            if (isActive) ioJob.cancel()
        }
    }
}
