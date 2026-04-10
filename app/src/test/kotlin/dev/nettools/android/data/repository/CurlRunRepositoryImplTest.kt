package dev.nettools.android.data.repository

import dev.nettools.android.data.db.CurlRunDao
import dev.nettools.android.data.db.CurlRunEntity
import dev.nettools.android.data.db.CurlRunOutputDao
import dev.nettools.android.data.db.CurlRunOutputEntity
import dev.nettools.android.domain.model.CurlCleanupStatus
import dev.nettools.android.domain.model.CurlOutputStream
import dev.nettools.android.domain.model.CurlRunStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [CurlRunRepositoryImpl].
 */
class CurlRunRepositoryImplTest {

    private val runDao: CurlRunDao = mockk(relaxed = true)
    private val outputDao: CurlRunOutputDao = mockk(relaxed = true)
    private val repository = CurlRunRepositoryImpl(runDao, outputDao)

    @Test
    fun `observeAll combines run and output entities`() = runTest {
        every { runDao.observeAll() } returns flowOf(
            listOf(
                CurlRunEntity(
                    id = "run-1",
                    commandText = "curl https://example.com",
                    effectiveCommandText = "curl https://example.com",
                    normalizedCommandText = "curl https://example.com",
                    startedAt = 1L,
                    finishedAt = 2L,
                    status = CurlRunStatus.COMPLETED.name,
                    exitCode = 0,
                    durationMillis = 10L,
                    loggingEnabled = true,
                    cleanupStatus = CurlCleanupStatus.SKIPPED.name,
                    cleanupWarning = null,
                ),
            ),
        )
        every { outputDao.observeAll() } returns flowOf(
            listOf(
                CurlRunOutputEntity(
                    runId = "run-1",
                    stdoutText = "ok",
                    stderrText = "",
                    stdoutBytes = 2,
                    stderrBytes = 0,
                    stdoutTruncated = false,
                    stderrTruncated = false,
                ),
            ),
        )

        val records = repository.observeAll().first()

        assertEquals(1, records.size)
        assertEquals("ok", records.single().output.stdoutText)
        assertEquals(CurlRunStatus.COMPLETED, records.single().summary.status)
    }

    @Test
    fun `appendOutput caps retained bytes and marks truncation`() = runTest {
        coEvery { outputDao.getById("run-1") } returns CurlRunOutputEntity(
            runId = "run-1",
            stdoutText = "abc",
            stderrText = "",
            stdoutBytes = 3,
            stderrBytes = 0,
            stdoutTruncated = false,
            stderrTruncated = false,
        )

        repository.appendOutput(
            runId = "run-1",
            stream = CurlOutputStream.STDOUT,
            text = "defgh",
            byteCap = 5,
        )

        coVerify {
            outputDao.upsert(
                match { entity ->
                    entity.runId == "run-1" &&
                        entity.stdoutText == "abcde" &&
                        entity.stdoutBytes == 5 &&
                        entity.stdoutTruncated
                },
            )
        }
    }

    @Test
    fun `updateStatus preserves existing values when optional fields omitted`() = runTest {
        coEvery { runDao.getById("run-1") } returns CurlRunEntity(
            id = "run-1",
            commandText = "curl https://example.com",
            effectiveCommandText = null,
            normalizedCommandText = "curl https://example.com",
            startedAt = 1L,
            finishedAt = null,
            status = CurlRunStatus.IN_PROGRESS.name,
            exitCode = null,
            durationMillis = null,
            loggingEnabled = true,
            cleanupStatus = null,
            cleanupWarning = null,
        )

        repository.updateStatus(runId = "run-1", status = CurlRunStatus.CANCELLED)

        coVerify {
            runDao.upsert(
                match { entity ->
                    entity.id == "run-1" &&
                        entity.status == CurlRunStatus.CANCELLED.name &&
                        entity.finishedAt == null &&
                        entity.exitCode == null &&
                        entity.cleanupStatus == null
                },
            )
        }
    }

    @Test
    fun `appendOutput leaves already truncated stream unchanged`() = runTest {
        coEvery { outputDao.getById("run-1") } returns CurlRunOutputEntity(
            runId = "run-1",
            stdoutText = "abcde",
            stderrText = "",
            stdoutBytes = 5,
            stderrBytes = 0,
            stdoutTruncated = true,
            stderrTruncated = false,
        )

        repository.appendOutput("run-1", CurlOutputStream.STDOUT, "zzz", byteCap = 5)

        coVerify {
            outputDao.upsert(match { entity ->
                entity.stdoutText == "abcde" &&
                    entity.stdoutBytes == 5 &&
                    entity.stdoutTruncated
            })
        }
        assertTrue(true)
    }
}
