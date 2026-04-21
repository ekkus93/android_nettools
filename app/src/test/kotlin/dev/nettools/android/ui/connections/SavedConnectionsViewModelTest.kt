package dev.nettools.android.ui.connections

import dev.nettools.android.data.security.CredentialStore
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.ConnectionProfile
import dev.nettools.android.domain.repository.ConnectionProfileRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [SavedConnectionsViewModel], covering editor open/close, field change helpers,
 * validation, save/delete flows, and credential delegation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SavedConnectionsViewModelTest {

    private val profileRepository: ConnectionProfileRepository = mockk()
    private val credentialStore: CredentialStore = mockk()
    private val profilesFlow = MutableStateFlow<List<ConnectionProfile>>(emptyList())
    private lateinit var viewModel: SavedConnectionsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun makeProfile(
        id: String = "profile-id",
        name: String = "My Server",
        host: String = "server.example.com",
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

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { profileRepository.getAll() } returns profilesFlow
        coJustRun { profileRepository.save(any()) }
        coJustRun { profileRepository.delete(any()) }
        justRun { credentialStore.savePassword(any(), any()) }
        justRun { credentialStore.deletePassword(any()) }
        viewModel = SavedConnectionsViewModel(profileRepository, credentialStore)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Task 1.1 — openEditor: new profile ────────────────────────────────────

    @Test
    fun `openEditor(null) sets editState with isNew=true, port=22, PASSWORD auth`() = runTest(testDispatcher) {
        viewModel.openEditor(null)

        val state = viewModel.editState.value
        assertNotNull(state)
        assertTrue(state!!.isNew)
        assertEquals("22", state.port)
        assertEquals(AuthType.PASSWORD, state.authType)
        assertEquals("", state.name)
        assertEquals("", state.host)
        assertEquals("", state.username)
    }

    @Test
    fun `openEditor(null) does NOT call credentialStore_getPassword`() = runTest(testDispatcher) {
        viewModel.openEditor(null)

        verify(exactly = 0) { credentialStore.getPassword(any()) }
    }

    // ── Task 1.2 — openEditor: existing profile (no saved password) ───────────

    @Test
    fun `openEditor(profile) with savePassword=false pre-populates all fields`() = runTest(testDispatcher) {
        val profile = makeProfile(
            id = "p1",
            name = "Test Server",
            host = "test.host",
            port = 2222,
            username = "admin",
            authType = AuthType.PRIVATE_KEY,
            keyPath = "/path/to/key",
            savePassword = false,
        )

        viewModel.openEditor(profile)

        val state = viewModel.editState.value!!
        assertEquals("p1", state.id)
        assertEquals("Test Server", state.name)
        assertEquals("test.host", state.host)
        assertEquals("2222", state.port)
        assertEquals("admin", state.username)
        assertEquals(AuthType.PRIVATE_KEY, state.authType)
        assertEquals("/path/to/key", state.keyPath)
    }

    @Test
    fun `openEditor(profile) with savePassword=false sets password=empty without calling getPassword`() = runTest(testDispatcher) {
        val profile = makeProfile(savePassword = false)

        viewModel.openEditor(profile)

        assertEquals("", viewModel.editState.value!!.password)
        verify(exactly = 0) { credentialStore.getPassword(any()) }
    }

    @Test
    fun `openEditor(profile) sets isNew=false`() = runTest(testDispatcher) {
        viewModel.openEditor(makeProfile())

        assertFalse(viewModel.editState.value!!.isNew)
    }

    // ── Task 1.3 — openEditor: existing profile with saved password ───────────

    @Test
    fun `openEditor(profile) with savePassword=true calls getPassword and populates password`() = runTest(testDispatcher) {
        val profile = makeProfile(id = "p1", savePassword = true)
        every { credentialStore.getPassword("p1") } returns "secret123"

        viewModel.openEditor(profile)

        verify { credentialStore.getPassword("p1") }
        assertEquals("secret123", viewModel.editState.value!!.password)
    }

    @Test
    fun `openEditor(profile) with savePassword=true and null stored password sets password to empty`() = runTest(testDispatcher) {
        val profile = makeProfile(id = "p1", savePassword = true)
        every { credentialStore.getPassword("p1") } returns null

        viewModel.openEditor(profile)

        assertEquals("", viewModel.editState.value!!.password)
    }

    // ── Task 1.4 — dismissEditor ───────────────────────────────────────────────

    @Test
    fun `dismissEditor sets editState to null`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        assertNotNull(viewModel.editState.value)

        viewModel.dismissEditor()

        assertNull(viewModel.editState.value)
    }

    // ── Task 1.5 — field change helpers clear validation errors ───────────────

    @Test
    fun `onNameChange updates name and clears nameError`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        // Trigger a nameError by saving with blank name
        viewModel.saveProfile()
        advanceUntilIdle()
        assertNotNull(viewModel.editState.value!!.nameError)

        viewModel.onNameChange("NewName")

        assertEquals("NewName", viewModel.editState.value!!.name)
        assertNull(viewModel.editState.value!!.nameError)
    }

    @Test
    fun `onHostChange updates host and clears hostError`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.saveProfile()
        advanceUntilIdle()
        assertNotNull(viewModel.editState.value!!.hostError)

        viewModel.onHostChange("myhost.com")

        assertEquals("myhost.com", viewModel.editState.value!!.host)
        assertNull(viewModel.editState.value!!.hostError)
    }

    @Test
    fun `onPortChange updates port and clears portError`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onPortChange("0")
        viewModel.onNameChange("name")
        viewModel.onHostChange("host")
        viewModel.onUsernameChange("user")
        viewModel.saveProfile()
        advanceUntilIdle()
        assertNotNull(viewModel.editState.value!!.portError)

        viewModel.onPortChange("2222")

        assertEquals("2222", viewModel.editState.value!!.port)
        assertNull(viewModel.editState.value!!.portError)
    }

    @Test
    fun `onUsernameChange updates username and clears usernameError`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.saveProfile()
        advanceUntilIdle()
        assertNotNull(viewModel.editState.value!!.usernameError)

        viewModel.onUsernameChange("newuser")

        assertEquals("newuser", viewModel.editState.value!!.username)
        assertNull(viewModel.editState.value!!.usernameError)
    }

    // ── Task 1.6 — saveProfile: validation errors ─────────────────────────────

    @Test
    fun `saveProfile with blank name sets nameError and does not call repository_save`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onHostChange("host.com")
        viewModel.onUsernameChange("user")

        viewModel.saveProfile()
        advanceUntilIdle()

        assertNotNull(viewModel.editState.value!!.nameError)
        coVerify(exactly = 0) { profileRepository.save(any()) }
    }

    @Test
    fun `saveProfile with blank host sets hostError`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onNameChange("name")
        viewModel.onUsernameChange("user")

        viewModel.saveProfile()
        advanceUntilIdle()

        assertNotNull(viewModel.editState.value!!.hostError)
    }

    @Test
    fun `saveProfile with port 0 sets portError`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onNameChange("name")
        viewModel.onHostChange("host.com")
        viewModel.onPortChange("0")
        viewModel.onUsernameChange("user")

        viewModel.saveProfile()
        advanceUntilIdle()

        assertNotNull(viewModel.editState.value!!.portError)
        assertEquals("Port must be 1\u201365535", viewModel.editState.value!!.portError)
    }

    @Test
    fun `saveProfile with port 65536 sets portError`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onNameChange("name")
        viewModel.onHostChange("host.com")
        viewModel.onPortChange("65536")
        viewModel.onUsernameChange("user")

        viewModel.saveProfile()
        advanceUntilIdle()

        assertNotNull(viewModel.editState.value!!.portError)
    }

    @Test
    fun `saveProfile with non-numeric port sets portError`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onNameChange("name")
        viewModel.onHostChange("host.com")
        viewModel.onPortChange("abc")
        viewModel.onUsernameChange("user")

        viewModel.saveProfile()
        advanceUntilIdle()

        assertNotNull(viewModel.editState.value!!.portError)
    }

    @Test
    fun `saveProfile with blank username sets usernameError`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onNameChange("name")
        viewModel.onHostChange("host.com")

        viewModel.saveProfile()
        advanceUntilIdle()

        assertNotNull(viewModel.editState.value!!.usernameError)
    }

    @Test
    fun `saveProfile with all blank fields sets all four errors simultaneously`() = runTest(testDispatcher) {
        viewModel.openEditor(null)

        viewModel.saveProfile()
        advanceUntilIdle()

        val state = viewModel.editState.value!!
        assertNotNull(state.nameError)
        assertNotNull(state.hostError)
        assertNotNull(state.usernameError)
        // Port starts as "22" (valid), so portError won't be set by default
        coVerify(exactly = 0) { profileRepository.save(any()) }
    }

    @Test
    fun `saveProfile with all invalid fields sets all errors`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onPortChange("0")

        viewModel.saveProfile()
        advanceUntilIdle()

        val state = viewModel.editState.value!!
        assertNotNull(state.nameError)
        assertNotNull(state.hostError)
        assertNotNull(state.portError)
        assertNotNull(state.usernameError)
    }

    // ── Task 1.7 — saveProfile: happy path (new profile) ─────────────────────

    @Test
    fun `saveProfile with valid fields calls profileRepository_save with new UUID`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onNameChange("My Server")
        viewModel.onHostChange("server.com")
        viewModel.onUsernameChange("admin")

        viewModel.saveProfile()
        advanceUntilIdle()

        coVerify {
            profileRepository.save(match { profile ->
                profile.name == "My Server" &&
                    profile.host == "server.com" &&
                    profile.username == "admin" &&
                    profile.id.isNotBlank()
            })
        }
    }

    @Test
    fun `saveProfile happy path clears editState to null`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onNameChange("My Server")
        viewModel.onHostChange("server.com")
        viewModel.onUsernameChange("admin")

        viewModel.saveProfile()
        advanceUntilIdle()

        assertNull(viewModel.editState.value)
    }

    @Test
    fun `saveProfile with savePassword=true and non-blank password calls credentialStore_savePassword`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onNameChange("Server")
        viewModel.onHostChange("host.com")
        viewModel.onUsernameChange("user")
        viewModel.onSavePasswordChange(true)
        viewModel.onPasswordChange("mypassword")

        viewModel.saveProfile()
        advanceUntilIdle()

        verify { credentialStore.savePassword(any(), "mypassword") }
    }

    @Test
    fun `saveProfile with savePassword=false calls credentialStore_deletePassword`() = runTest(testDispatcher) {
        viewModel.openEditor(null)
        viewModel.onNameChange("Server")
        viewModel.onHostChange("host.com")
        viewModel.onUsernameChange("user")
        // savePassword defaults to false

        viewModel.saveProfile()
        advanceUntilIdle()

        verify { credentialStore.deletePassword(any()) }
    }

    // ── Task 1.8 — saveProfile: happy path (edit existing) ───────────────────

    @Test
    fun `saveProfile for existing profile uses existing id`() = runTest(testDispatcher) {
        val profile = makeProfile(id = "existing-id-123")
        viewModel.openEditor(profile)

        viewModel.saveProfile()
        advanceUntilIdle()

        coVerify { profileRepository.save(match { it.id == "existing-id-123" }) }
    }

    @Test
    fun `saveProfile with savePassword=true and blank password does NOT call credentialStore_savePassword`() = runTest(testDispatcher) {
        val profile = makeProfile(id = "p1", savePassword = true)
        every { credentialStore.getPassword("p1") } returns null
        viewModel.openEditor(profile)
        // password is blank (getPassword returned null)

        viewModel.saveProfile()
        advanceUntilIdle()

        verify(exactly = 0) { credentialStore.savePassword(any(), any()) }
    }

    // ── Task 1.9 — delete flow ────────────────────────────────────────────────

    @Test
    fun `requestDelete sets deleteConfirmId to the given id`() = runTest(testDispatcher) {
        viewModel.requestDelete("profile-123")

        assertEquals("profile-123", viewModel.deleteConfirmId.value)
    }

    @Test
    fun `dismissDelete sets deleteConfirmId to null`() = runTest(testDispatcher) {
        viewModel.requestDelete("profile-123")
        viewModel.dismissDelete()

        assertNull(viewModel.deleteConfirmId.value)
    }

    @Test
    fun `confirmDelete calls profileRepository_delete with the id`() = runTest(testDispatcher) {
        viewModel.confirmDelete("profile-456")
        advanceUntilIdle()

        coVerify { profileRepository.delete("profile-456") }
    }

    @Test
    fun `confirmDelete calls credentialStore_deletePassword with the id`() = runTest(testDispatcher) {
        viewModel.confirmDelete("profile-456")
        advanceUntilIdle()

        verify { credentialStore.deletePassword("profile-456") }
    }

    @Test
    fun `confirmDelete sets deleteConfirmId to null after completion`() = runTest(testDispatcher) {
        viewModel.requestDelete("profile-456")
        assertEquals("profile-456", viewModel.deleteConfirmId.value)

        viewModel.confirmDelete("profile-456")
        advanceUntilIdle()

        assertNull(viewModel.deleteConfirmId.value)
    }
}
