package dev.nettools.android.service

import dev.nettools.android.domain.model.TransferStatus
import net.schmizz.sshj.xfer.InMemorySourceFile
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

/** Metadata describing where a download is written before any finalization step. */
internal data class DownloadTarget(
    val workingFile: File,
    val safDestinationUri: String? = null,
) {
    val isSafBacked: Boolean
        get() = safDestinationUri != null
}

/** Simple [net.schmizz.sshj.xfer.LocalSourceFile] backed by an input-stream factory. */
internal class StreamSourceFile(
    private val sourceName: String,
    private val sourceLength: Long,
    private val openStream: () -> InputStream,
) : InMemorySourceFile() {

    override fun getName(): String = sourceName

    override fun getLength(): Long = sourceLength

    override fun getInputStream(): InputStream = openStream()
}

/** Serially drains a queue and processes each item one at a time. */
internal suspend fun <T> drainQueueSequentially(
    dequeue: () -> T?,
    shouldProcess: (T) -> Boolean = { true },
    process: suspend (T) -> Unit,
) {
    while (true) {
        val item = dequeue() ?: break
        if (shouldProcess(item)) {
            process(item)
        }
    }
}

/** Builds a stable cache file path used to resume SAF downloads across retries. */
internal fun buildStableSafTempFile(
    cacheDir: File,
    destinationUri: String,
    remotePath: String,
    remoteFileName: String,
): File {
    val digest = MessageDigest.getInstance("SHA-256")
        .digest("$destinationUri|$remotePath".toByteArray())
        .joinToString("") { "%02x".format(it) }
        .take(24)
    val safeName = remoteFileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
    return File(cacheDir, "download_resume_${digest}_$safeName.part")
}

/** Prepares an existing partial file for resume and restarts from zero if it is stale. */
internal fun prepareResumeOffset(
    workingFile: File,
    remoteSizeBytes: Long?,
): Long {
    if (!workingFile.exists()) return 0L
    val localSize = workingFile.length()
    if (remoteSizeBytes != null && localSize > remoteSizeBytes) {
        workingFile.delete()
        return 0L
    }
    return localSize
}

/** Decides whether a download working file should be deleted after transfer finalization. */
internal fun shouldDeleteWorkingFile(
    target: DownloadTarget?,
    finalStatus: TransferStatus,
    safFinalizationAttempted: Boolean,
    safFinalizationSucceeded: Boolean,
): Boolean {
    if (target == null || !target.isSafBacked) return false
    return when {
        finalStatus == TransferStatus.COMPLETED && safFinalizationSucceeded -> true
        safFinalizationAttempted && !safFinalizationSucceeded -> true
        else -> false
    }
}
