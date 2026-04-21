package dev.nettools.android.ui.curl

import dev.nettools.android.domain.model.CurlRunOutput
import dev.nettools.android.domain.model.CurlRunRecord
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.model.CurlRunSummary
import dev.nettools.android.domain.repository.CurlRunRepository
import dev.nettools.android.domain.usecase.curl.ClearCurlLogsUseCase
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [CurlLogsViewModel], verifying sorted run emission and clearAll delegation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CurlLogsViewModelTest {

    private val curlRunRepository: CurlRunRepository = mockk()
    private val clearCurlLogs: ClearCurlLogsUseCase = mockk()
    private val recordsFlow = MutableStateFlow<List<CurlRunRecord>>(emptyList())
    private lateinit var viewModel: CurlLogsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun makeSummary(id: String, startedAt: Long) = CurlRunSummary(
        id = id,
        commandText = "curl https://example.com",
        normalizedCommandText = "curl https://example.com",
        startedAt = startedAt,
        status = CurlRunStatus.COMPLETED,
    )

    private fun makeRecord(id: String, startedAt: Long) = CurlRunRecord(
        summary = makeSummary(id, startedAt),
        output = CurlRunOutput(),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { curlRunRepository.observeAll() } returns recordsFlow
        coJustRun { clearCurlLogs() }
        viewModel = CurlLogsViewModel(curlRunRepository, clearCurlLogs)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Task 6.1 — runs emission ──────────────────────────────────────────────

    @Test
    fun `runs emits empty list when repository emits empty`() = runTest(testDispatcher) {
        recordsFlow.value = emptyList()
        advanceUntilIdle()

        val runs = viewModel.runs.first()
        assertEquals(0, runs.size)
    }

    @Test
    fun `runs emits records sorted newest-first by startedAt`() = runTest(testDispatcher) {
        recordsFlow.value = listOf(
            makeRecord("run-1", startedAt = 1_000L),
            makeRecord("run-3", startedAt = 3_000L),
            makeRecord("run-2", startedAt = 2_000L),
        )
        advanceUntilIdle()

        val runs = viewModel.runs.first()
        assertEquals(3, runs.size)
        assertEquals("run-3", runs[0].summary.id)
        assertEquals("run-2", runs[1].summary.id)
        assertEquals("run-1", runs[2].summary.id)
    }

    @Test
    fun `runs re-emits re-sorted list when repository emits update`() = runTest(testDispatcher) {
        recordsFlow.value = listOf(makeRecord("run-1", startedAt = 1_000L))
        advanceUntilIdle()

        assertEquals(1, viewModel.runs.first().size)

        recordsFlow.value = listOf(
            makeRecord("run-1", startedAt = 1_000L),
            makeRecord("run-2", startedAt = 5_000L),
        )
        advanceUntilIdle()

        val runs = viewModel.runs.first()
        assertEquals(2, runs.size)
        assertEquals("run-2", runs[0].summary.id)
        assertEquals("run-1", runs[1].summary.id)
    }

    // ── Task 6.2 — clearAll ───────────────────────────────────────────────────

    @Test
    fun `clearAll calls the clearCurlLogs use case`() = runTest(testDispatcher) {
        viewModel.clearAll()
        advanceUntilIdle()

        coVerify { clearCurlLogs() }
    }
}
