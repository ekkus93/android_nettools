package dev.nettools.android.ui.curl

import dev.nettools.android.data.curl.CurlRuntimeMetadata
import dev.nettools.android.data.curl.CurlRuntimeMetadataProvider
import dev.nettools.android.data.curl.CurlRuntimeMetadataResult
import dev.nettools.android.domain.model.CurlSettings
import dev.nettools.android.domain.model.WorkspaceEntry
import dev.nettools.android.domain.repository.CurlRunRepository
import dev.nettools.android.domain.repository.CurlSettingsRepository
import dev.nettools.android.domain.repository.WorkspaceRepository
import dev.nettools.android.domain.usecase.curl.ClearCurlLogsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
import java.io.InputStream
import java.io.OutputStream

/**
 * Unit tests for [CurlSettingsViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CurlSettingsViewModelTest {

    private val settingsRepository: CurlSettingsRepository = mockk(relaxed = true)
    private val workspaceRepository: WorkspaceRepository = FakeWorkspaceRepository()
    private val runtimeMetadataProvider: CurlRuntimeMetadataProvider = mockk()
    private val runRepository: CurlRunRepository = mockk(relaxed = true)
    private val settingsFlow = MutableStateFlow(CurlSettings(loggingEnabled = true, saveHistoryEnabled = true))
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.observeSettings() } returns settingsFlow
        coEvery { settingsRepository.getSettings() } returns settingsFlow.value
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads bundled runtime metadata when available`() = runTest(testDispatcher) {
        every { runtimeMetadataProvider.getRuntimeMetadata() } returns CurlRuntimeMetadataResult.Available(
            metadata = CurlRuntimeMetadata(
                bundledCurlVersion = "curl 8.8.0",
                supportedProtocols = listOf("http", "https"),
                supportedFeatures = listOf("SSL", "HTTP2"),
                http2Supported = true,
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("curl 8.8.0", state.bundledCurlVersion)
        assertEquals(listOf("http", "https"), state.bundledProtocols)
        assertEquals(listOf("SSL", "HTTP2"), state.bundledFeatures)
        assertEquals(true, state.http2Supported)
        assertNull(state.bundledRuntimeError)
        assertEquals("/workspace/root", state.effectiveWorkspaceRoot)
    }

    @Test
    fun `init surfaces runtime metadata failure without crashing`() = runTest(testDispatcher) {
        every { runtimeMetadataProvider.getRuntimeMetadata() } returns
            CurlRuntimeMetadataResult.Unavailable("Bundled curl runtime metadata is unavailable on this build.")

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Bundled curl runtime metadata is unavailable on this build.", state.bundledRuntimeError)
        assertEquals("", state.bundledCurlVersion)
        assertEquals(emptyList<String>(), state.bundledProtocols)
        assertEquals(emptyList<String>(), state.bundledFeatures)
        assertEquals(false, state.http2Supported)
    }

    private fun createViewModel(): CurlSettingsViewModel {
        return CurlSettingsViewModel(
            settingsRepository = settingsRepository,
            workspaceRepository = workspaceRepository,
            runtimeMetadataProvider = runtimeMetadataProvider,
            clearCurlLogs = ClearCurlLogsUseCase(runRepository),
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
