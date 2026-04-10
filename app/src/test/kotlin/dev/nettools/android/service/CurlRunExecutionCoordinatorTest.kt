package dev.nettools.android.service

import dev.nettools.android.data.curl.CurlBinaryProvider
import dev.nettools.android.data.curl.CurlCommandWorkspaceAdapter
import dev.nettools.android.data.curl.CurlExecutionRequest
import dev.nettools.android.data.curl.CurlExecutionResult
import dev.nettools.android.data.curl.CurlExecutor
import dev.nettools.android.data.curl.CurlRemoteCleanupExecutor
import dev.nettools.android.data.curl.CurlRemoteCleanupPlanner
import dev.nettools.android.data.curl.CurlRuntime
import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.CurlOutputChunk
import dev.nettools.android.domain.model.CurlOutputStream
import dev.nettools.android.domain.model.CurlPathReferenceRole
import dev.nettools.android.domain.model.CurlRunRecord
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.domain.model.CurlRunSummary
import dev.nettools.android.domain.model.ParsedCurlCommand
import dev.nettools.android.domain.model.ParsedCurlPathReference
import dev.nettools.android.domain.model.WorkspaceEntry
import dev.nettools.android.domain.repository.CurlRunRepository
import dev.nettools.android.domain.repository.WorkspaceRepository
import dev.nettools.android.util.CurlUserMessageFormatter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

/**
 * Integration-style tests for [CurlRunExecutionCoordinator].
 */
class CurlRunExecutionCoordinatorTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `execute records cancellation for an active run`() = runTest {
        val repository = RecordingCurlRunRepository()
        val holder = CurlRunHolder(this)
        val started = CompletableDeferred<Unit>()
        var outcome: CurlRunExecutionOutcome? = null
        val coordinator = createCoordinator(
            holder = holder,
            repository = repository,
            curlExecutor = object : CurlExecutor {
                override suspend fun execute(
                    request: CurlExecutionRequest,
                    onOutput: suspend (CurlOutputChunk) -> Unit,
                ): CurlExecutionResult {
                    onOutput(CurlOutputChunk(CurlOutputStream.STDOUT, "running"))
                    started.complete(Unit)
                    awaitCancellation()
                }
            },
        )

        val job = launch {
            outcome = coordinator.execute(params(loggingEnabled = true))
        }

        withTimeout(5_000) { started.await() }
        job.cancel()
        withTimeout(5_000) { job.join() }

