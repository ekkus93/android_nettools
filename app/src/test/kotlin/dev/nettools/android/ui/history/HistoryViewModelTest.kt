package dev.nettools.android.ui.history

import dev.nettools.android.domain.model.HistoryStatus
import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.domain.model.TransferHistoryEntry
import dev.nettools.android.domain.repository.TransferHistoryRepository
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [HistoryViewModel], verifying filter logic, detail dialog state,
 * and clear-all delegation to the repository.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val repository: TransferHistoryRepository = mockk()
    private val allEntries = MutableStateFlow<List<TransferHistoryEntry>>(emptyList())
    private lateinit var viewModel: HistoryViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun makeEntry(
        id: String,
        fileName: String = "file.txt",
        host: String = "server.local",
        remoteDir: String = "/home/user",
    ) = TransferHistoryEntry(
        id = id,
        timestamp = 0L,
        direction = TransferDirection.UPLOAD,
        host = host,
        username = "user",
        fileName = fileName,
        remoteDir = remoteDir,
        fileSizeBytes = 100L,
        status = HistoryStatus.SUCCESS,
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAll() } returns allEntries
        coJustRun { repository.clearAll() }
        viewModel = HistoryViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── filter / searchQuery ──────────────────────────────────────────────────

    @Test
    fun `empty query - returns all entries`() = runTest(testDispatcher) {
        allEntries.value = listOf(
            makeEntry("1", fileName = "alpha.zip"),
            makeEntry("2", fileName = "beta.tar"),
        )
        advanceUntilIdle()

        val history = viewModel.history.first()

        assertEquals(2, history.size)
    }

    @Test
    fun `search query - filters by file name case-insensitively`() = runTest(testDispatcher) {
        allEntries.value = listOf(
            makeEntry("1", fileName = "Report_2024.pdf"),
            makeEntry("2", fileName = "video.mp4"),
        )
        advanceUntilIdle()

        viewModel.onSearchQueryChange("report")
        advanceUntilIdle()

        val history = viewModel.history.first()
        assertEquals(1, history.size)
        assertEquals("Report_2024.pdf", history.first().fileName)
    }

    @Test
    fun `search query - filters by host name`() = runTest(testDispatcher) {
        allEntries.value = listOf(
            makeEntry("1", host = "prod.example.com"),
            makeEntry("2", host = "dev.internal"),
        )
        advanceUntilIdle()

        viewModel.onSearchQueryChange("prod")
        advanceUntilIdle()

        val history = viewModel.history.first()
        assertEquals(1, history.size)
        assertEquals("prod.example.com", history.first().host)
    }

    @Test
    fun `search query - filters by remote dir`() = runTest(testDispatcher) {
        allEntries.value = listOf(
            makeEntry("1", remoteDir = "/var/www/html"),
            makeEntry("2", remoteDir = "/home/user/docs"),
        )
        advanceUntilIdle()

        viewModel.onSearchQueryChange("/var")
        advanceUntilIdle()

        val history = viewModel.history.first()
        assertEquals(1, history.size)
        assertTrue(history.first().remoteDir.startsWith("/var"))
    }

    @Test
    fun `search query - returns empty list when nothing matches`() = runTest(testDispatcher) {
        allEntries.value = listOf(makeEntry("1", fileName = "cat.txt"))
        advanceUntilIdle()

        viewModel.onSearchQueryChange("XYZZY_NO_MATCH")
        advanceUntilIdle()

        assertTrue(viewModel.history.first().isEmpty())
    }

    @Test
    fun `clearing search query - restores all entries`() = runTest(testDispatcher) {
        allEntries.value = listOf(
            makeEntry("1", fileName = "a.zip"),
            makeEntry("2", fileName = "b.zip"),
        )
        viewModel.onSearchQueryChange("a.zip")
        advanceUntilIdle()
        assertEquals(1, viewModel.history.first().size)

        viewModel.onSearchQueryChange("")
        advanceUntilIdle()
        assertEquals(2, viewModel.history.first().size)
    }

    // ── detail dialog state ───────────────────────────────────────────────────

    @Test
    fun `onEntrySelected - sets selectedEntry`() = runTest(testDispatcher) {
        val entry = makeEntry("selected")

        viewModel.onEntrySelected(entry)

        assertNotNull(viewModel.selectedEntry.value)
        assertEquals("selected", viewModel.selectedEntry.value?.id)
    }

    @Test
    fun `onDetailDismissed - clears selectedEntry`() = runTest(testDispatcher) {
        viewModel.onEntrySelected(makeEntry("x"))
        viewModel.onDetailDismissed()

        assertNull(viewModel.selectedEntry.value)
    }

    // ── clearAll ──────────────────────────────────────────────────────────────

    @Test
    fun `clearAll - delegates to repository`() = runTest(testDispatcher) {
        viewModel.clearAll()
        advanceUntilIdle()

        coVerify { repository.clearAll() }
    }
}
