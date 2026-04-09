package dev.nettools.android.util

import java.net.URLDecoder

/**
 * Formats a byte count as a human-readable size string (e.g. "1.2 MB", "345 KB").
 */
fun Long.toFormattedSize(): String = when {
    this >= 1_073_741_824L -> "%.1f GB".format(this / 1_073_741_824.0)
    this >= 1_048_576L -> "%.1f MB".format(this / 1_048_576.0)
    this >= 1_024L -> "%.1f KB".format(this / 1_024.0)
    else -> "$this B"
}

/**
 * Formats a bytes-per-second speed value as a human-readable speed string
 * (e.g. "1.2 MB/s", "345 KB/s").
 */
fun Double.toSpeedString(): String = when {
    this >= 1_073_741_824.0 -> "%.1f GB/s".format(this / 1_073_741_824.0)
    this >= 1_048_576.0 -> "%.1f MB/s".format(this / 1_048_576.0)
    this >= 1_024.0 -> "%.1f KB/s".format(this / 1_024.0)
    else -> "%.0f B/s".format(this)
}

/**
 * Estimates and formats the time remaining for a transfer as a human-readable string
 * (e.g. "2m 30s remaining", "45s remaining").
 *
 * @param speedBytesPerSec Current transfer speed in bytes per second.
 * @return Formatted ETA string, or "Unknown" if speed is zero or total is unknown.
 */
fun Long.toEtaString(speedBytesPerSec: Double): String {
    if (speedBytesPerSec <= 0.0 || this < 0L) return "Unknown"
    val remainingSec = (this / speedBytesPerSec).toLong()
    val minutes = remainingSec / 60
    val seconds = remainingSec % 60
    return when {
        minutes > 0 -> "${minutes}m ${seconds}s remaining"
        else -> "${seconds}s remaining"
    }
}

/**
 * Returns "[REDACTED]" for any string value.
 * Use this whenever a password or key material might otherwise be logged.
 *
 * @return The literal string "[REDACTED]".
 */
fun String.sanitizeForLog(): String = "[REDACTED]"

/**
 * Formats a local path or SAF `content://` URI into a human-readable label for the UI.
 *
 * Examples:
 * - `/storage/emulated/0/Download/file.txt` -> unchanged
 * - `content://.../document/primary%3ADownload%2Fgiphy.gif` -> `Download/giphy.gif`
 * - `content://.../tree/primary%3ADownload%2FTransfer` -> `Download/Transfer`
 */
fun String.toDisplayPath(): String {
    if (!startsWith("content://")) return this

    val encodedDocumentId = sequenceOf("/document/", "/tree/")
        .mapNotNull { marker ->
            substringAfter(marker, missingDelimiterValue = "")
                .takeIf { it.isNotBlank() }
                ?.substringBefore('/')
        }
        .firstOrNull()
        ?: return decodeSafSegment(substringAfterLast('/'))

    return formatDocumentId(decodeSafSegment(encodedDocumentId))
}

private fun decodeSafSegment(value: String): String =
    URLDecoder.decode(value, "UTF-8")

private fun formatDocumentId(documentId: String): String {
    val volumePrefix = documentId.substringBefore(':', missingDelimiterValue = "")
    val relativePath = documentId.substringAfter(':', missingDelimiterValue = documentId).trim('/')
    return when {
        relativePath.isNotBlank() -> relativePath
        volumePrefix.isNotBlank() -> volumePrefix
        else -> documentId.substringAfterLast('/')
    }
}
