package dev.nettools.android.ui.navigation

/**
 * Centralised navigation route constants for the NavHost.
 */
object Routes {
    const val HOME = "home"
    const val TRANSFER = "transfer"
    const val SFTP_BROWSER = "sftp_browser"
    const val SAVED_CONNECTIONS = "saved_connections"
    const val HISTORY = "history"
    const val PROGRESS = "progress/{jobId}"

    /** Builds the progress route for a specific [jobId]. */
    fun progress(jobId: String) = "progress/$jobId"
}
