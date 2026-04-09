package dev.nettools.android.data.ssh

import dev.nettools.android.domain.model.RemoteFileEntry
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.FileAttributes
import net.schmizz.sshj.sftp.FileMode
import net.schmizz.sshj.sftp.RemoteResourceInfo
import net.schmizz.sshj.sftp.SFTPClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [SftpClient], verifying domain model mapping and
 * correct delegation to SSHJ's [SFTPClient].
 */
class SftpClientTest {

    private val sftpClient = SftpClient()

    private fun makeSSH(sftp: SFTPClient): SSHClient = mockk<SSHClient>().also {
        every { it.newSFTPClient() } returns sftp
    }

    private fun makeResourceInfo(
        name: String,
        size: Long,
        isDir: Boolean,
        mtime: Long = 0L,
    ): RemoteResourceInfo {
        val attrs = mockk<FileAttributes>()
        every { attrs.size } returns size
        every { attrs.mtime } returns mtime
        every { attrs.permissions } returns null
        every { attrs.type } returns if (isDir) FileMode.Type.DIRECTORY else FileMode.Type.REGULAR

        return mockk<RemoteResourceInfo>().also {
            every { it.name } returns name
            every { it.attributes } returns attrs
        }
    }

    // ── listDirectory ─────────────────────────────────────────────────────────

    @Test
    fun `listDirectory - maps file entries to domain model`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)

        every { sftp.ls("/home/user") } returns listOf(
            makeResourceInfo("file.txt", 1024L, isDir = false, mtime = 1_000L),
            makeResourceInfo("docs", 0L, isDir = true, mtime = 2_000L),
        )

        val entries = sftpClient.listDirectory(sshClient, "/home/user")

        assertEquals(2, entries.size)

        val file = entries.first { !it.isDirectory }
        assertEquals("file.txt", file.name)
        assertEquals("/home/user/file.txt", file.path)
        assertEquals(1024L, file.sizeBytes)
        assertFalse(file.isDirectory)
        assertEquals(1_000L, file.modifiedAt)

        val dir = entries.first { it.isDirectory }
        assertEquals("docs", dir.name)
        assertTrue(dir.isDirectory)
    }

    @Test
    fun `listDirectory - closes SFTP client after listing`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)
        every { sftp.ls(any<String>()) } returns emptyList()

        sftpClient.listDirectory(sshClient, "/")

        verify { sftp.close() }
    }

    @Test
    fun `listDirectory - returns empty list for empty directory`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)
        every { sftp.ls(any<String>()) } returns emptyList()

        val entries = sftpClient.listDirectory(sshClient, "/empty")

        assertTrue(entries.isEmpty())
    }

    // ── stat ──────────────────────────────────────────────────────────────────

    @Test
    fun `stat - returns RemoteFileEntry for existing path`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)
        val attrs = mockk<FileAttributes>()

        every { attrs.size } returns 512L
        every { attrs.permissions } returns null
        every { attrs.type } returns FileMode.Type.REGULAR
        every { attrs.mtime } returns 9_999L
        every { sftp.stat("/path/to/file.txt") } returns attrs

        val entry = sftpClient.stat(sshClient, "/path/to/file.txt")

        assertTrue(entry != null)
        assertEquals("file.txt", entry!!.name)
        assertEquals(512L, entry.sizeBytes)
        assertFalse(entry.isDirectory)
    }

    @Test
    fun `stat - returns null when SFTP throws`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)
        every { sftp.stat(any<String>()) } throws RuntimeException("File not found")

        val entry = sftpClient.stat(sshClient, "/nonexistent")

        assertNull(entry)
    }

    // ── mkdir ─────────────────────────────────────────────────────────────────

    @Test
    fun `mkdir - delegates to SFTP mkdir and closes client`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)

        sftpClient.mkdir(sshClient, "/new/dir")

        verify { sftp.mkdir("/new/dir") }
        verify { sftp.close() }
    }

    // ── rename ────────────────────────────────────────────────────────────────

    @Test
    fun `rename - delegates to SFTP rename and closes client`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)

        sftpClient.rename(sshClient, "/old/path", "/new/path")

        verify { sftp.rename("/old/path", "/new/path") }
        verify { sftp.close() }
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    fun `delete - delegates to SFTP rm and closes client`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)

        every { sftp.type("/path/to/remove.txt") } returns FileMode.Type.REGULAR

        sftpClient.delete(sshClient, "/path/to/remove.txt")

        verify { sftp.rm("/path/to/remove.txt") }
        verify { sftp.close() }
    }

    @Test
    fun `delete - removes empty directory with rmdir`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)

        every { sftp.type("/empty-dir") } returns FileMode.Type.DIRECTORY
        every { sftp.ls("/empty-dir") } returns emptyList()

        sftpClient.delete(sshClient, "/empty-dir")

        verify { sftp.rmdir("/empty-dir") }
        verify(exactly = 0) { sftp.rm(any()) }
    }

    @Test
    fun `delete - removes non-empty directories recursively`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)

        every { sftp.type("/dir") } returns FileMode.Type.DIRECTORY
        every { sftp.type("/dir/nested.txt") } returns FileMode.Type.REGULAR
        every { sftp.ls("/dir") } returns listOf(
            makeResourceInfo(".", 0L, isDir = true),
            makeResourceInfo("..", 0L, isDir = true),
            makeResourceInfo("nested.txt", 5L, isDir = false),
        )

        sftpClient.delete(sshClient, "/dir")

        verify { sftp.rm("/dir/nested.txt") }
        verify { sftp.rmdir("/dir") }
    }

    // ── getFileSize ───────────────────────────────────────────────────────────

    @Test
    fun `getFileSize - returns size from stat`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)
        val attrs = mockk<FileAttributes>()

        every { attrs.size } returns 2048L
        every { sftp.stat(any<String>()) } returns attrs

        val size = sftpClient.getFileSize(sshClient, "/data/big.bin")

        assertEquals(2048L, size)
    }

    @Test
    fun `getFileSize - returns null when stat throws`() = runTest {
        val sftp = mockk<SFTPClient>(relaxed = true)
        val sshClient = makeSSH(sftp)
        every { sftp.stat(any<String>()) } throws RuntimeException("No such file")

        val size = sftpClient.getFileSize(sshClient, "/missing.bin")

        assertNull(size)
    }
}
