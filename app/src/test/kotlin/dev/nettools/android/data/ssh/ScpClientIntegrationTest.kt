package dev.nettools.android.data.ssh

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.scp.server.ScpCommandFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.sftp.server.SftpSubsystemFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.InputStream
import java.nio.file.Path

/**
 * Integration tests for [ScpClient] using an in-process Apache MINA SSHD server.
 *
 * These tests exercise real SCP/SFTP transfers over a loopback SSH connection,
 * covering: upload, download, and resumable download.
 */
class ScpClientIntegrationTest {

    @TempDir
    lateinit var serverRoot: Path

    @TempDir
    lateinit var clientDir: Path

    private lateinit var server: SshServer
    private lateinit var scpClient: ScpClient

    @BeforeEach
    fun startServer() {
        server = SshServer.setUpDefaultServer().apply {
            port = 0 // bind to any available port
            keyPairProvider = SimpleGeneratorHostKeyProvider()
            passwordAuthenticator = PasswordAuthenticator { _, _, _ -> true }
            commandFactory = ScpCommandFactory.Builder().build()
            subsystemFactories = listOf(SftpSubsystemFactory.Builder().build())
            fileSystemFactory = VirtualFileSystemFactory(serverRoot)
            start()
        }
        scpClient = ScpClient()
    }

    @AfterEach
    fun stopServer() {
        server.stop(true)
    }

    private fun openSshClient(): SSHClient = SSHClient().apply {
        addHostKeyVerifier(PromiscuousVerifier())
        connect("localhost", server.port)
        authPassword("testuser", "testpass")
    }

    // ── upload ────────────────────────────────────────────────────────────────

