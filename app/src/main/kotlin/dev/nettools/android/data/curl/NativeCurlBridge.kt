package dev.nettools.android.data.curl

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin JNI wrapper for bundled native curl metadata.
 *
 * This starts as a scaffold so native packaging is wired before full execution
 * support lands.
 */
@Singleton
class NativeCurlBridge @Inject constructor() {

    init {
        System.loadLibrary("curlbridge")
    }

    /** Returns the bundled native curl bridge version string. */
    fun getBundledCurlVersion(): String = nativeGetBundledCurlVersion()

    /** Returns the protocol list reported by the native bridge. */
    fun getSupportedProtocols(): List<String> = nativeGetSupportedProtocols().toList()

    private external fun nativeGetBundledCurlVersion(): String
    private external fun nativeGetSupportedProtocols(): Array<String>
}
