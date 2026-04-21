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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

/**
 * Integration tests for [SftpClient] using an in-process Apache MINA SSHD server.
 * Every public method is exercised over a real SFTP wire protocol connection.
 */
@Suppress("TooManyFunctions")
class SftpClientIntegrationTest {

    @TempDir
    lateinit var serverRoot: Path

    @TempDir
    lateinit var clientDir: Path

    private lateinit var server: SshServer
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

    // ── 1.1 listDirectory ────────────────────────────────────────────────────

    @Test
    fun `listDirectory - empty directory returns empty list`() = runTest {
        val sshClient = openSshClient()
        try {
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertTrue(entries.isEmpty(), "Expected empty list for empty dir")
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `listDirectory - two files returns two entries`() = runTest {
        serverRoot.resolve("a.txt").toFile().writeText("aaa")
        serverRoot.resolve("b.txt").toFile().writeText("bbb")
        val sshClient = openSshClient()
        try {
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertEquals(2, entries.size)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `listDirectory - entries have correct name path and isDirectory for files`() = runTest {
        serverRoot.resolve("hello.txt").toFile().writeText("hello")
        val sshClient = openSshClient()
        try {
            val entries = sftpClient.listDirectory(sshClient, "/")
            val entry = entries.single()
            assertEquals("hello.txt", entry.name)
            assertTrue(entry.path.endsWith("/hello.txt"), "path=${entry.path}")
            assertFalse(entry.isDirectory)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `listDirectory - subdirectory entry has isDirectory true`() = runTest {
        serverRoot.resolve("subdir").toFile().mkdir()
        val sshClient = openSshClient()
        try {
            val entries = sftpClient.listDirectory(sshClient, "/")
            val dir = entries.single { it.name == "subdir" }
            assertTrue(dir.isDirectory)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `listDirectory - does not include dot or dot-dot entries`() = runTest {
        serverRoot.resolve("file.txt").toFile().writeText("x")
        val sshClient = openSshClient()
        try {
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertFalse(entries.any { it.name == "." || it.name == ".." })
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `listDirectory - sizeBytes matches actual file size`() = runTest {
        val content = "hello world".toByteArray()
        serverRoot.resolve("sized.txt").toFile().writeBytes(content)
        val sshClient = openSshClient()
        try {
            val entries = sftpClient.listDirectory(sshClient, "/")
            val entry = entries.single { it.name == "sized.txt" }
            assertEquals(content.size.toLong(), entry.sizeBytes)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `listDirectory - path is absolute`() = runTest {
        serverRoot.resolve("sub").toFile().mkdir()
        serverRoot.resolve("sub").toFile().resolve("child.txt").writeText("c")
        val sshClient = openSshClient()
        try {
            val entries = sftpClient.listDirectory(sshClient, "/sub")
            val entry = entries.single()
            assertTrue(entry.path.startsWith("/"), "Expected absolute path, got: ${entry.path}")
            assertTrue(entry.path.contains("child.txt"))
        } finally {
            sshClient.close()
        }
    }

    // ── 1.2 resolvePath ──────────────────────────────────────────────────────

    @Test
    fun `resolvePath - tilde returns non-empty string without tilde`() = runTest {
        val sshClient = openSshClient()
        try {
            val resolved = sftpClient.resolvePath(sshClient, "~")
            assertTrue(resolved.isNotEmpty())
            assertFalse(resolved.contains("~"), "Expected ~ to be expanded, got: $resolved")
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `resolvePath - tilde-slash-subdir expands correctly`() = runTest {
        val sshClient = openSshClient()
        try {
            val resolved = sftpClient.resolvePath(sshClient, "~/subdir")
            assertTrue(resolved.contains("subdir"))
            assertFalse(resolved.contains("~"))
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `resolvePath - absolute path returned unchanged`() = runTest {
        val sshClient = openSshClient()
        try {
            val resolved = sftpClient.resolvePath(sshClient, "/absolute")
            assertEquals("/absolute", resolved)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `resolvePath - dot returns same as tilde`() = runTest {
        val sshClient = openSshClient()
        try {
            val home = sftpClient.resolvePath(sshClient, "~")
            val dot = sftpClient.resolvePath(sshClient, ".")
            assertEquals(home, dot)
        } finally {
            sshClient.close()
        }
    }

    // ── 1.3 mkdir ────────────────────────────────────────────────────────────

    @Test
    fun `mkdir - creates directory visible in listDirectory`() = runTest {
        val sshClient = openSshClient()
        try {
            sftpClient.mkdir(sshClient, "/newdir")
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertTrue(entries.any { it.name == "newdir" && it.isDirectory })
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `mkdir - on existing path throws`() = runTest {
        serverRoot.resolve("existing").toFile().mkdir()
        val sshClient = openSshClient()
        try {
            assertThrows<Exception> {
                sftpClient.mkdir(sshClient, "/existing")
            }
        } finally {
            sshClient.close()
        }
    }

    // ── 1.4 rename ───────────────────────────────────────────────────────────

    @Test
    fun `rename - old path gone new path appears`() = runTest {
        serverRoot.resolve("old.txt").toFile().writeText("data")
        val sshClient = openSshClient()
        try {
            sftpClient.rename(sshClient, "/old.txt", "/new.txt")
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertFalse(entries.any { it.name == "old.txt" })
            assertTrue(entries.any { it.name == "new.txt" })
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `rename - entry after rename uses new name`() = runTest {
        serverRoot.resolve("before.txt").toFile().writeText("data")
        val sshClient = openSshClient()
        try {
            sftpClient.rename(sshClient, "/before.txt", "/after.txt")
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertEquals("after.txt", entries.single().name)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `rename - directory renames and new name appears`() = runTest {
        serverRoot.resolve("dirA").toFile().mkdir()
        serverRoot.resolve("dirA").toFile().resolve("child.txt").writeText("c")
        val sshClient = openSshClient()
        try {
            sftpClient.rename(sshClient, "/dirA", "/dirB")
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertFalse(entries.any { it.name == "dirA" })
            assertTrue(entries.any { it.name == "dirB" && it.isDirectory })
        } finally {
            sshClient.close()
        }
    }

    // ── 1.5 delete ───────────────────────────────────────────────────────────

    @Test
    fun `delete - file removed from listDirectory`() = runTest {
        serverRoot.resolve("del.txt").toFile().writeText("bye")
        val sshClient = openSshClient()
        try {
            sftpClient.delete(sshClient, "/del.txt")
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertFalse(entries.any { it.name == "del.txt" })
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `delete - non-empty directory removed recursively`() = runTest {
        val dir = serverRoot.resolve("dir").toFile().also { it.mkdir() }
        File(dir, "child.txt").writeText("content")
        val sshClient = openSshClient()
        try {
            sftpClient.delete(sshClient, "/dir")
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertFalse(entries.any { it.name == "dir" })
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `delete - non-existent path throws`() = runTest {
        val sshClient = openSshClient()
        try {
            assertThrows<Exception> {
                sftpClient.delete(sshClient, "/does_not_exist.txt")
            }
        } finally {
            sshClient.close()
        }
    }

    // ── 1.6 stat ─────────────────────────────────────────────────────────────

    @Test
    fun `stat - existing file returns entry with correct size and isDirectory false`() = runTest {
        val content = "stat test".toByteArray()
        serverRoot.resolve("stat.txt").toFile().writeBytes(content)
        val sshClient = openSshClient()
        try {
            val entry = sftpClient.stat(sshClient, "/stat.txt")
            assertNotNull(entry)
            assertEquals(content.size.toLong(), entry!!.sizeBytes)
            assertFalse(entry.isDirectory)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `stat - existing directory returns isDirectory true`() = runTest {
        serverRoot.resolve("statdir").toFile().mkdir()
        val sshClient = openSshClient()
        try {
            val entry = sftpClient.stat(sshClient, "/statdir")
            assertNotNull(entry)
            assertTrue(entry!!.isDirectory)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `stat - non-existent path returns null`() = runTest {
        val sshClient = openSshClient()
        try {
            val entry = sftpClient.stat(sshClient, "/nonexistent_xyz")
            assertNull(entry)
        } finally {
            sshClient.close()
        }
    }

    // ── 1.7 getFileSize ──────────────────────────────────────────────────────

    @Test
    fun `getFileSize - returns correct byte count`() = runTest {
        val content = "1234567890".toByteArray()
        serverRoot.resolve("size.txt").toFile().writeBytes(content)
        val sshClient = openSshClient()
        try {
            val size = sftpClient.getFileSize(sshClient, "/size.txt")
            assertEquals(content.size.toLong(), size)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `getFileSize - non-existent path returns null`() = runTest {
        val sshClient = openSshClient()
        try {
            val size = sftpClient.getFileSize(sshClient, "/no_such_file.txt")
            assertNull(size)
        } finally {
            sshClient.close()
        }
    }

    // ── 1.8 resolveUploadDestination ─────────────────────────────────────────

    @Test
    fun `resolveUploadDestination - existing dir appends fileName`() = runTest {
        serverRoot.resolve("uploads").toFile().mkdir()
        val sshClient = openSshClient()
        try {
            val dest = sftpClient.resolveUploadDestination(sshClient, "/uploads", "file.txt")
            assertEquals("/uploads/file.txt", dest)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `resolveUploadDestination - non-existing dir full file path returned as-is`() = runTest {
        val sshClient = openSshClient()
        try {
            val dest = sftpClient.resolveUploadDestination(sshClient, "/dir/output.txt", "file.txt")
            assertEquals("/dir/output.txt", dest)
        } finally {
            sshClient.close()
        }
    }

    @Test
    fun `resolveUploadDestination - trailing slash appends fileName`() = runTest {
        serverRoot.resolve("uploads2").toFile().mkdir()
        val sshClient = openSshClient()
        try {
            val dest = sftpClient.resolveUploadDestination(sshClient, "/uploads2/", "photo.jpg")
            assertTrue(dest.endsWith("photo.jpg"), "Expected fileName appended, got: $dest")
        } finally {
            sshClient.close()
        }
    }

    // ── Phase 5 helper (ScpClient upload + SFTP interop) ─────────────────────

    @Test
    fun `upload then listDirectory - uploaded file appears via sftp`() = runTest {
        val content = "hello sftp".toByteArray()
        val localFile = File(clientDir.toFile(), "hello.txt").apply { writeBytes(content) }
        val sshClient = openSshClient()
        try {
            ScpClient().upload(sshClient, localFile, "/hello.txt").toList()
            val entries = sftpClient.listDirectory(sshClient, "/")
            assertTrue(entries.any { it.name == "hello.txt" })
        } finally {
            sshClient.close()
        }
    }
}
