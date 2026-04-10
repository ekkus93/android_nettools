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
import io.mockk.mockk
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
