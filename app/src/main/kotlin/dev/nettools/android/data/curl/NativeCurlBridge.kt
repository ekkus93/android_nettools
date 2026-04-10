package dev.nettools.android.data.curl

import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin JNI wrapper around metadata exposed by the bundled libcurl build.
 */
@Singleton
class NativeCurlBridge @Inject constructor() {

    init {
        initializeGlobal()
    }

    /** Returns the bundled native curl bridge version string. */
    fun getBundledCurlVersion(): String = nativeGetBundledCurlVersion()

    /** Returns the protocol list reported by the native bridge. */
    fun getSupportedProtocols(): List<String> = nativeGetSupportedProtocols().toList()

    /** Returns the feature list reported by the native bridge. */
    fun getSupportedFeatures(): List<String> = nativeGetSupportedFeatures().toList()

    /** True when the bundled runtime reports HTTP/2 support. */
    fun isHttp2Supported(): Boolean = nativeIsHttp2Supported()

    companion object {
        private val initialized = AtomicBoolean(false)

        /** Loads the native bridge and runs libcurl global initialization once. */
        fun initializeGlobal() {
            if (initialized.compareAndSet(false, true)) {
                System.loadLibrary("curlbridge")
                nativeInitializeGlobal()
            }
        }

        @JvmStatic
        private external fun nativeInitializeGlobal()
    }

    private external fun nativeGetBundledCurlVersion(): String
    private external fun nativeGetSupportedProtocols(): Array<String>
    private external fun nativeGetSupportedFeatures(): Array<String>
    private external fun nativeIsHttp2Supported(): Boolean
}
