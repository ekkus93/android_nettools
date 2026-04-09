package dev.nettools.android.data.ssh

/**
 * Snapshot of transfer progress for a single file.
 *
 * @property fileName Name of the file being transferred.
 * @property bytesTransferred Number of bytes transferred so far.
 * @property totalBytes Total file size in bytes; -1 if unknown.
 * @property speedBytesPerSec Current transfer speed in bytes per second.
 * @property isResuming Whether the transfer is resuming a previous partial transfer.
 */
data class TransferProgress(
    val fileName: String,
    val bytesTransferred: Long,
    val totalBytes: Long,
    val speedBytesPerSec: Double,
    val isResuming: Boolean = false
)
