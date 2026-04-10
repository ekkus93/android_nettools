package dev.nettools.android.ui.curl

import androidx.lifecycle.SavedStateHandle
import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.CurlRunOutput
import dev.nettools.android.domain.model.CurlRunRecord
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.model.CurlRunSummary
import dev.nettools.android.domain.repository.CurlRunRepository
import dev.nettools.android.domain.usecase.curl.CancelActiveCurlRunUseCase
import dev.nettools.android.domain.usecase.curl.ObserveActiveCurlRunUseCase
import dev.nettools.android.domain.usecase.curl.SaveCurlOutputUseCase
import dev.nettools.android.service.CurlRunHolder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [CurlResultsViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CurlResultsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ui state surfaces retained truncation flags and cleanup warnings`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        val repository = FakeCurlRunRepository(
            record = CurlRunRecord(
                summary = CurlRunSummary(
                    id = "run-1",
                    commandText = "curl https://example.com",
                    normalizedCommandText = "curl https://example.com",
                    startedAt = 1L,
                    finishedAt = 2L,
                    status = CurlRunStatus.FAILED,
                    exitCode = 23,
                    durationMillis = 123L,
                    loggingEnabled = true,
                    cleanupStatus = CurlCleanupStatus.FAILED,
                    cleanupWarning = "Remote cleanup warning",
                ),
                output = CurlRunOutput(
                    stdoutText = "stdout",
                    stderrText = "stderr",
                    stdoutBytes = 6,
                    stderrBytes = 6,
                    stdoutTruncated = true,
                    stderrTruncated = false,
                ),
            ),
        )
        val observeActiveCurlRun = ObserveActiveCurlRunUseCase(CurlRunHolder(this))
        val viewModel = CurlResultsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("runId" to "run-1")),
            repository = repository,
            observeActiveCurlRun = observeActiveCurlRun,
            cancelActiveCurlRun = mockk<CancelActiveCurlRunUseCase>(relaxed = true),
            saveCurlOutput = mockk<SaveCurlOutputUseCase>(relaxed = true),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.stdoutTruncated)
        assertEquals("Remote cleanup warning", viewModel.uiState.value.cleanupWarning)
        assertEquals(CurlCleanupStatus.FAILED, viewModel.uiState.value.cleanupStatus)
        assertEquals("stdout", viewModel.uiState.value.stdoutText)
    }

    @Test
    fun `ui state is marked missing when repository has no record and no live run`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        val observeActiveCurlRun = ObserveActiveCurlRunUseCase(CurlRunHolder(this))
        val viewModel = CurlResultsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("runId" to "run-1")),
            repository = FakeCurlRunRepository(record = null),
            observeActiveCurlRun = observeActiveCurlRun,
            cancelActiveCurlRun = mockk(relaxed = true),
            saveCurlOutput = mockk(relaxed = true),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isMissing)
        assertEquals("Command hidden because saved command history is disabled.", viewModel.uiState.value.commandText)
    }

    @Test
    fun `ui state prefers live run data when matching run is active`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        val holder = CurlRunHolder(this)
        holder.startRun(
            runId = "run-1",
            commandText = "curl https://live.example",
            effectiveCommandText = "curl --output /workspace/live.txt https://live.example",
        )
        holder.appendOutput(isStdout = false, chunk = "live stderr")
        val observeActiveCurlRun = ObserveActiveCurlRunUseCase(holder)
        val viewModel = CurlResultsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("runId" to "run-1")),
            repository = FakeCurlRunRepository(record = null),
            observeActiveCurlRun = observeActiveCurlRun,
            cancelActiveCurlRun = mockk(relaxed = true),
            saveCurlOutput = mockk(relaxed = true),
        )

        advanceUntilIdle()

        assertEquals("curl https://live.example", viewModel.uiState.value.commandText)
        assertEquals("curl --output /workspace/live.txt https://live.example", viewModel.uiState.value.effectiveCommandText)
        assertEquals("live stderr", viewModel.uiState.value.stderrText)
        assertEquals(CurlRunStatus.IN_PROGRESS, viewModel.uiState.value.status)
        assertEquals(false, viewModel.uiState.value.isMissing)
    }

    @Test
    fun `cancelRun requests cancellation and updates status when run is cancellable`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        val cancelActiveCurlRun = mockk<CancelActiveCurlRunUseCase>(relaxed = true)
        val holder = CurlRunHolder(this)
        holder.startRun(runId = "run-1", commandText = "curl https://example.com", effectiveCommandText = "curl https://example.com")
        val observeActiveCurlRun = ObserveActiveCurlRunUseCase(holder)
        val viewModel = CurlResultsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("runId" to "run-1")),
            repository = FakeCurlRunRepository(record = null),
            observeActiveCurlRun = observeActiveCurlRun,
            cancelActiveCurlRun = cancelActiveCurlRun,
            saveCurlOutput = mockk(relaxed = true),
        )
        advanceUntilIdle()

        viewModel.cancelRun()

        verify(exactly = 1) { cancelActiveCurlRun("run-1") }
        assertEquals(CurlRunStatus.CANCELLED, viewModel.uiState.value.status)
    }

    @Test
    fun `saveOutput reports saved path and clearSaveMessage resets it`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        val saveCurlOutput = mockk<SaveCurlOutputUseCase>()
        coEvery { saveCurlOutput("run-1", any()) } returns "/curl-output-run-1.txt"
        val viewModel = CurlResultsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("runId" to "run-1")),
            repository = FakeCurlRunRepository(
                record = retainedRecord(
                    output = CurlRunOutput(
                        stdoutText = "stdout",
                        stderrText = "stderr",
                        stdoutBytes = 6,
                        stderrBytes = 6,
                    ),
                ),
            ),
            observeActiveCurlRun = ObserveActiveCurlRunUseCase(CurlRunHolder(this)),
            cancelActiveCurlRun = mockk(relaxed = true),
            saveCurlOutput = saveCurlOutput,
        )
        advanceUntilIdle()

        viewModel.saveOutput()
        advanceUntilIdle()

        coVerify(exactly = 1) { saveCurlOutput("run-1", any()) }
        assertEquals("Saved output to /curl-output-run-1.txt", viewModel.uiState.value.saveMessage)
        viewModel.clearSaveMessage()
        assertEquals(null, viewModel.uiState.value.saveMessage)
    }

    @Test
    fun `saveOutput surfaces friendly error when saving fails`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        val saveCurlOutput = mockk<SaveCurlOutputUseCase>()
        coEvery { saveCurlOutput("run-1", any()) } throws IllegalStateException("Permission may have been revoked.")
        val viewModel = CurlResultsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("runId" to "run-1")),
            repository = FakeCurlRunRepository(record = retainedRecord()),
            observeActiveCurlRun = ObserveActiveCurlRunUseCase(CurlRunHolder(this)),
            cancelActiveCurlRun = mockk(relaxed = true),
            saveCurlOutput = saveCurlOutput,
        )
        advanceUntilIdle()

        viewModel.saveOutput()
        advanceUntilIdle()

        assertEquals(
            "Android no longer allows access to the selected file or destination.",
            viewModel.uiState.value.saveMessage,
        )
    }

    private fun retainedRecord(
        summary: CurlRunSummary = CurlRunSummary(
            id = "run-1",
            commandText = "curl https://example.com",
            normalizedCommandText = "curl https://example.com",
            startedAt = 1L,
            finishedAt = 2L,
            status = CurlRunStatus.COMPLETED,
            exitCode = 0,
            durationMillis = 123L,
            loggingEnabled = true,
        ),
        output: CurlRunOutput = CurlRunOutput(
            stdoutText = "stdout",
            stderrText = "stderr",
            stdoutBytes = 6,
            stderrBytes = 6,
        ),
    ): CurlRunRecord = CurlRunRecord(summary = summary, output = output)
}

private class FakeCurlRunRepository(
    record: CurlRunRecord?,
) : CurlRunRepository {
    private val flow = MutableStateFlow(record)

    override fun observeAll(): Flow<List<CurlRunRecord>> = emptyFlow()

    override fun observeById(runId: String): Flow<CurlRunRecord?> = flow

    override suspend fun getById(runId: String): CurlRunRecord? = flow.value

    override suspend fun upsert(record: CurlRunRecord) = Unit

    override suspend fun upsertSummary(summary: CurlRunSummary) = Unit

    override suspend fun appendOutput(
        runId: String,
        stream: dev.nettools.android.domain.model.CurlOutputStream,
        text: String,
        byteCap: Int,
    ) = Unit

    override suspend fun updateStatus(
        runId: String,
        status: CurlRunStatus,
        finishedAt: Long?,
        exitCode: Int?,
        durationMillis: Long?,
        cleanupWarning: String?,
        effectiveCommandText: String?,
        cleanupStatus: CurlCleanupStatus?,
    ) = Unit

    override suspend fun clearAll() = Unit
}
