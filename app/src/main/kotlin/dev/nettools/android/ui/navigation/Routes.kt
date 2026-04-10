package dev.nettools.android.ui.navigation

/**
 * Centralised navigation route constants for the NavHost.
 */
object Routes {
    const val HOME = "home"
    const val TRANSFER = "transfer"
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
}
