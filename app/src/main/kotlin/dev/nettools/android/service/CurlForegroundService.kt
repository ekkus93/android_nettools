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
import dev.nettools.android.data.curl.CurlCommandWorkspaceAdapter
import dev.nettools.android.data.curl.CurlExecutionRequest
import dev.nettools.android.data.curl.CurlExecutor
import dev.nettools.android.domain.model.CurlOutputStream
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.repository.CurlRunRepository
import dev.nettools.android.util.CurlUserMessageFormatter
import kotlinx.coroutines.CancellationException
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
    @Inject lateinit var runRepository: CurlRunRepository
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var workspaceAdapter: CurlCommandWorkspaceAdapter
    @Inject lateinit var curlExecutor: CurlExecutor

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
        val preparedCommand = workspaceAdapter.prepareForExecution(params.parsedCommand)
        val effectiveCommandText = preparedCommand.effectiveCommandText
        curlRunHolder.startRun(
            runId = params.runId,
            commandText = params.rawCommandText,
            effectiveCommandText = effectiveCommandText,
        )
        runRepository.updateStatus(
            params.runId,
            status = CurlRunStatus.IN_PROGRESS,
            effectiveCommandText = effectiveCommandText,
        )
        startForeground(
            FOREGROUND_NOTIFICATION_ID,
            notificationHelper.createCurlProgressNotification(
                runId = params.runId,
                commandText = params.rawCommandText,
                statusText = "Running curl",
                channelId = CURL_CHANNEL_ID,
            ),
        )

        try {
            val result = curlExecutor.execute(
                request = CurlExecutionRequest(
                    runId = params.runId,
                    parsedCommand = preparedCommand.command,
                    workspaceDirectory = params.workspaceRootPath,
                ),
            ) { chunk ->
                val isStdout = chunk.stream == CurlOutputStream.STDOUT
                curlRunHolder.appendOutput(isStdout = isStdout, chunk = chunk.text)
                if (params.loggingEnabled) {
                    runRepository.appendOutput(
                        runId = params.runId,
                        stream = chunk.stream,
                        text = chunk.text,
                        byteCap = if (isStdout) params.stdoutByteCap else params.stderrByteCap,
                    )
                }
            }

            val finalStatus = if (result.exitCode == 0) CurlRunStatus.COMPLETED else CurlRunStatus.FAILED
            runRepository.updateStatus(
                runId = params.runId,
                status = finalStatus,
                finishedAt = System.currentTimeMillis(),
                exitCode = result.exitCode,
                durationMillis = result.durationMillis,
                effectiveCommandText = effectiveCommandText,
                cleanupStatus = dev.nettools.android.domain.model.CurlCleanupStatus.SKIPPED,
            )
            curlRunHolder.updateStatus(
                status = finalStatus,
                exitCode = result.exitCode,
                cleanupStatus = dev.nettools.android.domain.model.CurlCleanupStatus.SKIPPED,
            )
            val notification = if (finalStatus == CurlRunStatus.COMPLETED) {
                notificationHelper.createCurlCompletionNotification(
                    commandText = params.rawCommandText,
                    exitCode = result.exitCode,
                    channelId = CURL_CHANNEL_ID,
                )
            } else {
                notificationHelper.createCurlFailureNotification(
                    commandText = params.rawCommandText,
                    reason = "Exit code: ${result.exitCode}",
                    channelId = CURL_CHANNEL_ID,
                )
            }
            notifyCompletion(params.runId, notification)
        } catch (e: CancellationException) {
            val cancellationMessage = CurlUserMessageFormatter.executionCancelled()
            curlRunHolder.appendOutput(isStdout = false, chunk = cancellationMessage)
            if (params.loggingEnabled) {
                runRepository.appendOutput(
                    runId = params.runId,
                    stream = CurlOutputStream.STDERR,
                    text = cancellationMessage,
                    byteCap = params.stderrByteCap,
                )
            }
            val cleanupResult = workspaceAdapter.cleanupPartialOutputs(preparedCommand)
            runRepository.updateStatus(
                runId = params.runId,
                status = CurlRunStatus.CANCELLED,
                finishedAt = System.currentTimeMillis(),
                cleanupWarning = cleanupResult.warning,
                effectiveCommandText = effectiveCommandText,
                cleanupStatus = cleanupResult.status,
            )
            curlRunHolder.updateStatus(
                status = CurlRunStatus.CANCELLED,
                cleanupWarning = cleanupResult.warning,
                cleanupStatus = cleanupResult.status,
            )
            notifyCompletion(
                params.runId,
                notificationHelper.createCurlCancellationNotification(
                    commandText = params.rawCommandText,
                    channelId = CURL_CHANNEL_ID,
                ),
            )
            throw e
        } catch (e: Exception) {
            val failureReason = CurlUserMessageFormatter.executionFailure(e)
            val cleanupResult = workspaceAdapter.cleanupPartialOutputs(preparedCommand)
            runRepository.updateStatus(
                runId = params.runId,
                status = CurlRunStatus.FAILED,
                finishedAt = System.currentTimeMillis(),
                cleanupWarning = cleanupResult.warning,
                effectiveCommandText = effectiveCommandText,
                cleanupStatus = cleanupResult.status,
            )
            curlRunHolder.appendOutput(isStdout = false, chunk = failureReason)
            curlRunHolder.updateStatus(
                status = CurlRunStatus.FAILED,
                cleanupWarning = cleanupResult.warning,
                cleanupStatus = cleanupResult.status,
            )
            notifyCompletion(
                params.runId,
                notificationHelper.createCurlFailureNotification(
                    commandText = params.rawCommandText,
                    reason = failureReason,
                    channelId = CURL_CHANNEL_ID,
                ),
            )
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
