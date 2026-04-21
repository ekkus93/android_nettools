package dev.nettools.android.ui.transfer

import android.content.Context
import dev.nettools.android.data.db.QueuedJobDao
import dev.nettools.android.data.security.CredentialStore
import dev.nettools.android.data.security.KnownHostsManager
import dev.nettools.android.data.ssh.SshConnectionManager
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.ConnectionProfile
import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.domain.repository.ConnectionProfileRepository
import dev.nettools.android.domain.repository.TransferHistoryRepository
import dev.nettools.android.service.RemotePickerMode
import dev.nettools.android.service.SftpConnectionParams
import dev.nettools.android.service.TransferProgressHolder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [TransferViewModel], covering host-key preflight behavior for remote browsing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransferViewModelTest {

    private val context: Context = mockk(relaxed = true)
    private val profileRepository: ConnectionProfileRepository = mockk()
    private val historyRepository: TransferHistoryRepository = mockk(relaxed = true)
    private val credentialStore: CredentialStore = mockk(relaxed = true)
    private val knownHostsManager: KnownHostsManager = mockk()
    private val sshConnectionManager: SshConnectionManager = mockk()
    private val queuedJobDao: QueuedJobDao = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val profiles = MutableStateFlow(emptyList<ConnectionProfile>())

    private lateinit var progressHolder: TransferProgressHolder
    private lateinit var viewModel: TransferViewModel

    /** Creates a ViewModel with a custom [SavedStateHandle]. */
    private fun makeViewModel(
        savedStateHandle: androidx.lifecycle.SavedStateHandle = androidx.lifecycle.SavedStateHandle(),
    ): TransferViewModel {
        every { profileRepository.getAll() } returns profiles
        return TransferViewModel(
            context = context,
            profileRepository = profileRepository,
            historyRepository = historyRepository,
            credentialStore = credentialStore,
            knownHostsManager = knownHostsManager,
            progressHolder = progressHolder,
            sshConnectionManager = sshConnectionManager,
            savedStateHandle = savedStateHandle,
        )
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { profileRepository.getAll() } returns profiles
        progressHolder = TransferProgressHolder(
            queuedJobDao = queuedJobDao,
            credentialStore = credentialStore,
            appScope = CoroutineScope(testDispatcher),
        )
        viewModel = TransferViewModel(
            context = context,
            profileRepository = profileRepository,
            historyRepository = historyRepository,
            credentialStore = credentialStore,
            knownHostsManager = knownHostsManager,
            progressHolder = progressHolder,
            sshConnectionManager = sshConnectionManager,
            savedStateHandle = androidx.lifecycle.SavedStateHandle(),
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `browse remote path first connect prompts for trust then navigates after acceptance`() = runTest(testDispatcher) {
        every { sshConnectionManager.peekHostKey(any(), any()) } returns "SHA256:first-key"
        every { knownHostsManager.getStoredFingerprint(any(), any()) } returns null
        every { knownHostsManager.acceptHost(any(), any(), any()) } returns Unit

        viewModel.onHostChange("example.com")
        viewModel.onPortChange("22")
        viewModel.onUsernameChange("phil")
        viewModel.onAuthTypeChange(AuthType.PASSWORD)
        viewModel.onPasswordChange("secret")

        val navigateDeferred = async { viewModel.navigateToSftpBrowser.first() }

        viewModel.browseRemotePath()
        waitUntil { viewModel.uiState.value.pendingHostKey != null }

        assertEquals("SHA256:first-key", viewModel.uiState.value.pendingHostKey?.fingerprint)
        assertFalse(navigateDeferred.isCompleted)

        viewModel.onHostKeyAccepted()
        waitUntil { progressHolder.pendingSftpConnectionParams != null }
        navigateDeferred.await()
        advanceUntilIdle()

        verify { knownHostsManager.acceptHost("example.com", 22, "SHA256:first-key") }
        assertNull(viewModel.uiState.value.pendingHostKey)
        assertEquals(
            SftpConnectionParams(
                host = "example.com",
                port = 22,
                username = "phil",
                authType = AuthType.PASSWORD,
                password = "secret",
                keyPath = null,
                pickerMode = RemotePickerMode.PICK_DIRECTORY,
            ),
            progressHolder.pendingSftpConnectionParams,
        )
    }

    private fun waitUntil(timeoutMillis: Long = 1_000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (!condition()) {
            if (System.currentTimeMillis() >= deadline) {
                throw AssertionError("Condition was not met within ${timeoutMillis}ms")
            }
            Thread.sleep(10)
        }
    }

    // ── Task 4.1 — startTransfer: validation errors ───────────────────────────

    @Test
    fun `startTransfer with blank host sets hostError and does not start service`() = runTest(testDispatcher) {
        viewModel.onUsernameChange("user")
        viewModel.onLocalPathChange("/local/file.txt")
        viewModel.onRemotePathChange("/remote/file.txt")

        viewModel.startTransfer()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.hostError)
        verify(exactly = 0) { context.startForegroundService(any()) }
    }

    @Test
    fun `startTransfer with port 0 sets portError`() = runTest(testDispatcher) {
        viewModel.onHostChange("host.com")
        viewModel.onPortChange("0")
        viewModel.onUsernameChange("user")
        viewModel.onLocalPathChange("/local/file.txt")
        viewModel.onRemotePathChange("/remote/file.txt")

        viewModel.startTransfer()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.portError)
    }

    @Test
    fun `startTransfer with port 65536 sets portError`() = runTest(testDispatcher) {
        viewModel.onHostChange("host.com")
        viewModel.onPortChange("65536")
        viewModel.onUsernameChange("user")
        viewModel.onLocalPathChange("/local/file.txt")
        viewModel.onRemotePathChange("/remote/file.txt")

        viewModel.startTransfer()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.portError)
    }

    @Test
    fun `startTransfer with non-numeric port sets portError`() = runTest(testDispatcher) {
        viewModel.onHostChange("host.com")
        viewModel.onPortChange("abc")
        viewModel.onUsernameChange("user")
        viewModel.onLocalPathChange("/local/file.txt")
        viewModel.onRemotePathChange("/remote/file.txt")

        viewModel.startTransfer()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.portError)
    }

    @Test
    fun `startTransfer with blank username sets usernameError`() = runTest(testDispatcher) {
        viewModel.onHostChange("host.com")
        viewModel.onLocalPathChange("/local/file.txt")
        viewModel.onRemotePathChange("/remote/file.txt")

        viewModel.startTransfer()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.usernameError)
    }

    @Test
    fun `startTransfer with blank localPath sets localPathError`() = runTest(testDispatcher) {
        viewModel.onHostChange("host.com")
        viewModel.onUsernameChange("user")
        viewModel.onRemotePathChange("/remote/file.txt")

        viewModel.startTransfer()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.localPathError)
    }

    @Test
    fun `startTransfer with blank remotePath sets remotePathError`() = runTest(testDispatcher) {
        viewModel.onHostChange("host.com")
        viewModel.onUsernameChange("user")
        viewModel.onLocalPathChange("/local/file.txt")

        viewModel.startTransfer()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.remotePathError)
    }

    @Test
    fun `startTransfer with all blank sets all errors simultaneously`() = runTest(testDispatcher) {
        // All fields blank (port defaults to "22" which is valid, so portError won't fire)
        viewModel.startTransfer()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.hostError)
        assertNotNull(state.usernameError)
        assertNotNull(state.localPathError)
        assertNotNull(state.remotePathError)
        verify(exactly = 0) { context.startForegroundService(any()) }
    }

    // ── Task 4.2 — onAuthTypeChange ───────────────────────────────────────────

    @Test
    fun `onAuthTypeChange to PRIVATE_KEY updates authType`() = runTest(testDispatcher) {
        viewModel.onAuthTypeChange(AuthType.PRIVATE_KEY)

        assertEquals(AuthType.PRIVATE_KEY, viewModel.uiState.value.authType)
    }

    @Test
    fun `onAuthTypeChange to PASSWORD updates authType`() = runTest(testDispatcher) {
        viewModel.onAuthTypeChange(AuthType.PRIVATE_KEY)
        viewModel.onAuthTypeChange(AuthType.PASSWORD)

        assertEquals(AuthType.PASSWORD, viewModel.uiState.value.authType)
    }

    // ── Task 4.3 — SavedStateHandle prefill ──────────────────────────────────

    @Test
    fun `SavedStateHandle host prefills uiState_host`() = runTest(testDispatcher) {
        val vm = makeViewModel(androidx.lifecycle.SavedStateHandle(mapOf("host" to "myserver.com")))

        assertEquals("myserver.com", vm.uiState.value.host)
    }

    @Test
    fun `SavedStateHandle remoteDir and fileName prefill remotePath`() = runTest(testDispatcher) {
        val vm = makeViewModel(
            androidx.lifecycle.SavedStateHandle(
                mapOf(
                    "host" to "myserver.com",
                    "remoteDir" to "/uploads",
                    "fileName" to "report.pdf",
                )
            )
        )

        assertEquals("/uploads/report.pdf", vm.uiState.value.remotePath)
    }

    @Test
    fun `SavedStateHandle direction DOWNLOAD prefills direction`() = runTest(testDispatcher) {
        val vm = makeViewModel(
            androidx.lifecycle.SavedStateHandle(
                mapOf(
                    "host" to "myserver.com",
                    "direction" to "DOWNLOAD",
                )
            )
        )

        assertEquals(TransferDirection.DOWNLOAD, vm.uiState.value.direction)
    }

    @Test
    fun `SavedStateHandle direction UPLOAD prefills direction`() = runTest(testDispatcher) {
        val vm = makeViewModel(
            androidx.lifecycle.SavedStateHandle(
                mapOf(
                    "host" to "myserver.com",
                    "direction" to "UPLOAD",
                )
            )
        )

        assertEquals(TransferDirection.UPLOAD, vm.uiState.value.direction)
    }

    @Test
    fun `empty SavedStateHandle leaves all form fields blank`() = runTest(testDispatcher) {
        val vm = makeViewModel(androidx.lifecycle.SavedStateHandle())

        val state = vm.uiState.value
        assertEquals("", state.host)
        assertEquals("", state.remotePath)
        assertEquals(TransferDirection.UPLOAD, state.direction)
    }

    @Test
    fun `SavedStateHandle with only host leaves remotePath empty`() = runTest(testDispatcher) {
        val vm = makeViewModel(
            androidx.lifecycle.SavedStateHandle(mapOf("host" to "myserver.com"))
        )

        assertEquals("", vm.uiState.value.remotePath)
    }

    // ── Task 4.4 — onProfileSelected ─────────────────────────────────────────

    @Test
    fun `onProfileSelected populates host port username authType from profile`() = runTest(testDispatcher) {
        val profile = ConnectionProfile(
            id = "prof-1",
            name = "Test Server",
            host = "test.example.com",
            port = 2222,
            username = "testuser",
            authType = AuthType.PRIVATE_KEY,
            keyPath = "/path/key",
            savePassword = false,
        )
        profiles.value = listOf(profile)
        advanceUntilIdle()

        viewModel.onProfileSelected("prof-1")

        val state = viewModel.uiState.value
        assertEquals("test.example.com", state.host)
        assertEquals("2222", state.port)
        assertEquals("testuser", state.username)
        assertEquals(AuthType.PRIVATE_KEY, state.authType)
    }

    @Test
    fun `onProfileSelected with savePassword=true loads password from credentialStore`() = runTest(testDispatcher) {
        val profile = ConnectionProfile(
            id = "prof-2",
            name = "Server",
            host = "server.com",
            port = 22,
            username = "user",
            authType = AuthType.PASSWORD,
            savePassword = true,
        )
        profiles.value = listOf(profile)
        advanceUntilIdle()
        every { credentialStore.getPassword("prof-2") } returns "stored-password"

        viewModel.onProfileSelected("prof-2")

        assertEquals("stored-password", viewModel.uiState.value.password)
    }

    @Test
    fun `onProfileSelected with unknown profileId does not change fields`() = runTest(testDispatcher) {
        val originalHost = viewModel.uiState.value.host

        viewModel.onProfileSelected("nonexistent-id")

        assertEquals(originalHost, viewModel.uiState.value.host)
    }
}
