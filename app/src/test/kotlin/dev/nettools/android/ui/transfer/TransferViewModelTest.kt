package dev.nettools.android.ui.transfer

import android.content.Context
import dev.nettools.android.data.db.QueuedJobDao
import dev.nettools.android.data.security.CredentialStore
import dev.nettools.android.data.security.KnownHostsManager
import dev.nettools.android.data.ssh.SshConnectionManager
import dev.nettools.android.domain.model.AuthType
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
    private val profiles = MutableStateFlow(emptyList<dev.nettools.android.domain.model.ConnectionProfile>())

    private lateinit var progressHolder: TransferProgressHolder
    private lateinit var viewModel: TransferViewModel

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
}
