package dev.nettools.android.data.ssh

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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

/**
 * End-to-end integration tests combining [ScpClient] and [SftpClient] operations
 * over a single authenticated SSH connection. Verifies interoperability between
 * the two clients sharing the same [SSHClient] instance.
 */
class TransferFlowIntegrationTest {

    @TempDir
    lateinit var serverRoot: Path

    @TempDir
    lateinit var clientDir: Path

    private lateinit var server: SshServer
    private val scpClient = ScpClient()
    private val sftpClient = SftpClient()

    @BeforeEach
    fun startServer() {
        server = SshServer.setUpDefaultServer().apply {
            port = 0
            keyPairProvider = SimpleGeneratorHostKeyProvider()
            passwordAuthenticator = PasswordAuthenticator { _, _, _ -> true }
            commandFactory = ScpCommandFactory.Builder().build()
            subsystemFactories = listOf(SftpSubsystemFactory.Builder().build())
            fileSystemFactory = VirtualFileSystemFactory(serverRoot)
            start()
        }
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

    // ── 5.1 Upload then browse ────────────────────────────────────────────────

    @Test
    fun `upload via scp then listDirectory via sftp shows new file`() = runTest {
        val content = "hello sftp integration".toByteArray()
        val localFile = File(clientDir.toFile(), "hello.txt").apply { writeBytes(content) }
        val sshClient = openSshClient()
        try {
            scpClient.upload(sshClient, localFile, "/hello.txt").toList()
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertTrue(entries.any { it.name == "hello.txt" }, "Expected hello.txt in listing")
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `stat on uploaded file returns correct size`() = runTest {
        val content = "stat content test".toByteArray()
        val localFile = File(clientDir.toFile(), "stat_file.txt").apply { writeBytes(content) }
        val sshClient = openSshClient()
        try {
            scpClient.upload(sshClient, localFile, "/stat_file.txt").toList()
            val entry = sftpClient.stat(sshClient, "/stat_file.txt")
            assertEquals(content.size.toLong(), entry?.sizeBytes)
        } finally {
            sshClient.close()
        }
    }

    // ── 5.2 Browse, rename, then download ────────────────────────────────────

    @Test
    fun `browse then rename then download produces correct content`() = runTest {
        val content = "original content for rename test".toByteArray()
        serverRoot.resolve("original.txt").toFile().writeBytes(content)
        val sshClient = openSshClient()
        try {
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertTrue(entries.any { it.name == "original.txt" })

            sftpClient.rename(sshClient, "/original.txt", "/renamed.txt")

            val localFile = File(clientDir.toFile(), "renamed.txt")
            scpClient.download(sshClient, "/renamed.txt", localFile).toList()

            assertFalse(serverRoot.resolve("original.txt").toFile().exists())
            assertArrayEquals(content, localFile.readBytes())
        } finally {
            sshClient.close()
        }
    }

    // ── 5.3 Upload then resume download ──────────────────────────────────────

    @Test
    fun `upload then resumable download with offset produces complete file`() = runTest {
        val fullContent = ByteArray(100) { it.toByte() }
        val localFile = File(clientDir.toFile(), "file_for_resume.bin").apply {
            writeBytes(fullContent)
        }
        val sshClient = openSshClient()
        try {
            scpClient.upload(sshClient, localFile, "/file_for_resume.bin").toList()

            val partialFile = File(clientDir.toFile(), "file_resume_dl.bin").apply {
                writeBytes(fullContent.copyOf(50))
            }
            scpClient.downloadResumable(sshClient, "/file_for_resume.bin", partialFile, 50L).toList()

            assertArrayEquals(fullContent, partialFile.readBytes(), "Resumed download should produce complete file")
        } finally {
            sshClient.close()
        }
    }

    // ── 5.4 mkdir then upload into new directory ──────────────────────────────

    @Test
    fun `mkdir then upload into new directory then list shows file`() = runTest {
        val content = "file in new dir".toByteArray()
        val localFile = File(clientDir.toFile(), "file.txt").apply { writeBytes(content) }
        val sshClient = openSshClient()
        try {
            sftpClient.mkdir(sshClient, "/newdir")
            scpClient.upload(sshClient, localFile, "/newdir/file.txt").toList()
            val entries = sftpClient.listDirectory(sshClient, "/newdir")
            assertTrue(entries.any { it.name == "file.txt" }, "Expected file.txt in /newdir listing")
        } finally {
            sshClient.close()
        }
    }
}