    @Test
    fun `upload - transfers file to server and renames from part`() = runTest {
        val content = "Hello, integration test!".toByteArray()
        val localFile = File(clientDir.toFile(), "upload_test.txt").apply { writeBytes(content) }
        val remotePath = "/upload_test.txt"

        val sshClient = openSshClient()
        try {
            val progress = scpClient.upload(sshClient, localFile, remotePath).toList()

            assertTrue(progress.isNotEmpty(), "Expected at least one progress event")
            assertEquals(content.size.toLong(), progress.last().bytesTransferred)

            val uploaded = serverRoot.resolve("upload_test.txt").toFile()
            assertTrue(uploaded.exists(), "Uploaded file should exist on server")
            assertArrayEquals(content, uploaded.readBytes())
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `upload - part file is not left on server after completion`() = runTest {
        val localFile = File(clientDir.toFile(), "atomic_test.txt").apply { writeText("data") }
        val sshClient = openSshClient()
        try {
            scpClient.upload(sshClient, localFile, "/atomic_test.txt").toList()

            val partFile = serverRoot.resolve("atomic_test.txt.part").toFile()
            assertTrue(!partFile.exists(), ".part file should be removed after upload")
        } finally {
            sshClient.close()
        }
    }

    // ── download ──────────────────────────────────────────────────────────────

    @Test
    fun `download - retrieves file from server`() = runTest {
        val content = "Server file content".toByteArray()
        serverRoot.resolve("server_file.txt").toFile().writeBytes(content)

        val localFile = File(clientDir.toFile(), "downloaded.txt")
        val sshClient = openSshClient()
        try {
            scpClient.download(sshClient, "/server_file.txt", localFile).toList()

            assertTrue(localFile.exists(), "Downloaded file should exist locally")
            assertArrayEquals(content, localFile.readBytes())
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `download - emits progress events`() = runTest {
        val content = ByteArray(4096) { it.toByte() }
        serverRoot.resolve("big.bin").toFile().writeBytes(content)

        val localFile = File(clientDir.toFile(), "big.bin")
        val sshClient = openSshClient()
        try {
            val progress = scpClient.download(sshClient, "/big.bin", localFile).toList()

            assertTrue(progress.isNotEmpty(), "Expected progress events during download")
        } finally {
            sshClient.close()
        }
    }

    // ── downloadResumable ─────────────────────────────────────────────────────

    @Test
    fun `downloadResumable - resumes download from offset`() = runTest {
        val fullContent = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toByteArray()
        serverRoot.resolve("resume.txt").toFile().writeBytes(fullContent)

        val resumeOffset = 10L
        val partialFile = File(clientDir.toFile(), "resume.txt").apply {
            writeBytes(fullContent.copyOf(resumeOffset.toInt()))
        }

        val sshClient = openSshClient()
        try {
            scpClient.downloadResumable(sshClient, "/resume.txt", partialFile, resumeOffset)
                .toList()

            val result = partialFile.readBytes()
            assertArrayEquals(fullContent, result, "Resumed download should produce full file")
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `downloadResumable - emits progress for resumed bytes`() = runTest {
        val fullContent = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toByteArray()
        serverRoot.resolve("resume_progress.txt").toFile().writeBytes(fullContent)

        val resumeOffset = 10L
        val partialFile = File(clientDir.toFile(), "resume_progress.txt").apply {
            writeBytes(fullContent.copyOf(resumeOffset.toInt()))
        }

        val sshClient = openSshClient()
        try {
            val progress = scpClient.downloadResumable(
                sshClient,
                "/resume_progress.txt",
                partialFile,
                resumeOffset,
            ).toList()

            assertTrue(progress.isNotEmpty(), "Expected resumed transfer to emit progress")
            assertTrue(progress.first().isResuming, "Expected resumed transfer marker")
            assertEquals(resumeOffset, progress.first().resumeOffsetBytes)
            assertEquals(fullContent.size.toLong(), progress.last().bytesTransferred)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `downloadResumable - works from zero offset (full download via SFTP)`() = runTest {
        val content = "SFTP full download".toByteArray()
        serverRoot.resolve("sftp_full.txt").toFile().writeBytes(content)

        val localFile = File(clientDir.toFile(), "sftp_full.txt")
        val sshClient = openSshClient()
        try {
            scpClient.downloadResumable(sshClient, "/sftp_full.txt", localFile, resumeOffset = 0L)
                .toList()

            assertArrayEquals(content, localFile.readBytes())
        } finally {
            sshClient.close()
        }
    }

    // ── Phase 3: Cancellation & Error Tests ───────────────────────────────────

    /**
     * InputStream wrapper that throttles reads to [targetBytesPerSec] bytes/second,
     * ensuring large transfers stay in-flight long enough for cancellation tests.
     */
    private inner class ThrottledStream(
        private val inner: InputStream,
        private val targetBytesPerSec: Long,
    ) : InputStream() {
        private val startMs = System.currentTimeMillis()
        private var totalRead = 0L

        override fun read(): Int {
            val b = inner.read()
            if (b >= 0) throttle(1)
            return b
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val n = inner.read(b, off, len)
            if (n > 0) throttle(n.toLong())
            return n
        }

        private fun throttle(bytes: Long) {
            totalRead += bytes
            val expectedMs = totalRead * 1_000L / targetBytesPerSec
            val elapsedMs = System.currentTimeMillis() - startMs
            if (expectedMs > elapsedMs) Thread.sleep(expectedMs - elapsedMs)
        }

        override fun close() = inner.close()
    }

    /**
     * [FileSystemFile] that throttles its [InputStream] to [targetBytesPerSec] bytes/second,
     * guaranteeing the transfer is in-flight during the cancellation window.
     */
    private inner class SlowFileSystemFile(
        private val file: File,
        private val targetBytesPerSec: Long,
    ) : net.schmizz.sshj.xfer.FileSystemFile(file) {
        override fun getInputStream(): InputStream =
            ThrottledStream(file.inputStream(), targetBytesPerSec)
    }

    /**
     * Creates a large binary file in the client directory for cancellation tests.
     */
    private fun createLargeFile(sizeBytes: Int): File {
        val f = File(clientDir.toFile(), "large_$sizeBytes.bin")
        f.writeBytes(ByteArray(sizeBytes) { it.toByte() })
        return f
    }

    // ── 3.1 Upload cancellation ───────────────────────────────────────────────

    @Test
    fun `upload cancellation - complete file not left on server after cancel`() = runBlocking {
        // Throttle to 1 MB/s so 5 MB takes ~5 s; cancel at 500 ms = mid-transfer.
        val largeFile = createLargeFile(5_000_000)
        val slowSource = SlowFileSystemFile(largeFile, 1_000_000L)
        val sshClient = openSshClient()
        try {
            val job = launch(Dispatchers.IO) {
                scpClient.upload(sshClient, slowSource, "/upload_cancel.bin").collect { }
            }
            delay(500L)
            job.cancel()
            job.join()
            delay(300L)
            assertFalse(
                serverRoot.resolve("upload_cancel.bin").toFile().exists(),
                "Complete upload file should not exist after cancellation"
            )
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `upload cancellation - cancelled job is cancelled`() = runBlocking {
        val largeFile = createLargeFile(5_000_000)
        val slowSource = SlowFileSystemFile(largeFile, 1_000_000L)
        val sshClient = openSshClient()
        try {
            val job = launch(Dispatchers.IO) {
                scpClient.upload(sshClient, slowSource, "/upload_cancel2.bin").collect { }
            }
            delay(500L)
            job.cancel()
            job.join()
            assertTrue(job.isCancelled)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `upload cancellation - part file size less than total if it exists`() = runBlocking {
        val largeFile = createLargeFile(5_000_000)
        val slowSource = SlowFileSystemFile(largeFile, 1_000_000L)
        val sshClient = openSshClient()
        try {
            val job = launch(Dispatchers.IO) {
                scpClient.upload(sshClient, slowSource, "/upload_partial.bin").collect { }
            }
            delay(500L)
            job.cancel()
            job.join()
            delay(300L)
            val partFile = serverRoot.resolve("upload_partial.bin.part").toFile()
            if (partFile.exists()) {
                assertTrue(
                    partFile.length() < largeFile.length(),
                    "Part file size should be less than total: ${partFile.length()} >= ${largeFile.length()}"
                )
            }
            // The fully renamed file must not exist after a cancelled upload
            assertFalse(serverRoot.resolve("upload_partial.bin").toFile().exists())
        } finally {
            sshClient.close()
        }
    }

    // ── 3.2 Download cancellation ─────────────────────────────────────────────

    @Test
    fun `download cancellation - local file smaller than server file after cancel`() = runBlocking {
        // 50 MB ensures the SFTP download is in-flight during the 1000 ms delay window.
        val largeContent = ByteArray(50_000_000) { it.toByte() }
        serverRoot.resolve("large_dl.bin").toFile().writeBytes(largeContent)
        val localFile = File(clientDir.toFile(), "large_dl.bin")
        val sshClient = openSshClient()
        try {
            val job = launch(Dispatchers.IO) {
                scpClient.download(sshClient, "/large_dl.bin", localFile).collect { }
            }
            delay(1000L)
            job.cancel()
            job.join()
            assertTrue(
                !localFile.exists() || localFile.length() < largeContent.size,
                "Expected partial or no local file after cancellation"
            )
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `download cancellation - cancelled job is cancelled`() = runBlocking {
        val largeContent = ByteArray(50_000_000) { it.toByte() }
        serverRoot.resolve("large_dl2.bin").toFile().writeBytes(largeContent)
        val localFile = File(clientDir.toFile(), "large_dl2.bin")
        val sshClient = openSshClient()
        try {
            val job = launch(Dispatchers.IO) {
                scpClient.download(sshClient, "/large_dl2.bin", localFile).collect { }
            }
            delay(1000L)
            job.cancel()
            job.join()
            assertTrue(job.isCancelled)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `download cancellation - local file length less than total if it exists`() = runBlocking {
        val largeContent = ByteArray(50_000_000) { it.toByte() }
        serverRoot.resolve("large_dl3.bin").toFile().writeBytes(largeContent)
        val localFile = File(clientDir.toFile(), "large_dl3.bin")
        val sshClient = openSshClient()
        try {
            val job = launch(Dispatchers.IO) {
                scpClient.download(sshClient, "/large_dl3.bin", localFile).collect { }
            }
            delay(1000L)
            job.cancel()
            job.join()
            assertTrue(
                !localFile.exists() || localFile.length() < largeContent.size.toLong(),
                "Expected partial or no local file, got: ${localFile.length()}"
            )
        } finally {
            sshClient.close()
        }
    }

    // ── 3.3 Upload: remote error ──────────────────────────────────────────────

    @Test
    fun `upload - fails when remote parent directory does not exist`() = runTest {
        val localFile = File(clientDir.toFile(), "test_err.txt").apply { writeText("data") }
        val sshClient = openSshClient()
        try {
            val result = runCatching {
                scpClient.upload(sshClient, localFile, "/nonexistent_dir/file.txt").toList()
            }
            assertTrue(result.isFailure, "Expected upload to non-existent directory to fail")
            assertFalse(result.exceptionOrNull() is kotlinx.coroutines.CancellationException)
        } finally {
            sshClient.close()
        }
    }

    // ── 3.4 Large file progress fidelity ─────────────────────────────────────

    @Test
    fun `upload - 1MB file emits multiple progress events`() = runTest {
        val content = ByteArray(1_048_576) { it.toByte() }
        val localFile = File(clientDir.toFile(), "large1m.bin").apply { writeBytes(content) }
        val sshClient = openSshClient()
        try {
            val progress = scpClient.upload(sshClient, localFile, "/large1m.bin").toList()
            assertTrue(progress.size > 1, "Expected multiple progress events, got ${progress.size}")
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `upload - final progress event has correct bytesTransferred`() = runTest {
        val content = ByteArray(1_048_576) { it.toByte() }
        val localFile = File(clientDir.toFile(), "large1m_final.bin").apply { writeBytes(content) }
        val sshClient = openSshClient()
        try {
            val progress = scpClient.upload(sshClient, localFile, "/large1m_final.bin").toList()
            assertEquals(content.size.toLong(), progress.last().bytesTransferred)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `download - 1MB file emits multiple progress events`() = runTest {
        val content = ByteArray(1_048_576) { it.toByte() }
        serverRoot.resolve("large1m_dl.bin").toFile().writeBytes(content)
        val localFile = File(clientDir.toFile(), "large1m_dl.bin")
        val sshClient = openSshClient()
        try {
            val progress = scpClient.download(sshClient, "/large1m_dl.bin", localFile).toList()
            assertTrue(progress.size > 1, "Expected multiple progress events, got ${progress.size}")
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `upload and download - last progress event has positive speedBytesPerSec`() = runTest {
        val content = ByteArray(1_048_576) { it.toByte() }
        val localFile = File(clientDir.toFile(), "speed_test.bin").apply { writeBytes(content) }
        val sshClient = openSshClient()
        try {
            val uploadProgress = scpClient.upload(sshClient, localFile, "/speed_test.bin").toList()
            assertTrue(
                uploadProgress.all { it.speedBytesPerSec > 0.0 },
                "All upload progress events should have positive speed"
            )
            val downloadFile = File(clientDir.toFile(), "speed_dl.bin")
            val downloadProgress = scpClient.download(sshClient, "/speed_test.bin", downloadFile).toList()
            // The first download event is emitted at offset=0 (before any bytes are read),
            // so its speed is 0. All subsequent events should have positive speed.
            assertTrue(
                downloadProgress.last().speedBytesPerSec > 0.0,
                "Last download progress event should have positive speed"
            )
        } finally {
            sshClient.close()
        }
    }
}
