package dev.nettools.android.service

import dev.nettools.android.data.db.QueuedJobDao
import dev.nettools.android.data.db.QueuedJobEntity
import dev.nettools.android.data.security.CredentialStore
import dev.nettools.android.data.ssh.TransferProgress
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.domain.model.TransferJob
import dev.nettools.android.domain.model.TransferStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [TransferProgressHolder], covering in-memory queue operations,
 * Room persistence, credential storage, and job restore logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransferProgressHolderTest {

    private val queuedJobDao: QueuedJobDao = mockk(relaxed = true)
    private val credentialStore: CredentialStore = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var holder: TransferProgressHolder

    private fun makeParams(
        jobId: String = "job-1",
        password: String? = "secret",
    ) = PendingTransferParams(
        job = TransferJob(
            id = jobId,
            profileId = "profile-1",
            direction = TransferDirection.UPLOAD,
            localPath = "/local/file.txt",
            remotePath = "/remote/file.txt",
        ),
        host = "192.168.1.10",
        port = 22,
        username = "user",
        authType = AuthType.PASSWORD,
        password = password,
        keyPath = null,
    )

    @BeforeEach
    fun setUp() {
        holder = TransferProgressHolder(queuedJobDao, credentialStore, testScope)
    }

    // ── enqueue ───────────────────────────────────────────────────────────────

    @Test
    fun `enqueue - adds job to active jobs with QUEUED status`() = testScope.runTest {
        val params = makeParams()

        holder.enqueue(params)
        advanceUntilIdle()

        val active = holder.activeJobs.value
        assertEquals(1, active.size)
        assertEquals(TransferStatus.QUEUED, active.first().status)
        assertEquals("job-1", active.first().id)
    }

    @Test
    fun `enqueue - persists password to CredentialStore`() = testScope.runTest {
        val params = makeParams(jobId = "job-pw", password = "my_secret")

        holder.enqueue(params)
        advanceUntilIdle()

        verify { credentialStore.savePassword("queue_pw_job-pw", "my_secret") }
    }

    @Test
    fun `enqueue - skips password save when password is null`() = testScope.runTest {
        val params = makeParams(password = null)

        holder.enqueue(params)
        advanceUntilIdle()

        verify(exactly = 0) { credentialStore.savePassword(any(), any()) }
    }

    @Test
    fun `enqueue - persists job entity to Room`() = testScope.runTest {
        val params = makeParams()

        holder.enqueue(params)
        advanceUntilIdle()

        coVerify { queuedJobDao.upsert(any()) }
    }

    // ── dequeue ───────────────────────────────────────────────────────────────

    @Test
    fun `dequeue - returns null on empty queue`() {
        assertNull(holder.dequeue())
    }

    @Test
    fun `dequeue - returns and removes the enqueued params`() = testScope.runTest {
        val params = makeParams()
        holder.enqueue(params)
        advanceUntilIdle()

        val dequeued = holder.dequeue()

        assertNotNull(dequeued)
        assertEquals("job-1", dequeued!!.job.id)
        assertNull(holder.dequeue(), "Queue should be empty after dequeue")
    }

    @Test
    fun `dequeue - returns jobs in FIFO order`() = testScope.runTest {
        holder.enqueue(makeParams("first"))
        holder.enqueue(makeParams("second"))
        advanceUntilIdle()

        assertEquals("first", holder.dequeue()!!.job.id)
        assertEquals("second", holder.dequeue()!!.job.id)
    }

    // ── restorePersistedJobs ──────────────────────────────────────────────────

    @Test
    fun `restorePersistedJobs - re-enqueues entities from Room`() = testScope.runTest {
        val entity = QueuedJobEntity(
            jobId = "restored-1",
            host = "host.local",
            port = 22,
            username = "bob",
            authType = AuthType.PASSWORD.name,
            keyPath = null,
            profileId = null,
            direction = TransferDirection.DOWNLOAD.name,
            localPath = "/tmp/local",
            remotePath = "/remote/file",
            enqueuedAt = 0L,
        )
        coEvery { queuedJobDao.getAll() } returns listOf(entity)
        every { credentialStore.getPassword("queue_pw_restored-1") } returns "pw"

        val restored = holder.restorePersistedJobs()

        assertEquals(1, restored.size)
        assertEquals("restored-1", restored.first().job.id)
        assertEquals("pw", restored.first().password)
        assertNotNull(holder.dequeue())
    }

    @Test
    fun `restorePersistedJobs - skips already active jobs`() = testScope.runTest {
        holder.enqueue(makeParams("already-active"))
        advanceUntilIdle()

        val entity = QueuedJobEntity(
            jobId = "already-active",
            host = "h", port = 22, username = "u",
            authType = AuthType.PASSWORD.name, keyPath = null, profileId = null,
            direction = TransferDirection.UPLOAD.name,
            localPath = "/l", remotePath = "/r", enqueuedAt = 0L,
        )
        coEvery { queuedJobDao.getAll() } returns listOf(entity)

        // consume the original enqueue so we can check that nothing new is added
        holder.dequeue()

        val restored = holder.restorePersistedJobs()

        assertEquals(0, restored.size, "Already-active job should not be restored again")
    }

    // ── clearPersistedJob ─────────────────────────────────────────────────────

    @Test
    fun `clearPersistedJob - deletes from Room and removes password`() = testScope.runTest {
        holder.clearPersistedJob("job-done")
        advanceUntilIdle()

        coVerify { queuedJobDao.deleteById("job-done") }
        verify { credentialStore.deletePassword("queue_pw_job-done") }
    }

    // ── updateJobStatus / setJobFailed / removeJob ────────────────────────────

    @Test
    fun `updateJobStatus - changes status of matching job`() = testScope.runTest {
        holder.enqueue(makeParams("status-job"))
        advanceUntilIdle()

        holder.updateJobStatus("status-job", TransferStatus.IN_PROGRESS)

        val job = holder.activeJobs.value.first { it.id == "status-job" }
        assertEquals(TransferStatus.IN_PROGRESS, job.status)
    }

    @Test
    fun `setJobFailed - sets status to FAILED and stores error message`() = testScope.runTest {
        holder.enqueue(makeParams("fail-job"))
        advanceUntilIdle()

        holder.setJobFailed("fail-job", "Host unreachable")

        val job = holder.activeJobs.value.first { it.id == "fail-job" }
        assertEquals(TransferStatus.FAILED, job.status)
        assertEquals("Host unreachable", job.errorMessage)
    }

    @Test
    fun `removeJob - removes job and its progress entry`() = testScope.runTest {
        holder.enqueue(makeParams("remove-me"))
        advanceUntilIdle()
        holder.updateProgress(
            "remove-me",
            TransferProgress("f", 10L, 100L, 0.0)
        )

        holder.removeJob("remove-me")

        assertTrue(holder.activeJobs.value.none { it.id == "remove-me" })
        assertNull(holder.progress.value["remove-me"])
    }
}
