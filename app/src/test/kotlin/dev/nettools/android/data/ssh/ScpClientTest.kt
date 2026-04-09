package dev.nettools.android.data.ssh

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.RemoteFile
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.sftp.FileAttributes
import net.schmizz.sshj.xfer.LocalSourceFile
import net.schmizz.sshj.xfer.scp.SCPFileTransfer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

/**
 * Unit tests for [ScpClient].
 *
 * These tests focus on error propagation and edge-case logic. End-to-end
 * upload/download/resume behaviour is covered by [ScpClientIntegrationTest].
 */
class ScpClientTest {

    private val scpClient = ScpClient()

    @TempDir
    lateinit var tempDir: Path

    // ── upload error propagation ──────────────────────────────────────────────

    @Test
    fun `upload - exception from SCP propagates through flow`() = runTest {
        val localFile = File(tempDir.toFile(), "test.txt").apply { writeText("hello") }
        val sshClient = mockk<SSHClient>()
        val scpTransfer = mockk<SCPFileTransfer>()

        every { sshClient.newSCPFileTransfer() } returns scpTransfer
        every { scpTransfer.transferListener = any() } just Runs
        every { scpTransfer.upload(any<LocalSourceFile>(), any<String>()) } throws
            RuntimeException("Connection lost during SCP")

        val ex = assertThrows<RuntimeException> {
            scpClient.upload(sshClient, localFile, "/remote/test.txt").toList()
        }
        assertEquals("Connection lost during SCP", ex.message)
    }

    @Test
    fun `upload - SFTP rename is called with part path and final path`() = runTest {
        val localFile = File(tempDir.toFile(), "data.bin").apply { writeBytes(ByteArray(16)) }
        val sshClient = mockk<SSHClient>()
        val scpTransfer = mockk<SCPFileTransfer>()
        val sftpClient = mockk<SFTPClient>(relaxed = true)

        every { sshClient.newSCPFileTransfer() } returns scpTransfer
        every { sshClient.newSFTPClient() } returns sftpClient
        every { scpTransfer.transferListener = any() } just Runs
        every { scpTransfer.upload(any<LocalSourceFile>(), any<String>()) } just Runs

        scpClient.upload(sshClient, localFile, "/dest/data.bin").toList()

        verify { sftpClient.rename("/dest/data.bin.part", "/dest/data.bin") }
        verify { sftpClient.close() }
    }

    // ── download error propagation ────────────────────────────────────────────

    @Test
    fun `download - exception from SCP propagates through flow`() = runTest {
        val localFile = File(tempDir.toFile(), "out.txt")
        val sshClient = mockk<SSHClient>()
        val scpTransfer = mockk<SCPFileTransfer>()

        every { sshClient.newSCPFileTransfer() } returns scpTransfer
        every { scpTransfer.transferListener = any() } just Runs
        every { scpTransfer.download(any<String>(), any<net.schmizz.sshj.xfer.LocalDestFile>()) } throws
            RuntimeException("Permission denied")

        val ex = assertThrows<RuntimeException> {
            scpClient.download(sshClient, "/remote/out.txt", localFile).toList()
        }
        assertEquals("Permission denied", ex.message)
    }

    // ── downloadResumable ─────────────────────────────────────────────────────

    @Test
    fun `downloadResumable - opens SFTP and reads from resume offset`() = runTest {
        val content = "0123456789ABCDEF"
        val localFile = File(tempDir.toFile(), "partial.bin")
            .apply { writeText(content.substring(0, 8)) } // 8 bytes already present
        val sshClient = mockk<SSHClient>()
        val sftpClient = mockk<SFTPClient>(relaxed = true)
        val remoteFile = mockk<RemoteFile>(relaxed = true)
        val attrs = mockk<FileAttributes>()

        every { sshClient.newSFTPClient() } returns sftpClient
        every { sftpClient.open(any<String>()) } returns remoteFile
        every { remoteFile.fetchAttributes() } returns attrs
        every { attrs.size } returns content.length.toLong()
        every { remoteFile.read(any<Long>(), any(), any<Int>(), any<Int>()) } returns -1

        scpClient.downloadResumable(
            sshClient, "/remote/partial.bin", localFile, resumeOffset = 8L
        ).toList()

        verify { sftpClient.open("/remote/partial.bin") }
        verify { remoteFile.fetchAttributes() }
    }

    @Test
    fun `downloadResumable - closes remote file even when read fails`() = runTest {
        val localFile = File(tempDir.toFile(), "fail.bin")
        val sshClient = mockk<SSHClient>()
        val sftpClient = mockk<SFTPClient>(relaxed = true)
        val remoteFile = mockk<RemoteFile>(relaxed = true)
        val attrs = mockk<FileAttributes>()

        every { sshClient.newSFTPClient() } returns sftpClient
        every { sftpClient.open(any<String>()) } returns remoteFile
        every { remoteFile.fetchAttributes() } returns attrs
        every { attrs.size } returns 100L
        every { remoteFile.read(any<Long>(), any(), any<Int>(), any<Int>()) } throws
            RuntimeException("Read error")

        assertThrows<RuntimeException> {
            scpClient.downloadResumable(sshClient, "/remote/fail.bin", localFile, 0L).toList()
        }

        verify { remoteFile.close() }
    }
}
