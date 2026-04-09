package dev.nettools.android.service;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;

/**
 * Helper for building transfer-related notifications.
 * Provides factory methods for progress, success, and failure notifications.
 *
 * @param context Application context.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0013\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J&\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\tJ\u001e\u0010\u000e\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\t2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\tJ\u001e\u0010\u0011\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\t2\u0006\u0010\u0012\u001a\u00020\t2\u0006\u0010\r\u001a\u00020\tJ\f\u0010\u0013\u001a\u00020\t*\u00020\u0010H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Ldev/nettools/android/service/NotificationHelper;", "", "context", "Landroid/content/Context;", "<init>", "(Landroid/content/Context;)V", "createProgressNotification", "Landroid/app/Notification;", "jobId", "", "fileName", "progress", "Ldev/nettools/android/data/ssh/TransferProgress;", "channelId", "createSuccessNotification", "sizeBytes", "", "createFailureNotification", "reason", "toFormattedSize", "app_debug"})
public final class NotificationHelper {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    
    @javax.inject.Inject()
    public NotificationHelper(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Builds a progress notification for an ongoing transfer.
     *
     * @param jobId Unique job identifier, used to tag a cancel action.
     * @param fileName Name of the file being transferred.
     * @param progress Current [dev.nettools.android.data.ssh.TransferProgress] snapshot.
     * @param channelId Notification channel ID.
     * @return A [Notification] suitable for a foreground service.
     */
    @org.jetbrains.annotations.NotNull()
    public final android.app.Notification createProgressNotification(@org.jetbrains.annotations.NotNull()
    java.lang.String jobId, @org.jetbrains.annotations.NotNull()
    java.lang.String fileName, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.ssh.TransferProgress progress, @org.jetbrains.annotations.NotNull()
    java.lang.String channelId) {
        return null;
    }
    
    /**
     * Builds a notification indicating a successful transfer.
     *
     * @param fileName Name of the transferred file.
     * @param sizeBytes Total size in bytes.
     * @param channelId Notification channel ID.
     * @return A [Notification] suitable for display after service completion.
     */
    @org.jetbrains.annotations.NotNull()
    public final android.app.Notification createSuccessNotification(@org.jetbrains.annotations.NotNull()
    java.lang.String fileName, long sizeBytes, @org.jetbrains.annotations.NotNull()
    java.lang.String channelId) {
        return null;
    }
    
    /**
     * Builds a notification indicating a failed transfer.
     *
     * @param fileName Name of the file that failed to transfer.
     * @param reason Human-readable error description.
     * @param channelId Notification channel ID.
     * @return A [Notification] suitable for display after service completion.
     */
    @org.jetbrains.annotations.NotNull()
    public final android.app.Notification createFailureNotification(@org.jetbrains.annotations.NotNull()
    java.lang.String fileName, @org.jetbrains.annotations.NotNull()
    java.lang.String reason, @org.jetbrains.annotations.NotNull()
    java.lang.String channelId) {
        return null;
    }
    
    private final java.lang.String toFormattedSize(long $this$toFormattedSize) {
        return null;
    }
}