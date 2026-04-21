package dev.nettools.android.ui.progress

import android.content.Context
import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.domain.model.TransferJob
import dev.nettools.android.domain.model.TransferStatus
import dev.nettools.android.service.TransferProgressHolder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
 * Unit tests for [ProgressViewModel], verifying cancel logic and navigate-back emission.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {

    private val context: Context = mockk(relaxed = true)
    private val holder: TransferProgressHolder = mockk(relaxed = true)
    private val activeJobsFlow = MutableStateFlow<List<TransferJob>>(emptyList())
    private lateinit var viewModel: ProgressViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun makeJob(
        id: String,
        status: TransferStatus = TransferStatus.IN_PROGRESS,
    ) = TransferJob(
        id = id,
        profileId = "",
        direction = TransferDirection.UPLOAD,
        localPath = "/local/file.txt",
        remotePath = "/remote/file.txt",
        status = status,
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { holder.activeJobs } returns activeJobsFlow
        every { holder.progress } returns MutableStateFlow(emptyMap())
        viewModel = ProgressViewModel(context, holder)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cancelJob - updates job status to CANCELLED in holder`() = runTest(testDispatcher) {
        viewModel.cancelJob("job-1", isPrimary = false)

        verify { holder.updateJobStatus("job-1", TransferStatus.CANCELLED) }
    }

    @Test
    fun `cancelJob - sends cancel intent to service`() = runTest(testDispatcher) {
        viewModel.cancelJob("job-1", isPrimary = false)

        verify { context.startService(any()) }
    }

    @Test
    fun `cancelJob isPrimary=true with no other active jobs emits navigateBack`() =
        runTest(testDispatcher) {
            activeJobsFlow.value = listOf(makeJob("job-1", TransferStatus.IN_PROGRESS))
            advanceUntilIdle()

            var navigated = false
            val collectJob = launch { viewModel.navigateBack.collect { navigated = true } }

            viewModel.cancelJob("job-1", isPrimary = true)
            advanceUntilIdle()

            assertEquals(true, navigated)
            collectJob.cancel()
        }

    @Test
    fun `cancelJob isPrimary=true with other active jobs does NOT emit navigateBack`() =
        runTest(testDispatcher) {
            activeJobsFlow.value = listOf(
                makeJob("job-1", TransferStatus.IN_PROGRESS),
                makeJob("job-2", TransferStatus.QUEUED),
            )
            advanceUntilIdle()

            var navigated = false
            val collectJob = launch { viewModel.navigateBack.collect { navigated = true } }

            viewModel.cancelJob("job-1", isPrimary = true)
            advanceUntilIdle()

            assertEquals(false, navigated)
            collectJob.cancel()
        }

    @Test
    fun `cancelJob isPrimary=false never emits navigateBack`() = runTest(testDispatcher) {
        activeJobsFlow.value = listOf(makeJob("job-1", TransferStatus.IN_PROGRESS))
        advanceUntilIdle()

        var navigated = false
        val collectJob = launch { viewModel.navigateBack.collect { navigated = true } }

        viewModel.cancelJob("job-1", isPrimary = false)
        advanceUntilIdle()

        assertEquals(false, navigated)
        collectJob.cancel()
    }
}
