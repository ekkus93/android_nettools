package dev.nettools.android.domain.usecase.curl

import android.content.Context
import dev.nettools.android.domain.model.ParsedCurlCommand
import dev.nettools.android.service.CurlRunHolder
import dev.nettools.android.service.PendingCurlRunParams
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [DispatchPendingCurlRunUseCase], verifying holder interaction
 * and correct ordering with foreground service start.
 */
class DispatchPendingCurlRunUseCaseTest {

    // relaxed = true ensures startService()/startForegroundService() return safe defaults.
    private val context: Context = mockk(relaxed = true)
    private val curlRunHolder: CurlRunHolder = mockk()
    private lateinit var useCase: DispatchPendingCurlRunUseCase

    private val parsedCommand = ParsedCurlCommand(
        originalText = "curl https://example.com",
        normalizedText = "curl https://example.com",
        tokens = listOf("curl", "https://example.com"),
        pathReferences = emptyList(),
    )

    private val pendingRun = PendingCurlRunParams(
        runId = "run-001",
        rawCommandText = "curl https://example.com",
        parsedCommand = parsedCommand,
        workspaceRootPath = "/workspace",
        loggingEnabled = false,
        stdoutByteCap = 1024 * 1024,
        stderrByteCap = 512 * 1024,
    )

    @BeforeEach
    fun setUp() {
        justRun { curlRunHolder.setPendingRun(any()) }
        useCase = DispatchPendingCurlRunUseCase(context, curlRunHolder)
    }

    // ── Task 8.1 — holder interaction ────────────────────────────────────────

    @Test
    fun `invoke calls curlRunHolder_setPendingRun with the exact pendingRun`() {
        useCase(pendingRun)

        verify { curlRunHolder.setPendingRun(pendingRun) }
    }

    // ── Task 8.2 — service start ─────────────────────────────────────────────

    @Test
    fun `invoke calls context_startForegroundService after setting pending run`() {
        val callOrder = mutableListOf<String>()
        every { curlRunHolder.setPendingRun(any()) } answers {
            callOrder.add("setPendingRun")
        }
        // ContextCompat.startForegroundService calls startForegroundService (API 26+)
        // or startService (lower APIs, Build.VERSION.SDK_INT == 0 in JVM tests).
        every { context.startForegroundService(any()) } answers {
            callOrder.add("startForeground")
            null
        }
        every { context.startService(any()) } answers {
            callOrder.add("startService")
            null
        }

        useCase(pendingRun)

        assertEquals("setPendingRun", callOrder.firstOrNull(), "setPendingRun should be called first")
        assertTrue(callOrder.size >= 2, "Service start should be called after setPendingRun")
        assertTrue(
            callOrder[1] == "startForeground" || callOrder[1] == "startService",
            "Second call should start the service but was: ${callOrder.getOrNull(1)}",
        )
    }
}
