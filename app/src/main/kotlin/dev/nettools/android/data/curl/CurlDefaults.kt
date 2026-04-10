package dev.nettools.android.data.curl

/**
 * Centralized defaults and keys for the curl feature.
 */
object CurlDefaults {
    const val stdoutByteCap: Int = 4 * 1024 * 1024
    const val stderrByteCap: Int = 1024 * 1024
    const val defaultWorkspaceDirectoryName: String = "curl-workspace"

    const val settingLoggingEnabled: String = "curl.loggingEnabled"
    const val settingSaveHistoryEnabled: String = "curl.saveHistoryEnabled"
    const val settingWorkspaceRootPath: String = "curl.workspaceRootPath"
}
