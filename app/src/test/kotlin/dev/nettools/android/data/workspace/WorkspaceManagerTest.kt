package dev.nettools.android.data.workspace

import android.content.Context
import dev.nettools.android.data.curl.CurlCommandWorkspaceAdapter
import dev.nettools.android.domain.model.CurlSettings
import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.CurlPathReferenceRole
import dev.nettools.android.domain.model.ParsedCurlCommand
import dev.nettools.android.domain.model.ParsedCurlPathReference
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
    fun `workspace-backed download cleanup removes failed partial outputs`() = runTest {
        every { context.filesDir } returns tempDir.toFile()
        coEvery { settingsRepository.getSettings() } returns CurlSettings(
            workspaceRootPath = tempDir.resolve("workspace").toString(),
        )
        val manager = WorkspaceManager(context, settingsRepository)
        manager.createDirectory("/downloads")
        val adapter = CurlCommandWorkspaceAdapter(manager)
        val command = ParsedCurlCommand(
            originalText = "curl --output /downloads/out.txt https://example.com/file.txt",
            normalizedText = "curl --output /downloads/out.txt https://example.com/file.txt",
            tokens = listOf("curl", "--output", "/downloads/out.txt", "https://example.com/file.txt"),
            pathReferences = listOf(
                ParsedCurlPathReference(
                    originalPath = "/downloads/out.txt",
                    normalizedPath = "/downloads/out.txt",
                    role = CurlPathReferenceRole.OUTPUT_FILE,
                ),
            ),
        )

        val prepared = adapter.prepareForExecution(command)
        val partialFile = java.io.File(prepared.cleanupTargets.single()).apply {
            parentFile?.mkdirs()
            writeText("partial download")
        }

        val result = adapter.cleanupPartialOutputs(prepared)

        assertEquals(CurlCleanupStatus.SUCCEEDED, result.status)
        assertFalse(partialFile.exists())
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
