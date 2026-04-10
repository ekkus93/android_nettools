package dev.nettools.android.service

import dev.nettools.android.domain.model.CurlRunStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [CurlRunHolder].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CurlRunHolderTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Test
    fun `pending run is consumed once`() = testScope.runTest {
        val holder = CurlRunHolder(testScope)
        val pending = PendingCurlRunParams(
            runId = "run-1",
            rawCommandText = "curl --version",
            parsedCommand = dev.nettools.android.domain.model.ParsedCurlCommand(
                originalText = "curl --version",
                normalizedText = "curl --version",
                tokens = listOf("curl", "--version"),
                pathReferences = emptyList(),
            ),
            workspaceRootPath = "/tmp",
            loggingEnabled = false,
            stdoutByteCap = 10,
            stderrByteCap = 10,
        )

        holder.setPendingRun(pending)

        assertEquals("run-1", holder.consumePendingRun()?.runId)
        assertNull(holder.consumePendingRun())
    }

    @Test
    fun `start and output update live state`() = testScope.runTest {
        val holder = CurlRunHolder(testScope)

        holder.startRun("run-1")
        holder.appendOutput(isStdout = true, chunk = "hello")
        holder.updateStatus(status = CurlRunStatus.COMPLETED, exitCode = 0)

        val state = holder.liveState.value
        assertEquals("run-1", state.runId)
        assertEquals(CurlRunStatus.COMPLETED, state.status)
        assertEquals("hello", state.stdoutText)
        assertEquals(0, state.exitCode)
        assertNull(holder.activeRunId.value)
    }

    @Test
    fun `requestCancel stores target run id`() = testScope.runTest {
        val holder = CurlRunHolder(testScope)

        holder.requestCancel("run-9")

        assertEquals("run-9", holder.cancelRequestedRunId.value)
        assertTrue(holder.liveState.value.stdoutText.isEmpty())
    }
}
