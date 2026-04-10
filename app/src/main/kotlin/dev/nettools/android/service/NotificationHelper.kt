package dev.nettools.android.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Helper for building transfer-related notifications.
 * Provides factory methods for progress, success, and failure notifications.
 *
 * @param context Application context.
 */
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /**
     * Builds a progress notification for an ongoing transfer.
     *
     * @param jobId Unique job identifier, used to tag a cancel action.
     * @param fileName Name of the file being transferred.
     * @param progress Current [dev.nettools.android.data.ssh.TransferProgress] snapshot.
     * @param channelId Notification channel ID.
     * @return A [Notification] suitable for a foreground service.
     */
    fun createProgressNotification(
        jobId: String,
        fileName: String,
        progress: dev.nettools.android.data.ssh.TransferProgress,
        channelId: String
    ): Notification {
        val percent = if (progress.totalBytes > 0) {
            ((progress.bytesTransferred.toDouble() / progress.totalBytes) * 100).toInt()
        } else -1

        val cancelIntent = Intent(context, TransferForegroundService::class.java).apply {
            action = TransferForegroundService.ACTION_CANCEL
            putExtra(TransferForegroundService.EXTRA_JOB_ID, jobId)
        }
        val cancelPi = android.app.PendingIntent.getService(
            context, jobId.hashCode(), cancelIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            if (progress.isResuming && progress.resumeOffsetBytes > 0L) {
                append("Resuming from ")
                append(progress.resumeOffsetBytes.toFormattedSize())
                append(" · ")
            }
            append(progress.bytesTransferred.toFormattedSize())
            append(" / ")
            append(progress.totalBytes.toFormattedSize())
        }

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Transferring $fileName")
            .setContentText(contentText)
            .setProgress(100, percent.coerceAtLeast(0), percent < 0)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPi)
            .build()
    }

    /** Builds a generic foreground notification shown while the queue is being restored. */
    fun createQueueNotification(channelId: String): Notification =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Preparing transfer queue")
            .setContentText("Restoring queued transfers…")
            .setOngoing(true)
            .build()

    /**
     * Builds a progress notification for an active curl run.
     *
     * @param runId Unique curl run identifier.
     * @param commandText Command currently being executed.
     * @param statusText Short status message.
     * @param channelId Notification channel ID.
     * @return A [Notification] suitable for a foreground curl service.
     */
    fun createCurlProgressNotification(
        runId: String,
        commandText: String,
        statusText: String,
        channelId: String,
    ): Notification {
        val cancelIntent = Intent(context, CurlForegroundService::class.java).apply {
            action = CurlForegroundService.ACTION_CANCEL
            putExtra(CurlForegroundService.EXTRA_RUN_ID, runId)
        }
        val cancelPi = android.app.PendingIntent.getService(
            context,
            runId.hashCode(),
            cancelIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Running curl")
            .setContentText(statusText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(commandText))
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPi)
            .build()
    }

    /**
     * Builds a completion notification for a curl run.
     */
    fun createCurlCompletionNotification(
        commandText: String,
        exitCode: Int,
        channelId: String,
    ): Notification =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Curl run complete")
            .setContentText("Exit code: $exitCode")
            .setStyle(NotificationCompat.BigTextStyle().bigText(commandText))
            .setAutoCancel(true)
            .build()

    /**
     * Builds a failure notification for a curl run.
     */
    fun createCurlFailureNotification(
        commandText: String,
        reason: String,
        channelId: String,
    ): Notification =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Curl run failed")
            .setContentText(reason)
            .setStyle(NotificationCompat.BigTextStyle().bigText(commandText))
            .setAutoCancel(true)
            .build()

    /**
     * Builds a cancellation notification for a curl run.
     */
    fun createCurlCancellationNotification(
        commandText: String,
        channelId: String,
    ): Notification =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Curl run cancelled")
            .setContentText("The command was cancelled.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(commandText))
            .setAutoCancel(true)
            .build()

    /**
     * Builds a notification indicating a successful transfer.
     *
     * @param fileName Name of the transferred file.
     * @param sizeBytes Total size in bytes.
     * @param channelId Notification channel ID.
     * @return A [Notification] suitable for display after service completion.
     */
    fun createSuccessNotification(
        fileName: String,
        sizeBytes: Long,
        channelId: String
    ): Notification =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("Transfer complete")
            .setContentText("$fileName (${sizeBytes.toFormattedSize()})")
            .setAutoCancel(true)
            .build()

    /**
     * Builds a notification indicating a failed transfer.
     *
     * @param fileName Name of the file that failed to transfer.
     * @param reason Human-readable error description.
     * @param channelId Notification channel ID.
     * @return A [Notification] suitable for display after service completion.
     */
    fun createFailureNotification(
        fileName: String,
        reason: String,
        channelId: String
    ): Notification =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Transfer failed: $fileName")
            .setContentText(reason)
            .setAutoCancel(true)
            .build()

    private fun Long.toFormattedSize(): String = when {
        this >= 1_073_741_824L -> "%.1f GB".format(this / 1_073_741_824.0)
        this >= 1_048_576L -> "%.1f MB".format(this / 1_048_576.0)
        this >= 1_024L -> "%.1f KB".format(this / 1_024.0)
        else -> "$this B"
    }
}
