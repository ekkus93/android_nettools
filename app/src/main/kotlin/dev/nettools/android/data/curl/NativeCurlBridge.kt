package dev.nettools.android.data.curl

import dev.nettools.android.util.CurlUserMessageFormatter
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin JNI wrapper around metadata exposed by the bundled libcurl build.
 */
@Singleton
class NativeCurlBridge @Inject constructor() : CurlRuntimeMetadataProvider {

    override fun getRuntimeMetadata(): CurlRuntimeMetadataResult {
        return try {
            initializeGlobal()
            try {
                CurlRuntimeMetadataResult.Available(
                    metadata = CurlRuntimeMetadata(
                        bundledCurlVersion = nativeGetBundledCurlVersion(),
                        supportedProtocols = nativeGetSupportedProtocols().toList(),
                        supportedFeatures = nativeGetSupportedFeatures().toList(),
                        http2Supported = nativeIsHttp2Supported(),
                    ),
                )
            } finally {
                shutdownGlobal()
            }
        } catch (_: UnsatisfiedLinkError) {
            CurlRuntimeMetadataResult.Unavailable(CurlUserMessageFormatter.runtimeMetadataUnavailable())
        } catch (_: IllegalStateException) {
            CurlRuntimeMetadataResult.Unavailable(CurlUserMessageFormatter.runtimeMetadataUnavailable())
        }
    }

    companion object {
        private val libraryLoaded = AtomicBoolean(false)
        private val initialized = AtomicBoolean(false)

        /** Loads the native bridge and runs libcurl global initialization once. */
        fun initializeGlobal() {
            synchronized(this) {
                if (libraryLoaded.compareAndSet(false, true)) {
                    System.loadLibrary("curlbridge")
                }
                if (initialized.compareAndSet(false, true)) {
                    try {
                        nativeInitializeGlobal()
                    } catch (error: RuntimeException) {
                        initialized.set(false)
                        throw error
                    }
                }
            }
        }

        /** Releases libcurl global state after metadata has been read. */
        fun shutdownGlobal() {
            synchronized(this) {
                if (initialized.compareAndSet(true, false)) {
                    nativeCleanupGlobal()
                }
            }
        }

        @JvmStatic
        private external fun nativeInitializeGlobal()

        @JvmStatic
        private external fun nativeCleanupGlobal()
    }

    private external fun nativeGetBundledCurlVersion(): String
    private external fun nativeGetSupportedProtocols(): Array<String>
    private external fun nativeGetSupportedFeatures(): Array<String>
    private external fun nativeIsHttp2Supported(): Boolean
}
