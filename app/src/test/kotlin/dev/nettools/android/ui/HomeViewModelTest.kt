package dev.nettools.android.ui

import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.domain.model.TransferJob
import dev.nettools.android.domain.model.TransferStatus
import dev.nettools.android.service.TransferProgressHolder
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [HomeViewModel], verifying active transfer count and first job ID.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val holder: TransferProgressHolder = mockk()
    private val activeJobsFlow = MutableStateFlow<List<TransferJob>>(emptyList())
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun makeJob(id: String, status: TransferStatus) = TransferJob(
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
        viewModel = HomeViewModel(holder)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `activeTransferCount is 0 when no active jobs`() = runTest(testDispatcher) {
        activeJobsFlow.value = emptyList()
        advanceUntilIdle()

        assertEquals(0, viewModel.activeTransferCount.first())
    }

    @Test
    fun `activeTransferCount is 1 for one in-progress job`() = runTest(testDispatcher) {
        activeJobsFlow.value = listOf(makeJob("j1", TransferStatus.IN_PROGRESS))
        advanceUntilIdle()

        assertEquals(1, viewModel.activeTransferCount.first())
    }

    @Test
    fun `activeTransferCount counts queued and paused as active`() = runTest(testDispatcher) {
        activeJobsFlow.value = listOf(
            makeJob("j1", TransferStatus.IN_PROGRESS),
            makeJob("j2", TransferStatus.QUEUED),
            makeJob("j3", TransferStatus.PAUSED),
            makeJob("j4", TransferStatus.COMPLETED),
            makeJob("j5", TransferStatus.FAILED),
        )
        advanceUntilIdle()

        assertEquals(3, viewModel.activeTransferCount.first())
    }

    @Test
    fun `firstActiveJobId is null when no active jobs`() = runTest(testDispatcher) {
        activeJobsFlow.value = emptyList()
        advanceUntilIdle()

        assertNull(viewModel.firstActiveJobId.first())
    }

    @Test
    fun `firstActiveJobId returns id of first active job`() = runTest(testDispatcher) {
        activeJobsFlow.value = listOf(
            makeJob("first", TransferStatus.IN_PROGRESS),
            makeJob("second", TransferStatus.QUEUED),
        )
        advanceUntilIdle()

        assertEquals("first", viewModel.firstActiveJobId.first())
    }

    @Test
    fun `firstActiveJobId ignores completed jobs`() = runTest(testDispatcher) {
        activeJobsFlow.value = listOf(
            makeJob("done", TransferStatus.COMPLETED),
            makeJob("active", TransferStatus.IN_PROGRESS),
        )
        advanceUntilIdle()

        assertEquals("active", viewModel.firstActiveJobId.first())
    }
}
