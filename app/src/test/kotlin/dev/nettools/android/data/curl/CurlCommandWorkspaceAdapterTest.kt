package dev.nettools.android.data.curl

import dev.nettools.android.domain.model.CurlPathReferenceRole
import dev.nettools.android.domain.model.ParsedCurlCommand
import dev.nettools.android.domain.model.ParsedCurlPathReference
import dev.nettools.android.domain.repository.WorkspaceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/**
 * Unit tests for [CurlCommandWorkspaceAdapter].
 */
class CurlCommandWorkspaceAdapterTest {

    private val workspaceRepository: WorkspaceRepository = mockk()

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `prepareForExecution rewrites workspace-backed file arguments`() = runTest {
        val outputPath = tempDir.resolve("downloads/out.txt").toFile().apply {
            requireNotNull(parentFile).mkdirs()
            writeText("partial")
        }
        coEvery { workspaceRepository.resolveLocalPath("/payload.json") } returns tempDir.resolve("payload.json").toString()
        coEvery { workspaceRepository.resolveLocalPath("/downloads/out.txt") } returns outputPath.absolutePath
        val adapter = CurlCommandWorkspaceAdapter(workspaceRepository)
        val command = ParsedCurlCommand(
            originalText = "curl --data @/payload.json --output /downloads/out.txt https://example.com",
            normalizedText = "curl --data @/payload.json --output /downloads/out.txt https://example.com",
            tokens = listOf("curl", "--data", "@/payload.json", "--output", "/downloads/out.txt", "https://example.com"),
            pathReferences = listOf(
                ParsedCurlPathReference(
                    originalPath = "/payload.json",
                    normalizedPath = "/payload.json",
                    role = CurlPathReferenceRole.PAYLOAD_FILE,
                ),
                ParsedCurlPathReference(
                    originalPath = "/downloads/out.txt",
                    normalizedPath = "/downloads/out.txt",
                    role = CurlPathReferenceRole.OUTPUT_FILE,
                ),
            ),
        )

        val prepared = adapter.prepareForExecution(command)

        assertEquals("@${tempDir.resolve("payload.json")}", prepared.command.tokens[2])
        assertEquals(outputPath.absolutePath, prepared.command.tokens[4])
        assertEquals(
            "curl --data @${tempDir.resolve("payload.json")} --output ${outputPath.absolutePath} https://example.com",
            prepared.effectiveCommandText,
        )
        assertEquals(listOf(outputPath.absolutePath), prepared.cleanupTargets)
        assertEquals(outputPath.absolutePath, prepared.localPathMap["/downloads/out.txt"])
    }

    @Test
    fun `validate reports missing input files and invalid output directories`() = runTest {
        val missingFile = tempDir.resolve("missing.txt").toString()
        val outputPath = tempDir.resolve("missing-dir/out.txt").toString()
        coEvery { workspaceRepository.resolveLocalPath("/missing.txt") } returns missingFile
        coEvery { workspaceRepository.resolveLocalPath("/missing-dir/out.txt") } returns outputPath
        val adapter = CurlCommandWorkspaceAdapter(workspaceRepository)
        val command = ParsedCurlCommand(
            originalText = "curl --data @/missing.txt --output /missing-dir/out.txt https://example.com",
            normalizedText = "curl --data @/missing.txt --output /missing-dir/out.txt https://example.com",
            tokens = listOf("curl"),
            pathReferences = listOf(
                ParsedCurlPathReference(
                    originalPath = "/missing.txt",
                    normalizedPath = "/missing.txt",
                    role = CurlPathReferenceRole.PAYLOAD_FILE,
                ),
                ParsedCurlPathReference(
                    originalPath = "/missing-dir/out.txt",
                    normalizedPath = "/missing-dir/out.txt",
                    role = CurlPathReferenceRole.OUTPUT_FILE,
                ),
            ),
        )

        val errors = adapter.validate(command)

        assertEquals(
            listOf(
                "Workspace file not found: /missing.txt",
                "Workspace directory does not exist for /missing-dir/out.txt",
            ),
            errors.map { it.message },
        )
    }

    @Test
    fun `cleanupPartialOutputs records failed cleanup when a target cannot be deleted`() {
        val stuckDirectory = tempDir.resolve("stuck").toFile().apply {
            mkdirs()
            resolve("child.txt").writeText("keep")
        }
        val removableFile = tempDir.resolve("partial.txt").toFile().apply {
            writeText("partial")
        }
        val adapter = CurlCommandWorkspaceAdapter(workspaceRepository)
        val prepared = PreparedCurlCommand(
            command = ParsedCurlCommand(
                originalText = "curl",
                normalizedText = "curl",
                tokens = listOf("curl"),
                pathReferences = emptyList(),
            ),
            cleanupTargets = listOf(removableFile.absolutePath, stuckDirectory.absolutePath),
            localPathMap = emptyMap(),
        )

        val result = adapter.cleanupPartialOutputs(prepared)

        assertEquals(dev.nettools.android.domain.model.CurlCleanupStatus.FAILED, result.status)
        assertTrue(result.warning?.contains(stuckDirectory.absolutePath) == true)
        assertTrue(!removableFile.exists())
    }

    @Test
    fun `cleanupPartialOutputs records success when outputs are removed`() {
        val removableFile = tempDir.resolve("partial.txt").toFile().apply {
            writeText("partial")
        }
        val adapter = CurlCommandWorkspaceAdapter(workspaceRepository)
        val prepared = PreparedCurlCommand(
            command = ParsedCurlCommand(
                originalText = "curl",
                normalizedText = "curl",
                tokens = listOf("curl"),
                pathReferences = emptyList(),
            ),
            cleanupTargets = listOf(removableFile.absolutePath),
            localPathMap = emptyMap(),
        )

        val result = adapter.cleanupPartialOutputs(prepared)

        assertEquals(dev.nettools.android.domain.model.CurlCleanupStatus.SUCCEEDED, result.status)
        assertNull(result.warning)
        assertTrue(!removableFile.exists())
    }
}
