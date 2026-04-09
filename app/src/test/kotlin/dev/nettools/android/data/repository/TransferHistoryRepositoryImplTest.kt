package dev.nettools.android.data.repository

import dev.nettools.android.data.db.TransferHistoryDao
import dev.nettools.android.data.db.TransferHistoryEntity
import dev.nettools.android.domain.model.HistoryStatus
import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.domain.model.TransferHistoryEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for [TransferHistoryRepositoryImpl], verifying DAO delegation
 * and correct bidirectional mapping between entity and domain model.
 */
class TransferHistoryRepositoryImplTest {

    private val dao: TransferHistoryDao = mockk(relaxed = true)
    private val repository = TransferHistoryRepositoryImpl(dao)

    private fun makeEntity(
        id: String = "id-1",
        status: String = HistoryStatus.SUCCESS.name,
        errorMessage: String? = null,
    ) = TransferHistoryEntity(
        id = id,
        timestamp = 1_000L,
        direction = TransferDirection.UPLOAD.name,
        host = "test.host",
        username = "user",
        fileName = "file.txt",
        remoteDir = "/remote",
        fileSizeBytes = 512L,
        status = status,
        errorMessage = errorMessage,
    )

    private fun makeEntry(
        id: String = "id-1",
        status: HistoryStatus = HistoryStatus.SUCCESS,
        errorMessage: String? = null,
    ) = TransferHistoryEntry(
        id = id,
        timestamp = 1_000L,
        direction = TransferDirection.UPLOAD,
        host = "test.host",
        username = "user",
        fileName = "file.txt",
        remoteDir = "/remote",
        fileSizeBytes = 512L,
        status = status,
        errorMessage = errorMessage,
    )

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    fun `getAll - maps entities to domain models`() = runTest {
        coEvery { dao.getAll() } returns flowOf(listOf(makeEntity()))

        val entries = repository.getAll().first()

        assertEquals(1, entries.size)
        val entry = entries.first()
        assertEquals("id-1", entry.id)
        assertEquals("file.txt", entry.fileName)
        assertEquals("test.host", entry.host)
        assertEquals(HistoryStatus.SUCCESS, entry.status)
        assertNull(entry.errorMessage)
    }

    @Test
    fun `getAll - preserves errorMessage for FAILED entries`() = runTest {
        coEvery { dao.getAll() } returns flowOf(
            listOf(makeEntity(status = HistoryStatus.FAILED.name, errorMessage = "Auth failed"))
        )

        val entries = repository.getAll().first()

        assertEquals("Auth failed", entries.first().errorMessage)
        assertEquals(HistoryStatus.FAILED, entries.first().status)
    }

    @Test
    fun `getAll - returns empty list when DAO emits empty`() = runTest {
        coEvery { dao.getAll() } returns flowOf(emptyList())

        val entries = repository.getAll().first()

        assertEquals(0, entries.size)
    }

    // ── insert ────────────────────────────────────────────────────────────────

    @Test
    fun `insert - delegates to DAO with correct entity`() = runTest {
        val entry = makeEntry()

        repository.insert(entry)

        coVerify {
            dao.insert(
                match { entity ->
                    entity.id == "id-1" &&
                        entity.fileName == "file.txt" &&
                        entity.status == HistoryStatus.SUCCESS.name &&
                        entity.errorMessage == null
                }
            )
        }
    }

    @Test
    fun `insert - maps FAILED status and errorMessage to entity`() = runTest {
        val entry = makeEntry(status = HistoryStatus.FAILED, errorMessage = "Disk full")

        repository.insert(entry)

        coVerify {
            dao.insert(
                match { entity ->
                    entity.status == HistoryStatus.FAILED.name &&
                        entity.errorMessage == "Disk full"
                }
            )
        }
    }

    // ── clearAll ──────────────────────────────────────────────────────────────

    @Test
    fun `clearAll - delegates to DAO`() = runTest {
        repository.clearAll()

        coVerify { dao.clearAll() }
    }
}
