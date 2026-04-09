package dev.nettools.android.domain.model

/**
 * An immutable record of a completed (or failed/cancelled) transfer,
 * written to the local Room database for history display.
 *
 * @property id Unique record identifier.
 * @property timestamp Unix epoch milliseconds when the transfer completed.
 * @property direction Whether this was an upload or download.
 * @property host Remote hostname or IP.
 * @property username Remote account username.
 * @property fileName Name of the transferred file (not full path).
 * @property remoteDir Remote directory path.
 * @property fileSizeBytes Total size of the transferred file in bytes.
 * @property status Outcome of the transfer.
 * @property errorMessage Human-readable failure reason; null for non-failed entries.
 */
data class TransferHistoryEntry(
    val id: String,
    val timestamp: Long,
    val direction: TransferDirection,
    val host: String,
    val username: String,
    val fileName: String,
    val remoteDir: String,
    val fileSizeBytes: Long,
    val status: HistoryStatus,
    val errorMessage: String? = null,
)

/** Final outcome status for a [TransferHistoryEntry]. */
enum class HistoryStatus {
    SUCCESS,
    FAILED,
    CANCELLED,
    RESUMED
}
