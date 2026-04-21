package dev.nettools.android.data.repository

import dev.nettools.android.data.db.KnownHostDao
import dev.nettools.android.data.db.KnownHostEntity
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for [KnownHostRepositoryImpl], verifying DAO delegation and
 * correct storage and retrieval of fingerprints.
 */
class KnownHostRepositoryImplTest {

    private val dao: KnownHostDao = mockk(relaxed = true)
    private val repository = KnownHostRepositoryImpl(dao)

    private fun makeEntity(
        host: String = "server.com",
        port: Int = 22,
        fingerprint: String = "SHA256:abcdef",
    ) = KnownHostEntity(
        id = "$host:$port",
        host = host,
        port = port,
        fingerprint = fingerprint,
    )

    // ── Task 9.2.1 — getByHost returns null when nothing stored ───────────────

    @Test
    fun `getByHost returns null when no entry is stored`() = runTest {
        coEvery { dao.getByHostAndPort("unknown.host", 22) } returns null

        val fingerprint = repository.getByHost("unknown.host", 22)

        assertNull(fingerprint)
    }

    // ── Task 9.2.2 — save then getByHost returns fingerprint ─────────────────

    @Test
    fun `save then getByHost returns the stored fingerprint`() = runTest {
        val entity = makeEntity(fingerprint = "SHA256:stored-key")
        coEvery { dao.getByHostAndPort("server.com", 22) } returns entity

        repository.save("server.com", 22, "SHA256:stored-key")
        val fingerprint = repository.getByHost("server.com", 22)

        assertEquals("SHA256:stored-key", fingerprint)
    }

    // ── Task 9.2.3 — save twice same host:port upserts fingerprint ────────────

    @Test
    fun `save twice same host and port replaces fingerprint`() = runTest {
        val updatedEntity = makeEntity(fingerprint = "SHA256:new-key")
        coEvery { dao.getByHostAndPort("server.com", 22) } returns updatedEntity

        repository.save("server.com", 22, "SHA256:old-key")
        repository.save("server.com", 22, "SHA256:new-key")

        val fingerprint = repository.getByHost("server.com", 22)
        assertEquals("SHA256:new-key", fingerprint)
        coVerify(exactly = 2) { dao.upsert(any()) }
    }

    // ── Task 9.2.4 — delete removes entry ────────────────────────────────────

    @Test
    fun `delete then getByHost returns null`() = runTest {
        coEvery { dao.getByHostAndPort("server.com", 22) } returns null

        repository.delete("server.com", 22)
        val fingerprint = repository.getByHost("server.com", 22)

        coVerify { dao.deleteByHostAndPort("server.com", 22) }
        assertNull(fingerprint)
    }

    // ── Task 9.2.5 — different ports are separate entries ─────────────────────

    @Test
    fun `different ports for same host are stored as separate entries`() = runTest {
        val entity22 = makeEntity(port = 22, fingerprint = "SHA256:key-port-22")
        val entity2222 = makeEntity(port = 2222, fingerprint = "SHA256:key-port-2222")
        coEvery { dao.getByHostAndPort("server.com", 22) } returns entity22
        coEvery { dao.getByHostAndPort("server.com", 2222) } returns entity2222

        repository.save("server.com", 22, "SHA256:key-port-22")
        repository.save("server.com", 2222, "SHA256:key-port-2222")

        val fp22 = repository.getByHost("server.com", 22)
        val fp2222 = repository.getByHost("server.com", 2222)

        assertEquals("SHA256:key-port-22", fp22)
        assertEquals("SHA256:key-port-2222", fp2222)
    }

    // ── Delegation ────────────────────────────────────────────────────────────

    @Test
    fun `save delegates to dao_upsert with correct entity fields`() = runTest {
        repository.save("myhost.com", 2222, "SHA256:myfingerprint")

        coVerify {
            dao.upsert(
                match { entity ->
                    entity.id == "myhost.com:2222" &&
                        entity.host == "myhost.com" &&
                        entity.port == 2222 &&
                        entity.fingerprint == "SHA256:myfingerprint"
                }
            )
        }
    }

    @Test
    fun `delete delegates to dao_deleteByHostAndPort`() = runTest {
        repository.delete("host.com", 22)

        coVerify { dao.deleteByHostAndPort("host.com", 22) }
    }
}
