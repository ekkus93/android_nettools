package dev.nettools.android.data.curl

import dev.nettools.android.domain.model.CurlOutputStream
import dev.nettools.android.domain.model.ParsedCurlCommand
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [ProcessCurlExecutor].
 */
class ProcessCurlExecutorTest {

    private val binaryProvider = object : CurlBinaryProvider {
        override suspend fun getRuntime(): CurlRuntime = CurlRuntime(executablePath = "curl")
    }
    private val tempDirectory: String = requireNotNull(System.getProperty("java.io.tmpdir"))

    @Test
    fun `execute streams stdout and returns exit code`() = runTest {
        val executor = ProcessCurlExecutor(binaryProvider)
        val chunks = mutableListOf<Pair<CurlOutputStream, String>>()

        val result = executor.execute(
            request = CurlExecutionRequest(
                runId = "run-1",
                parsedCommand = ParsedCurlCommand(
                    originalText = "curl --version",
                    normalizedText = "curl --version",
                    tokens = listOf("curl", "--version"),
                    pathReferences = emptyList(),
                ),
                workspaceDirectory = tempDirectory,
            ),
        ) { chunk ->
            chunks += chunk.stream to chunk.text
        }

        assertEquals(0, result.exitCode)
        assertTrue(chunks.any { it.first == CurlOutputStream.STDOUT && it.second.contains("curl") })
    }
}
