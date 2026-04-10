package dev.nettools.android.ui.workspace

import dev.nettools.android.domain.model.WorkspaceEntry
import dev.nettools.android.domain.repository.WorkspaceRepository
import dev.nettools.android.domain.usecase.curl.ExportWorkspaceDocumentUseCase
import dev.nettools.android.domain.usecase.curl.ImportWorkspaceDocumentUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.io.OutputStream

/**
 * Unit tests for [WorkspaceBrowserViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceBrowserViewModelTest {

    private val workspaceRepository = FakeWorkspaceRepository()
    private val importWorkspaceDocument: ImportWorkspaceDocumentUseCase = mockk(relaxed = true)
    private val exportWorkspaceDocument: ExportWorkspaceDocumentUseCase = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `importFiles delegates each selected document through the use case`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.importFiles(listOf("content://docs/one", "content://docs/two"))
        advanceUntilIdle()

        coVerify(exactly = 1) { importWorkspaceDocument(targetDirectoryPath = "/", documentUri = "content://docs/one") }
        coVerify(exactly = 1) { importWorkspaceDocument(targetDirectoryPath = "/", documentUri = "content://docs/two") }
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `exportFile delegates workspace path and destination uri through the use case`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportFile(path = "/logs/output.txt", destinationUri = "content://docs/export")
        advanceUntilIdle()

        coVerify(exactly = 1) {
            exportWorkspaceDocument(
                path = "/logs/output.txt",
                destinationUri = "content://docs/export",
            )
        }
    }

    @Test
    fun `initial load exposes the configured workspace root path`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals("/workspace/root", viewModel.uiState.value.workspaceRootPath)
    }

    @Test
    fun `importFiles surfaces a friendly error when import fails`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        coEvery {
            importWorkspaceDocument(targetDirectoryPath = "/", documentUri = "content://docs/fail")
        } throws IllegalStateException("Permission may have been revoked.")

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.importFiles(listOf("content://docs/fail"))
        advanceUntilIdle()

        assertEquals(
            "Android no longer allows access to the selected file or destination.",
            viewModel.uiState.value.errorMessage,
        )
    }

    private fun createViewModel(): WorkspaceBrowserViewModel {
        return WorkspaceBrowserViewModel(
            workspaceRepository = workspaceRepository,
            importWorkspaceDocument = importWorkspaceDocument,
            exportWorkspaceDocument = exportWorkspaceDocument,
        )
    }
}

private class FakeWorkspaceRepository : WorkspaceRepository {
    override suspend fun getWorkspaceRootPath(): String = "/workspace/root"

    override suspend fun list(path: String): List<WorkspaceEntry> = emptyList()

    override suspend fun createDirectory(path: String) = Unit

    override suspend fun rename(path: String, newName: String): WorkspaceEntry {
        error("Not needed for this test")
    }

    override suspend fun move(path: String, destinationDirectoryPath: String): WorkspaceEntry {
        error("Not needed for this test")
    }

    override suspend fun delete(path: String) = Unit

    override fun normalizePath(path: String): String = path

    override suspend fun resolveLocalPath(path: String): String = path

    override suspend fun writeTextFile(path: String, text: String): WorkspaceEntry {
        error("Not needed for this test")
    }

    override suspend fun importFile(
        targetDirectoryPath: String,
        fileName: String,
        inputStream: InputStream,
    ): WorkspaceEntry {
        error("Not needed for this test")
    }

    override suspend fun exportFile(path: String, outputStream: OutputStream) = Unit
}
