package dev.nettools.android.data.workspace

import android.content.Context
import dev.nettools.android.domain.model.CurlSettings
import dev.nettools.android.domain.repository.CurlSettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path

/**
 * Unit tests for [WorkspaceManager].
 */
class WorkspaceManagerTest {

    private val context: Context = mockk()
    private val settingsRepository: CurlSettingsRepository = mockk()

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `getWorkspaceRootPath falls back to app files dir`() = runTest {
        every { context.filesDir } returns tempDir.toFile()
        coEvery { settingsRepository.getSettings() } returns CurlSettings()
        val manager = WorkspaceManager(context, settingsRepository)

        val rootPath = manager.getWorkspaceRootPath()

        assertTrue(rootPath.endsWith("curl-workspace"))
    }

    @Test
    fun `normalizePath prevents traversal above root`() {
        assertEquals("/etc/passwd", WorkspacePathResolver.normalize("../../etc/passwd"))
        assertEquals("/", WorkspacePathResolver.normalize("../.."))
    }

    @Test
    fun `workspace operations create rename move and delete entries`() = runTest {
        every { context.filesDir } returns tempDir.toFile()
        coEvery { settingsRepository.getSettings() } returns CurlSettings(
            workspaceRootPath = tempDir.resolve("workspace").toString(),
        )
        val manager = WorkspaceManager(context, settingsRepository)

        manager.createDirectory("/docs")
        val listedAfterCreate = manager.list("/")
        assertEquals(1, listedAfterCreate.size)
        assertEquals("docs", listedAfterCreate.single().name)
        assertTrue(listedAfterCreate.single().isDirectory)

        val renamed = manager.rename("/docs", "reports")
        assertEquals("/reports", renamed.path)

        manager.createDirectory("/archive")
        val moved = manager.move("/reports", "/archive")
        assertEquals("/archive/reports", moved.path)

        manager.delete("/archive")
        val finalEntries = manager.list("/")
        assertFalse(finalEntries.any { it.name == "archive" })
    }

    @Test
    fun `workspace import and export copy file contents`() = runTest {
        every { context.filesDir } returns tempDir.toFile()
        coEvery { settingsRepository.getSettings() } returns CurlSettings(
            workspaceRootPath = tempDir.resolve("workspace").toString(),
        )
        val manager = WorkspaceManager(context, settingsRepository)

        manager.createDirectory("/imports")
        val imported = manager.importFile(
            targetDirectoryPath = "/imports",
            fileName = "sample.txt",
            inputStream = ByteArrayInputStream("hello workspace".toByteArray()),
        )
        assertEquals("/imports/sample.txt", imported.path)

        val output = ByteArrayOutputStream()
        manager.exportFile(path = imported.path, outputStream = output)
        assertEquals("hello workspace", output.toString())
    }

    @Test
    fun `workspace import sanitizes incoming file names`() = runTest {
        every { context.filesDir } returns tempDir.toFile()
        coEvery { settingsRepository.getSettings() } returns CurlSettings(
            workspaceRootPath = tempDir.resolve("workspace").toString(),
        )
        val manager = WorkspaceManager(context, settingsRepository)

        manager.importFile(
            targetDirectoryPath = "/",
            fileName = "../secret.txt",
            inputStream = ByteArrayInputStream("secret".toByteArray()),
        )

        val entries = manager.list("/")
        assertEquals("secret.txt", entries.single().name)
        assertFalse(tempDir.resolve("secret.txt").toFile().exists())
    }

    @Test
    fun `workspace import rejects blank file names`() {
        every { context.filesDir } returns tempDir.toFile()
        coEvery { settingsRepository.getSettings() } returns CurlSettings(
            workspaceRootPath = tempDir.resolve("workspace").toString(),
        )
        val manager = WorkspaceManager(context, settingsRepository)

        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                manager.importFile(
                    targetDirectoryPath = "/",
                    fileName = "   ",
                    inputStream = ByteArrayInputStream(ByteArray(0)),
                )
            }
        }
    }
}