        assertEquals(CurlRunStatus.CANCELLED, outcome?.status)
        assertEquals(CurlRunStatus.CANCELLED, holder.liveState.value.status)
        assertTrue(holder.liveState.value.stderrText.contains(CurlUserMessageFormatter.executionCancelled()))
        assertTrue(
            repository.statusUpdates.any { update ->
                update.status == CurlRunStatus.CANCELLED &&
                    update.cleanupStatus == CurlCleanupStatus.SKIPPED
            },
        )
        assertTrue(
            repository.outputAppends.any { append ->
                append.stream == CurlOutputStream.STDERR &&
                    append.text.contains(CurlUserMessageFormatter.executionCancelled())
            },
        )
    }

    @Test
    fun `execute cleans partial local outputs when curl exits nonzero`() = runTest {
        val repository = RecordingCurlRunRepository()
        val holder = CurlRunHolder(this)
        val outputFile = tempDir.resolve("workspace/out.txt").toFile().apply {
            requireNotNull(parentFile).mkdirs()
            writeText("partial")
        }
        val coordinator = createCoordinator(
            holder = holder,
            repository = repository,
            curlExecutor = object : CurlExecutor {
                override suspend fun execute(
                    request: CurlExecutionRequest,
                    onOutput: suspend (CurlOutputChunk) -> Unit,
                ): CurlExecutionResult = CurlExecutionResult(exitCode = 22, durationMillis = 123L)
            },
        )

        val outcome = coordinator.execute(
            params(
                command = ParsedCurlCommand(
                    originalText = "curl -o /out.txt https://example.com",
                    normalizedText = "curl -o /out.txt https://example.com",
                    tokens = listOf("curl", "-o", "/out.txt", "https://example.com"),
                    pathReferences = listOf(
                        ParsedCurlPathReference(
                            originalPath = "/out.txt",
                            normalizedPath = "/out.txt",
                            role = CurlPathReferenceRole.OUTPUT_FILE,
                        ),
                    ),
                ),
            ),
        )

        assertEquals(CurlRunStatus.FAILED, outcome.status)
        assertEquals(22, outcome.exitCode)
        assertEquals("Exit code: 22", outcome.failureReason)
        assertFalse(outputFile.exists())
        assertTrue(
            repository.statusUpdates.any { update ->
                update.status == CurlRunStatus.FAILED &&
                    update.exitCode == 22 &&
                    update.cleanupStatus == CurlCleanupStatus.SUCCEEDED
            },
        )
    }

    private fun createCoordinator(
        holder: CurlRunHolder,
        repository: RecordingCurlRunRepository,
        curlExecutor: CurlExecutor,
    ): CurlRunExecutionCoordinator {
        val workspaceRepository = FakeWorkspaceRepository(tempDir.resolve("workspace"))
        return CurlRunExecutionCoordinator(
            curlRunHolder = holder,
            runRepository = repository,
            workspaceAdapter = CurlCommandWorkspaceAdapter(workspaceRepository),
            curlExecutor = curlExecutor,
            remoteCleanupPlanner = CurlRemoteCleanupPlanner(),
            remoteCleanupExecutor = CurlRemoteCleanupExecutor(
                binaryProvider = object : CurlBinaryProvider {
                    override suspend fun getRuntime(): CurlRuntime {
                        error("Remote cleanup should not run in this test")
                    }
                },
            ),
        )
    }

    private fun params(
        command: ParsedCurlCommand = ParsedCurlCommand(
            originalText = "curl https://example.com",
            normalizedText = "curl https://example.com",
            tokens = listOf("curl", "https://example.com"),
            pathReferences = emptyList(),
        ),
        loggingEnabled: Boolean = false,
    ): PendingCurlRunParams = PendingCurlRunParams(
        runId = "run-1",
        rawCommandText = command.originalText,
        parsedCommand = command,
        workspaceRootPath = tempDir.resolve("workspace").toString(),
        loggingEnabled = loggingEnabled,
        stdoutByteCap = 16_384,
        stderrByteCap = 16_384,
    )
}

private class RecordingCurlRunRepository : CurlRunRepository {
    val statusUpdates = mutableListOf<StatusUpdate>()
    val outputAppends = mutableListOf<OutputAppend>()

    override fun observeAll(): Flow<List<CurlRunRecord>> = emptyFlow()

    override fun observeById(runId: String): Flow<CurlRunRecord?> = emptyFlow()

    override suspend fun getById(runId: String): CurlRunRecord? = null

    override suspend fun upsert(record: CurlRunRecord) = Unit

    override suspend fun upsertSummary(summary: CurlRunSummary) = Unit

    override suspend fun appendOutput(runId: String, stream: CurlOutputStream, text: String, byteCap: Int) {
        outputAppends += OutputAppend(runId, stream, text, byteCap)
    }

    override suspend fun updateStatus(
        runId: String,
        status: CurlRunStatus,
        finishedAt: Long?,
        exitCode: Int?,
        durationMillis: Long?,
        cleanupWarning: String?,
        effectiveCommandText: String?,
        cleanupStatus: CurlCleanupStatus?,
    ) {
        statusUpdates += StatusUpdate(
            runId = runId,
            status = status,
            exitCode = exitCode,
            durationMillis = durationMillis,
            cleanupWarning = cleanupWarning,
            effectiveCommandText = effectiveCommandText,
            cleanupStatus = cleanupStatus,
        )
    }

    override suspend fun clearAll() = Unit
}

private data class StatusUpdate(
    val runId: String,
    val status: CurlRunStatus,
    val exitCode: Int?,
    val durationMillis: Long?,
    val cleanupWarning: String?,
    val effectiveCommandText: String?,
    val cleanupStatus: CurlCleanupStatus?,
)

private data class OutputAppend(
    val runId: String,
    val stream: CurlOutputStream,
    val text: String,
    val byteCap: Int,
)

private class FakeWorkspaceRepository(
    private val workspaceRoot: Path,
) : WorkspaceRepository {
    override suspend fun getWorkspaceRootPath(): String = workspaceRoot.toString()

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

    override suspend fun resolveLocalPath(path: String): String =
        workspaceRoot.resolve(path.removePrefix("/")).toString()

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

    override suspend fun exportFile(path: String, outputStream: OutputStream) {
        error("Not needed for this test")
    }
}
