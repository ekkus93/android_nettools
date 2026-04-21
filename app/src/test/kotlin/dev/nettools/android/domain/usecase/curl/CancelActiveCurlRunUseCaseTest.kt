package dev.nettools.android.domain.usecase.curl

import android.content.Context
import dev.nettools.android.service.CurlForegroundService
import dev.nettools.android.service.CurlRunHolder
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [CancelActiveCurlRunUseCase], verifying holder interaction and service dispatch.
 *
 * Note: In JVM unit tests with `returnDefaultValues = true`, [android.content.Intent] is a stub
 * where setters are no-ops and getters return null. Intent content tests would require Robolectric.
 * We verify the service-start call and rely on correct constants for action/extra keys.
 */
class CancelActiveCurlRunUseCaseTest {

    // relaxed = true ensures all context methods return safe defaults (avoids ClassCastException
    // when startService() / startForegroundService() return ComponentName, not Unit).
    private val context: Context = mockk(relaxed = true)
    private val holder: CurlRunHolder = mockk()
    private lateinit var useCase: CancelActiveCurlRunUseCase

    @BeforeEach
    fun setUp() {
        justRun { holder.requestCancel(any()) }
        useCase = CancelActiveCurlRunUseCase(context, holder)
    }

    // ── Task 7.1 — holder interaction ────────────────────────────────────────

    @Test
    fun `invoke calls holder_requestCancel with the runId`() {
        useCase("run-abc")

        verify { holder.requestCancel("run-abc") }
    }

    @Test
    fun `invoke starts foreground service after cancelling`() {
        useCase("run-abc")

        // ContextCompat.startForegroundService calls startForegroundService on API 26+
        // or startService on lower APIs (Build.VERSION.SDK_INT == 0 in JVM tests).
        try {
            verify { context.startForegroundService(any()) }
        } catch (_: AssertionError) {
            verify { context.startService(any()) }
        }
    }

    // ── Task 7.2 — constants verification ────────────────────────────────────

    @Test
    fun `ACTION_CANCEL constant has expected value`() {
        assertEquals("dev.nettools.android.CANCEL_CURL_RUN", CurlForegroundService.ACTION_CANCEL)
    }

    @Test
    fun `EXTRA_RUN_ID constant has expected value`() {
        assertEquals("run_id", CurlForegroundService.EXTRA_RUN_ID)
    }
}
