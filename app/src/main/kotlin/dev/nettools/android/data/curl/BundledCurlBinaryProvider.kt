package dev.nettools.android.data.curl

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts and exposes the bundled per-ABI curl runtime shipped with the app.
 */
@Singleton
class BundledCurlBinaryProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CurlBinaryProvider {

    override suspend fun getRuntime(): CurlRuntime {
        val abi = currentSupportedAbis().firstOrNull()
            ?: error("No supported device ABI was reported by Android.")
        val executable = File(context.applicationInfo.nativeLibraryDir, "libcurl_exec.so")
        check(executable.exists() && executable.canExecute()) {
            "No bundled curl runtime is available for this device ABI."
        }
        val cacert = extractAsset(
            assetPath = "curl/cacert.pem",
            targetFile = File(runtimeDirectory(abi), "cacert.pem"),
            executable = false,
        )
        return CurlRuntime(
            executablePath = executable.absolutePath,
            caCertificatePath = cacert.absolutePath,
        )
    }

    private fun runtimeDirectory(abi: String): File = File(context.filesDir, "curl-runtime/$abi").apply {
        mkdirs()
    }

    private fun extractAsset(
        assetPath: String,
        targetFile: File,
        executable: Boolean,
    ): File {
        return installFileAtomically(
            targetFile = targetFile,
            executable = executable,
        ) { output ->
            context.assets.open(assetPath).use { input ->
                input.copyTo(output)
            }
        }
    }
}

internal fun currentSupportedAbis(): Array<String> = Build.SUPPORTED_ABIS
