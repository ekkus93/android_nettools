package dev.nettools.android.ui.navigation

import android.net.Uri
import dev.nettools.android.domain.model.TransferDirection

/**
 * Centralised navigation route constants for the NavHost.
 */
object Routes {
    const val HOME = "home"
    const val TRANSFER = "transfer"
    const val TRANSFER_PATTERN = "transfer?host={host}&remoteDir={remoteDir}&fileName={fileName}&direction={direction}"
    const val CURL = "curl"
    const val CURL_LOGS = "curl_logs"
    const val CURL_SETTINGS = "curl_settings"
    const val CURL_WORKSPACE = "curl_workspace"
    const val CURL_RESULTS = "curl_results/{runId}"
    const val SFTP_BROWSER = "sftp_browser"
    const val SAVED_CONNECTIONS = "saved_connections"
    const val HISTORY = "history"
    const val PROGRESS = "progress/{jobId}"

    /** Builds the progress route for a specific [jobId]. */
    fun progress(jobId: String) = "progress/$jobId"

    /** Builds the curl results route for a specific [runId]. */
    fun curlResults(runId: String) = "curl_results/$runId"

    /**
     * Builds a Transfer route pre-filled with data from a history entry.
     *
     * @param host Remote hostname.
     * @param remoteDir Remote directory path.
     * @param fileName File name (without directory).
     * @param direction Transfer direction to pre-select.
     */
    fun transferPrefill(
        host: String,
        remoteDir: String,
        fileName: String,
        direction: TransferDirection,
    ): String = "transfer?host=${Uri.encode(host)}&remoteDir=${Uri.encode(remoteDir)}" +
        "&fileName=${Uri.encode(fileName)}&direction=${direction.name}"
}
