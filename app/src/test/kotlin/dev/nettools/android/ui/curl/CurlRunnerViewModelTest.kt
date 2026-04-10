package dev.nettools.android.ui.curl

import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.model.CurlValidationError
import dev.nettools.android.domain.model.ParsedCurlCommand
import dev.nettools.android.domain.usecase.curl.CurlStartResult
import dev.nettools.android.domain.usecase.curl.DispatchPendingCurlRunUseCase
import dev.nettools.android.domain.usecase.curl.ObserveActiveCurlRunUseCase
import dev.nettools.android.domain.usecase.curl.StartCurlRunUseCase
import dev.nettools.android.service.CurlLiveRunState
import dev.nettools.android.service.PendingCurlRunParams
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [CurlRunnerViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CurlRunnerViewModelTest {

    private val startCurlRun: StartCurlRunUseCase = mockk()
    private val dispatchPendingCurlRun: DispatchPendingCurlRunUseCase = mockk(relaxed = true)
    private val observeActiveCurlRun: ObserveActiveCurlRunUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `runCommand dispatches prepared run and navigates to results`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        every { observeActiveCurlRun() } returns MutableStateFlow(CurlLiveRunState())
        val pendingRun = pendingRunParams(runId = "run-123")
        coEvery { startCurlRun("curl https://example.com") } returns CurlStartResult(pendingRun = pendingRun)

        val viewModel = createViewModel()
        val navigation = async { viewModel.navigateToResults.first() }
        viewModel.onCommandChange("curl https://example.com")
        viewModel.runCommand()
        advanceUntilIdle()

        verify(exactly = 1) { dispatchPendingCurlRun(pendingRun) }
        assertEquals("run-123", navigation.await())
    }

    @Test
    fun `runCommand shows validation messages when start use case rejects input`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        every { observeActiveCurlRun() } returns MutableStateFlow(CurlLiveRunState())
        coEvery { startCurlRun("curl --bogus") } returns CurlStartResult(
            errors = listOf(CurlValidationError(message = "Unknown option: --bogus", token = "--bogus")),
        )

        val viewModel = createViewModel()
        viewModel.onCommandChange("curl --bogus")
        viewModel.runCommand()
        advanceUntilIdle()

        assertEquals(listOf("Unknown option: --bogus"), viewModel.uiState.value.validationMessages)
        verify(exactly = 0) { dispatchPendingCurlRun(any()) }
    }

    @Test
    fun `runCommand refuses to start a second active run`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        every { observeActiveCurlRun() } returns MutableStateFlow(
            CurlLiveRunState(
                runId = "active-run",
                commandText = "curl https://busy.example",
                status = CurlRunStatus.IN_PROGRESS,
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.runCommand()

        assertEquals("Only one curl run can be active at a time.", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { startCurlRun(any()) }
        verify(exactly = 0) { dispatchPendingCurlRun(any()) }
    }

    private fun createViewModel(): CurlRunnerViewModel {
        return CurlRunnerViewModel(
            startCurlRun = startCurlRun,
            dispatchPendingCurlRun = dispatchPendingCurlRun,
            observeActiveCurlRun = observeActiveCurlRun,
        )
    }

    private fun pendingRunParams(runId: String): PendingCurlRunParams {
        return PendingCurlRunParams(
            runId = runId,
            rawCommandText = "curl https://example.com",
            parsedCommand = ParsedCurlCommand(
                originalText = "curl https://example.com",
                normalizedText = "curl https://example.com",
                tokens = listOf("curl", "https://example.com"),
                pathReferences = emptyList(),
            ),
            workspaceRootPath = "/workspace",
            loggingEnabled = false,
            stdoutByteCap = 1024,
            stderrByteCap = 1024,
        )
    }
}
