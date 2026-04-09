package dev.nettools.android.service

import dev.nettools.android.domain.model.TransferStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class TransferIoSupportTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `drainQueueSequentially processes items one at a time`() = runTest {
        val queue = ArrayDeque(listOf("first", "second"))
        val active = AtomicInteger(0)
        var maxActive = 0
        val processed = mutableListOf<String>()

        drainQueueSequentially(
            dequeue = { queue.removeFirstOrNull() },
            process = { item ->
                val current = active.incrementAndGet()
                maxActive = maxOf(maxActive, current)
                delay(1)
                processed += item
                active.decrementAndGet()
            },
        )

        assertEquals(listOf("first", "second"), processed)
        assertEquals(1, maxActive)
    }

    @Test
    fun `buildStableSafTempFile returns same path for same destination and remote path`() {
        val cacheDir = tempDir.toFile()

        val first = buildStableSafTempFile(
            cacheDir = cacheDir,
            destinationUri = "content://tree/primary%3ADownload",
            remotePath = "/srv/file.txt",
            remoteFileName = "file.txt",
        )
        val second = buildStableSafTempFile(
            cacheDir = cacheDir,
            destinationUri = "content://tree/primary%3ADownload",
            remotePath = "/srv/file.txt",
            remoteFileName = "file.txt",
        )

        assertEquals(first.absolutePath, second.absolutePath)
    }

    @Test
    fun `prepareResumeOffset deletes stale oversized partial file`() {
        val workingFile = tempDir.resolve("resume.part").toFile().apply {
            writeBytes(ByteArray(32))
        }

        val resumeOffset = prepareResumeOffset(workingFile, remoteSizeBytes = 16L)

        assertEquals(0L, resumeOffset)
        assertFalse(workingFile.exists())
    }

    @Test
    fun `prepareResumeOffset preserves valid partial file`() {
        val workingFile = tempDir.resolve("resume.part").toFile().apply {
            writeBytes(ByteArray(8))
        }

        val resumeOffset = prepareResumeOffset(workingFile, remoteSizeBytes = 16L)

        assertEquals(8L, resumeOffset)
        assertTrue(workingFile.exists())
    }

    @Test
    fun `shouldDeleteWorkingFile keeps partial SAF download after transfer failure`() {
        val target = DownloadTarget(tempDir.resolve("resume.part").toFile(), "content://tree/test")

        val shouldDelete = shouldDeleteWorkingFile(
            target = target,
            finalStatus = TransferStatus.FAILED,
            safFinalizationAttempted = false,
            safFinalizationSucceeded = false,
        )

        assertFalse(shouldDelete)
    }

    @Test
    fun `shouldDeleteWorkingFile deletes SAF temp after finalization failure`() {
        val target = DownloadTarget(tempDir.resolve("resume.part").toFile(), "content://tree/test")

        val shouldDelete = shouldDeleteWorkingFile(
            target = target,
            finalStatus = TransferStatus.FAILED,
            safFinalizationAttempted = true,
            safFinalizationSucceeded = false,
        )

        assertTrue(shouldDelete)
    }

    @Test
    fun `shouldDeleteWorkingFile deletes SAF temp after successful completion`() {
        val target = DownloadTarget(tempDir.resolve("resume.part").toFile(), "content://tree/test")

        val shouldDelete = shouldDeleteWorkingFile(
            target = target,
            finalStatus = TransferStatus.COMPLETED,
            safFinalizationAttempted = true,
            safFinalizationSucceeded = true,
        )

        assertTrue(shouldDelete)
    }

    @Test
    fun `StreamSourceFile opens supplied stream and exposes metadata`() {
        val source = StreamSourceFile(
            sourceName = "upload.bin",
            sourceLength = 4L,
            openStream = { ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)) },
        )

        val bytes = source.getInputStream().use { it.readBytes() }

        assertEquals("upload.bin", source.name)
        assertEquals(4L, source.length)
        assertTrue(bytes.contentEquals(byteArrayOf(1, 2, 3, 4)))
    }
}
