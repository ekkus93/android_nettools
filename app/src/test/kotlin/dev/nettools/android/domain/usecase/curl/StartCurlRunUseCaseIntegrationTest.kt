package dev.nettools.android.domain.usecase.curl

import dev.nettools.android.data.curl.CurlCommandParser
import dev.nettools.android.data.curl.CurlCommandWorkspaceAdapter
import dev.nettools.android.data.curl.CurlOptionCatalog
import dev.nettools.android.domain.model.CurlRunRecord
import dev.nettools.android.domain.model.CurlRunSummary
import dev.nettools.android.domain.model.CurlSettings
import dev.nettools.android.domain.model.WorkspaceEntry
import dev.nettools.android.domain.repository.CurlRunRepository
import dev.nettools.android.domain.repository.CurlSettingsRepository
import dev.nettools.android.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.io.OutputStream

/**
 * Integration-style tests for [StartCurlRunUseCase].
 */
class StartCurlRunUseCaseIntegrationTest {

    private val workspaceRepository = FakeWorkspaceRepository()
    private val optionCatalog = object : CurlOptionCatalog {
        override fun supportedOptions(): Set<String> = emptySet()
    }
    private val validateCurlCommand = ValidateCurlCommandUseCase(CurlCommandParser(optionCatalog))

    @Test
    fun `start run keeps logging on while suppressing saved history when history is disabled`() = runTest {
        val runRepository = RecordingCurlRunRepository()
        val useCase = createUseCase(
            settings = CurlSettings(
                loggingEnabled = true,
                saveHistoryEnabled = false,
            ),
            runRepository = runRepository,
        )

        val result = useCase("curl https://example.com")

        assertTrue(result.isReady)
        assertEquals(true, result.pendingRun?.loggingEnabled)
        val storedSummary = requireNotNull(runRepository.lastUpsertedRecord).summary
        assertEquals("", storedSummary.commandText)
        assertEquals("", storedSummary.normalizedCommandText)
        assertTrue(storedSummary.loggingEnabled)
    }

    @Test
    fun `start run persists command history when history is enabled`() = runTest {
        val runRepository = RecordingCurlRunRepository()
        val useCase = createUseCase(
            settings = CurlSettings(
                loggingEnabled = false,
                saveHistoryEnabled = true,
            ),
            runRepository = runRepository,
        )

        val result = useCase("curl https://example.com")

        assertTrue(result.isReady)
        assertEquals(false, result.pendingRun?.loggingEnabled)
        val storedSummary = requireNotNull(runRepository.lastUpsertedRecord).summary
        assertEquals("curl https://example.com", storedSummary.commandText)
        assertEquals("curl https://example.com", storedSummary.normalizedCommandText)
        assertFalse(storedSummary.loggingEnabled)
    }

    private fun createUseCase(
        settings: CurlSettings,
        runRepository: RecordingCurlRunRepository,
    ): StartCurlRunUseCase {
        return StartCurlRunUseCase(
            validateCurlCommand = validateCurlCommand,
            workspaceAdapter = CurlCommandWorkspaceAdapter(workspaceRepository),
            settingsRepository = object : CurlSettingsRepository {
                override fun observeSettings(): Flow<CurlSettings> = emptyFlow()
                override suspend fun getSettings(): CurlSettings = settings
                override suspend fun setLoggingEnabled(enabled: Boolean) = Unit
                override suspend fun setSaveHistoryEnabled(enabled: Boolean) = Unit
                override suspend fun setWorkspaceRootPath(path: String?) = Unit
            },
            workspaceRepository = workspaceRepository,
            runRepository = runRepository,
        )
    }
}

private class RecordingCurlRunRepository : CurlRunRepository {
    var lastUpsertedRecord: CurlRunRecord? = null

    override fun observeAll(): Flow<List<CurlRunRecord>> = emptyFlow()

    override fun observeById(runId: String): Flow<CurlRunRecord?> = emptyFlow()

    override suspend fun getById(runId: String): CurlRunRecord? = null

    override suspend fun upsert(record: CurlRunRecord) {
        lastUpsertedRecord = record
    }

    override suspend fun upsertSummary(summary: CurlRunSummary) = Unit

    override suspend fun appendOutput(runId: String, stream: dev.nettools.android.domain.model.CurlOutputStream, text: String, byteCap: Int) = Unit

    override suspend fun updateStatus(
        runId: String,
        status: dev.nettools.android.domain.model.CurlRunStatus,
        finishedAt: Long?,
        exitCode: Int?,
        durationMillis: Long?,
        cleanupWarning: String?,
        effectiveCommandText: String?,
        cleanupStatus: dev.nettools.android.domain.model.CurlCleanupStatus?,
    ) = Unit

    override suspend fun clearAll() = Unit
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

    override suspend fun resolveLocalPath(path: String): String = "/workspace/root$path"

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
