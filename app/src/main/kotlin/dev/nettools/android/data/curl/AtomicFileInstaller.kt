package dev.nettools.android.data.curl

import java.io.File
import java.io.OutputStream

/**
 * Writes a bundled asset into [targetFile] atomically so failed installs do not
 * leave partial runtime files behind.
 */
internal fun installFileAtomically(
    targetFile: File,
    executable: Boolean,
    writer: (OutputStream) -> Unit,
): File {
    if (targetFile.exists()) {
        return targetFile
    }

    targetFile.parentFile?.mkdirs()
    val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
    if (tempFile.exists()) {
        tempFile.delete()
    }

    var installed = false
    try {
        tempFile.outputStream().use(writer)
        if (executable && !tempFile.setExecutable(true, false)) {
            error("Unable to mark bundled curl runtime as executable.")
        }
        if (!tempFile.renameTo(targetFile)) {
            error("Unable to install bundled curl runtime asset.")
        }
        installed = true
        return targetFile
    } finally {
        if (!installed && tempFile.exists()) {
            tempFile.delete()
        }
    }
}
