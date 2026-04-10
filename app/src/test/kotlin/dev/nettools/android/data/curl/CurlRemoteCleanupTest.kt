package dev.nettools.android.data.curl

import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.ParsedCurlCommand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/**
 * Tests for remote partial-upload cleanup planning and execution.
 */
class CurlRemoteCleanupTest {

    @TempDir
    lateinit var tempDir: Path

    private val planner = CurlRemoteCleanupPlanner()

    @Test
    fun `plan creates HTTP DELETE cleanup command`() {
        val plan = requireNotNull(
            planner.plan(
                command = commandOf(
                    "curl",
                    "-T",
                    "/workspace/upload.txt",
                    "--user",
                    "demo:secret",
                    "https://example.com/uploads/upload.txt",
                ),
            ),
        )

        assertEquals(emptyList<String>(), plan.warnings)
        assertEquals(1, plan.commands.size)
        assertEquals(
            listOf(
                "curl",
                "--user",
                "demo:secret",
                "--request",
                "DELETE",
                "https://example.com/uploads/upload.txt",
            ),
            plan.commands.single().tokens,
        )
    }

    @Test
    fun `plan creates SFTP rm cleanup command`() {
        val plan = requireNotNull(
            planner.plan(
                command = commandOf(
                    "curl",
                    "--upload-file",
                    "/workspace/upload.txt",
                    "sftp://demo@example.com/home/demo/upload.txt",
                ),
            ),
        )

        assertEquals(emptyList<String>(), plan.warnings)
        assertEquals(
            listOf(
                "curl",
                "--quote",
                "rm /home/demo/upload.txt",
                "sftp://demo@example.com/",
            ),
            plan.commands.single().tokens,
        )
    }

    @Test
    fun `plan warns for unsupported protocol`() {
        val plan = requireNotNull(
            planner.plan(
                command = commandOf(
                    "curl",
                    "-T",
                    "/workspace/upload.txt",
                    "scp://example.com/home/demo/upload.txt",
                ),
            ),
        )

        assertTrue(plan.commands.isEmpty())
        assertTrue(plan.warnings.single().contains("not supported"))
    }

    @Test
    fun `executor surfaces warning when cleanup command fails`() = kotlinx.coroutines.test.runTest {
        val script = tempDir.resolve("cleanup.sh").toFile().apply {
            writeText(
                """
                #!/bin/sh
                echo "cleanup failed" >&2
                exit 1
                """.trimIndent(),
            )
            setExecutable(true)
        }
        val executor = CurlRemoteCleanupExecutor(
            binaryProvider = object : CurlBinaryProvider {
                override suspend fun getRuntime(): CurlRuntime = CurlRuntime(executablePath = script.absolutePath)
            },
        )

        val result = executor.execute(
            plan = CurlRemoteCleanupPlan(
                commands = listOf(
                    CurlRemoteCleanupCommand(
                        target = "https://example.com/uploads/upload.txt",
                        tokens = listOf("curl", "--request", "DELETE", "https://example.com/uploads/upload.txt"),
                    ),
                ),
            ),
            workspaceDirectory = tempDir.toString(),
        )

        assertEquals(CurlCleanupStatus.FAILED, result.status)
        val warning = requireNotNull(result.warning)
        assertTrue(warning.contains("Curl could not confirm remote partial-upload cleanup."))
        assertTrue(warning.contains("cleanup failed"))
    }

    private fun commandOf(vararg tokens: String): ParsedCurlCommand = ParsedCurlCommand(
        originalText = tokens.joinToString(separator = " "),
        normalizedText = tokens.joinToString(separator = " "),
        tokens = tokens.toList(),
        pathReferences = emptyList(),
    )
}
