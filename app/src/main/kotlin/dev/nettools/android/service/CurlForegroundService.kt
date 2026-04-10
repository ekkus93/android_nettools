package dev.nettools.android.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.nettools.android.data.curl.CurlExecutionRequest
import dev.nettools.android.data.curl.CurlExecutor
import dev.nettools.android.domain.model.CurlOutputStream
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.repository.CurlRunRepository
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
    }

    @Inject lateinit var curlRunHolder: CurlRunHolder
    @Inject lateinit var runRepository: CurlRunRepository
    @Inject lateinit var notificationHelper: NotificationHelper
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
        curlRunHolder.startRun(params.runId)
        runRepository.updateStatus(params.runId, status = CurlRunStatus.IN_PROGRESS)
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
                    parsedCommand = params.parsedCommand,
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
            )
            curlRunHolder.updateStatus(status = finalStatus, exitCode = result.exitCode)
        } catch (e: CancellationException) {
            runRepository.updateStatus(
                runId = params.runId,
                status = CurlRunStatus.CANCELLED,
                finishedAt = System.currentTimeMillis(),
            )
            curlRunHolder.updateStatus(status = CurlRunStatus.CANCELLED)
            throw e
        } catch (e: Exception) {
            runRepository.updateStatus(
                runId = params.runId,
                status = CurlRunStatus.FAILED,
                finishedAt = System.currentTimeMillis(),
            )
            curlRunHolder.appendOutput(isStdout = false, chunk = (e.message ?: "Curl execution failed"))
            curlRunHolder.updateStatus(status = CurlRunStatus.FAILED)
        } finally {
            activeRunJob = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
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
