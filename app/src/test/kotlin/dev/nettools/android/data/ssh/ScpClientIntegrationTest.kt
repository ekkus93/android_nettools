package dev.nettools.android.data.ssh

import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
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
}
