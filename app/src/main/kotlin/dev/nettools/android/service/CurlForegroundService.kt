package dev.nettools.android.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.nettools.android.domain.model.CurlRunStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that executes a single curl run at a time.
 */
@AndroidEntryPoint
class CurlForegroundService : LifecycleService() {

    companion object {
        const val CURL_CHANNEL_ID = "curl_channel"
        const val ACTION_CANCEL = "dev.nettools.android.CANCEL_CURL_RUN"
        const val EXTRA_RUN_ID = "run_id"
        private const val FOREGROUND_NOTIFICATION_ID = 2001
        private const val TAG = "CurlFgService"
    }

    @Inject lateinit var curlRunHolder: CurlRunHolder
    @Inject lateinit var runExecutionCoordinator: CurlRunExecutionCoordinator
    @Inject lateinit var notificationHelper: NotificationHelper

    private var activeRunJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_CANCEL -> {
                val runId = intent.getStringExtra(EXTRA_RUN_ID)
                if (runId != null) {
                    curlRunHolder.requestCancel(runId)
                    activeRunJob?.cancel()
                }
            }

            else -> {
                if (activeRunJob?.isActive != true) {
                    curlRunHolder.consumePendingRun()?.let { params ->
                        activeRunJob = lifecycleScope.launch {
                            executeRun(params)
                        }
                    } ?: stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun executeRun(params: PendingCurlRunParams) {
        try {
            val outcome = runExecutionCoordinator.execute(params) {
                startForeground(
                    FOREGROUND_NOTIFICATION_ID,
                    notificationHelper.createCurlProgressNotification(
                        runId = params.runId,
                        commandText = params.rawCommandText,
                        statusText = "Running curl",
                        channelId = CURL_CHANNEL_ID,
                    ),
                )
            }
            val notification = when (outcome.status) {
                CurlRunStatus.COMPLETED -> notificationHelper.createCurlCompletionNotification(
                    commandText = params.rawCommandText,
                    exitCode = requireNotNull(outcome.exitCode),
                    channelId = CURL_CHANNEL_ID,
                )
                CurlRunStatus.CANCELLED -> notificationHelper.createCurlCancellationNotification(
                    commandText = params.rawCommandText,
                    channelId = CURL_CHANNEL_ID,
                )
                CurlRunStatus.FAILED -> notificationHelper.createCurlFailureNotification(
                    commandText = params.rawCommandText,
                    reason = requireNotNull(outcome.failureReason),
                    channelId = CURL_CHANNEL_ID,
                )
                else -> error("Unexpected terminal curl status: ${outcome.status}")
            }
            notifyCompletion(params.runId, notification)
        } finally {
            activeRunJob = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun notifyCompletion(runId: String, notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Skipping curl completion notification because POST_NOTIFICATIONS is not granted")
            return
        }
        try {
            NotificationManagerCompat.from(this).notify(runId.hashCode(), notification)
        } catch (e: SecurityException) {
            Log.d(TAG, "Unable to post curl completion notification", e)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CURL_CHANNEL_ID,
            "Curl Runs",
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
