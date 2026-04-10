package dev.nettools.android.data.curl

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import java.nio.file.Path

/**
 * Unit tests for [installFileAtomically].
 */
class AtomicFileInstallerTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `installFileAtomically writes target and leaves no temp file`() {
        val target = tempDir.resolve("curl").toFile()

        installFileAtomically(targetFile = target, executable = true) { output ->
            output.write("curl".toByteArray())
        }

        assertTrue(target.exists())
        assertTrue(target.canExecute())
        assertFalse(File(target.parentFile, "curl.tmp").exists())
        assertArrayEquals("curl".toByteArray(), target.readBytes())
    }

    @Test
    fun `installFileAtomically removes temp file when write fails`() {
        val target = tempDir.resolve("curl").toFile()

        runCatching {
            installFileAtomically(targetFile = target, executable = false) {
                throw IOException("boom")
            }
        }

        assertFalse(target.exists())
        assertFalse(File(target.parentFile, "curl.tmp").exists())
    }
}
