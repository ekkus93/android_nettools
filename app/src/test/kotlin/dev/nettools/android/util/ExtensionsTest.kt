package dev.nettools.android.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for extension functions in [Extensions.kt].
 */
class ExtensionsTest {

    // ── toFormattedSize ───────────────────────────────────────────────────────

    @Test
    fun `toFormattedSize formats bytes correctly`() {
        assertEquals("512 B", 512L.toFormattedSize())
    }

    @Test
    fun `toFormattedSize formats kilobytes correctly`() {
        val result = 2048L.toFormattedSize()
        assertTrue(result.contains("KB"), "Expected KB but got: $result")
    }

    @Test
    fun `toFormattedSize formats megabytes correctly`() {
        val result = (2 * 1_048_576L).toFormattedSize()
        assertTrue(result.contains("MB"), "Expected MB but got: $result")
    }

    @Test
    fun `toFormattedSize formats gigabytes correctly`() {
        val result = (2 * 1_073_741_824L).toFormattedSize()
        assertTrue(result.contains("GB"), "Expected GB but got: $result")
    }

    @Test
    fun `toFormattedSize zero bytes`() {
        assertEquals("0 B", 0L.toFormattedSize())
    }

    // ── toSpeedString ─────────────────────────────────────────────────────────

    @Test
    fun `toSpeedString formats bytes per second`() {
        val result = 512.0.toSpeedString()
        assertTrue(result.contains("B/s"), "Expected B/s but got: $result")
    }

    @Test
    fun `toSpeedString formats kilobytes per second`() {
        val result = 2048.0.toSpeedString()
        assertTrue(result.contains("KB/s"), "Expected KB/s but got: $result")
    }

    @Test
    fun `toSpeedString formats megabytes per second`() {
        val result = (2.0 * 1_048_576).toSpeedString()
        assertTrue(result.contains("MB/s"), "Expected MB/s but got: $result")
    }

    @Test
    fun `toSpeedString formats gigabytes per second`() {
        val result = (2.0 * 1_073_741_824).toSpeedString()
        assertTrue(result.contains("GB/s"), "Expected GB/s but got: $result")
    }

    // ── toEtaString ───────────────────────────────────────────────────────────

    @Test
    fun `toEtaString returns Unknown for zero speed`() {
        assertEquals("Unknown", 1000L.toEtaString(0.0))
    }

    @Test
    fun `toEtaString returns Unknown for negative remaining bytes`() {
        assertEquals("Unknown", (-1L).toEtaString(100.0))
    }

    @Test
    fun `toEtaString formats seconds only when under a minute`() {
        // 500 bytes remaining at 100 B/s = 5 seconds
        val result = 500L.toEtaString(100.0)
        assertTrue(result.contains("5s"), "Expected '5s' in result but got: $result")
    }

    @Test
    fun `toEtaString formats minutes and seconds`() {
        // 150 bytes remaining at 1 B/s = 150 seconds = 2m 30s
        val result = 150L.toEtaString(1.0)
        assertTrue(result.contains("2m"), "Expected '2m' in result but got: $result")
        assertTrue(result.contains("30s"), "Expected '30s' in result but got: $result")
    }

    @Test
    fun `toEtaString contains remaining keyword`() {
        val result = 60L.toEtaString(1.0)
        assertTrue(result.contains("remaining"), "Expected 'remaining' in result but got: $result")
    }

    // ── sanitizeForLog ────────────────────────────────────────────────────────

    @Test
    fun `sanitizeForLog returns REDACTED for any string`() {
        assertEquals("[REDACTED]", "super_secret_password123".sanitizeForLog())
    }

    @Test
    fun `sanitizeForLog returns REDACTED for empty string`() {
        assertEquals("[REDACTED]", "".sanitizeForLog())
    }

    // ── toDisplayPath ──────────────────────────────────────────────────────────

    @Test
    fun `toDisplayPath keeps filesystem paths unchanged`() {
        assertEquals("/storage/emulated/0/Download/giphy.gif", "/storage/emulated/0/Download/giphy.gif".toDisplayPath())
    }

    @Test
    fun `toDisplayPath formats SAF document URIs`() {
        val uri = "content://com.android.externalstorage.documents/document/primary%3ADownload%2Fgiphy.gif"

        assertEquals("Download/giphy.gif", uri.toDisplayPath())
    }

    @Test
    fun `toDisplayPath formats SAF tree URIs`() {
        val uri = "content://com.android.externalstorage.documents/tree/primary%3ADownload%2FTransfer"

        assertEquals("Download/Transfer", uri.toDisplayPath())
    }
}
