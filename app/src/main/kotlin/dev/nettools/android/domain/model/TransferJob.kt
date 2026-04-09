package dev.nettools.android.domain.model

/**
 * Represents an in-progress or queued file transfer job.
 *
 * @property id Unique job identifier.
 * @property profileId ID of the [ConnectionProfile] to use.
 * @property direction Whether this is an upload or download.
 * @property localPath Absolute path on the Android device.
 * @property remotePath Absolute or relative path on the remote host.
 * @property status Current status of the job.
 * @property bytesTransferred Number of bytes transferred so far.
 * @property totalBytes Total file size in bytes; -1 if unknown.
 * @property errorMessage Human-readable error if the job failed.
 */
data class TransferJob(
    val id: String,
    val profileId: String,
    val direction: TransferDirection,
    val localPath: String,
    val remotePath: String,
    val status: TransferStatus = TransferStatus.QUEUED,
    val bytesTransferred: Long = 0L,
    val totalBytes: Long = -1L,
    val errorMessage: String? = null
)

/** Direction of a file transfer. */
enum class TransferDirection {
    UPLOAD,
    DOWNLOAD
}

/** Status lifecycle of a [TransferJob]. */
enum class TransferStatus {
    QUEUED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}
