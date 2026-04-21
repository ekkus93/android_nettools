package dev.nettools.android.data.repository

import dev.nettools.android.data.db.ConnectionProfileDao
import dev.nettools.android.data.db.ConnectionProfileEntity
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.ConnectionProfile
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [ConnectionProfileRepositoryImpl], verifying DAO delegation and
 * correct bidirectional mapping between entity and domain model.
 */
class ConnectionProfileRepositoryImplTest {

    private val dao: ConnectionProfileDao = mockk(relaxed = true)
    private val repository = ConnectionProfileRepositoryImpl(dao)

    private fun makeEntity(
        id: String = "id-1",
        name: String = "My Server",
        host: String = "server.com",
        port: Int = 22,
        username: String = "user",
        authType: String = "PASSWORD",
        keyPath: String? = null,
        savePassword: Boolean = false,
    ) = ConnectionProfileEntity(
        id = id,
        name = name,
        host = host,
        port = port,
        username = username,
        authType = authType,
        keyPath = keyPath,
        savePassword = savePassword,
    )

    private fun makeProfile(
        id: String = "id-1",
        name: String = "My Server",
        host: String = "server.com",
        port: Int = 22,
        username: String = "user",
        authType: AuthType = AuthType.PASSWORD,
        keyPath: String? = null,
        savePassword: Boolean = false,
    ) = ConnectionProfile(
        id = id,
        name = name,
        host = host,
        port = port,
        username = username,
        authType = authType,
        keyPath = keyPath,
        savePassword = savePassword,
    )

    // ── Task 9.1.1 — getAll() emits empty list on fresh db ────────────────────

    @Test
    fun `getAll emits empty list when DAO emits empty`() = runTest {
        coEvery { dao.getAll() } returns flowOf(emptyList())

        val profiles = repository.getAll().first()

        assertTrue(profiles.isEmpty())
    }

    // ── Task 9.1.2 — save then getAll contains the profile ───────────────────

    @Test
    fun `save then getAll contains the saved profile`() = runTest {
        val profile = makeProfile()
        val entity = makeEntity()
        coEvery { dao.getAll() } returns flowOf(listOf(entity))

        repository.save(profile)

        val profiles = repository.getAll().first()
        assertEquals(1, profiles.size)
        assertEquals("id-1", profiles.first().id)
        assertEquals("My Server", profiles.first().name)
        assertEquals("server.com", profiles.first().host)
    }

    // ── Task 9.1.3 — save twice same ID upserts ──────────────────────────────

    @Test
    fun `save twice same ID upserts the profile`() = runTest {
        val profile1 = makeProfile(name = "Original")
        val profile2 = makeProfile(name = "Updated")
        val updatedEntity = makeEntity(name = "Updated")
        coEvery { dao.getAll() } returns flowOf(listOf(updatedEntity))

        repository.save(profile1)
        repository.save(profile2)

        val profiles = repository.getAll().first()
        assertEquals(1, profiles.size)
        assertEquals("Updated", profiles.first().name)
        coVerify(exactly = 2) { dao.upsert(any()) }
    }

    // ── Task 9.1.4 — getById returns profile when exists ─────────────────────

    @Test
    fun `getById returns profile when it exists`() = runTest {
        coEvery { dao.getById("id-1") } returns makeEntity()

        val profile = repository.getById("id-1")

        assertEquals("id-1", profile?.id)
        assertEquals("My Server", profile?.name)
    }

    // ── Task 9.1.5 — getById returns null when not found ─────────────────────

    @Test
    fun `getById returns null when profile does not exist`() = runTest {
        coEvery { dao.getById("missing-id") } returns null

        val profile = repository.getById("missing-id")

        assertNull(profile)
    }

    // ── Task 9.1.6 — delete removes profile ──────────────────────────────────

    @Test
    fun `delete removes profile, getById returns null afterwards`() = runTest {
        coJustRun { dao.deleteById("id-1") }
        coEvery { dao.getById("id-1") } returns null

        repository.delete("id-1")
        val profile = repository.getById("id-1")

        coVerify { dao.deleteById("id-1") }
        assertNull(profile)
    }

    // ── Task 9.1.7 — getAll re-emits after operations ─────────────────────────

    @Test
    fun `getAll re-emits entity list reflecting latest DAO data`() = runTest {
        val entities = listOf(makeEntity("id-1"), makeEntity("id-2", name = "Second"))
        coEvery { dao.getAll() } returns flowOf(emptyList(), entities)

        val results = mutableListOf<List<ConnectionProfile>>()
        repository.getAll().collect { results.add(it) }

        assertEquals(2, results.size)
        assertTrue(results[0].isEmpty())
        assertEquals(2, results[1].size)
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    @Test
    fun `getAll maps entity fields to domain model correctly`() = runTest {
        val entity = makeEntity(
            id = "p-1",
            name = "Work Server",
            host = "work.example.com",
            port = 2222,
            username = "alice",
            authType = "PRIVATE_KEY",
            keyPath = "/home/alice/.ssh/id_rsa",
            savePassword = true,
        )
        coEvery { dao.getAll() } returns flowOf(listOf(entity))

        val profile = repository.getAll().first().first()

        assertEquals("p-1", profile.id)
        assertEquals("Work Server", profile.name)
        assertEquals("work.example.com", profile.host)
        assertEquals(2222, profile.port)
        assertEquals("alice", profile.username)
        assertEquals(AuthType.PRIVATE_KEY, profile.authType)
        assertEquals("/home/alice/.ssh/id_rsa", profile.keyPath)
        assertEquals(true, profile.savePassword)
    }

    @Test
    fun `save delegates to DAO with correct entity fields`() = runTest {
        val profile = makeProfile(
            id = "p-2",
            name = "Home Server",
            host = "home.local",
            port = 2222,
            username = "bob",
            authType = AuthType.PRIVATE_KEY,
            keyPath = "/home/bob/.ssh/id_ed25519",
            savePassword = true,
        )

        repository.save(profile)

        coVerify {
            dao.upsert(
                match { entity ->
                    entity.id == "p-2" &&
                        entity.name == "Home Server" &&
                        entity.host == "home.local" &&
                        entity.port == 2222 &&
                        entity.username == "bob" &&
                        entity.authType == "PRIVATE_KEY" &&
                        entity.keyPath == "/home/bob/.ssh/id_ed25519" &&
                        entity.savePassword
                }
            )
        }
    }
}
