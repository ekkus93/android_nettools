package dev.nettools.android.ui.transfer

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nettools.android.data.security.CredentialStore
import dev.nettools.android.data.security.KnownHostsManager
import dev.nettools.android.data.security.KnownHostsManager.VerificationResult
import dev.nettools.android.data.ssh.SshConnectionManager
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.ConnectionProfile
import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.domain.model.TransferError
import dev.nettools.android.domain.model.TransferJob
import dev.nettools.android.domain.model.TransferStatus
import dev.nettools.android.domain.repository.ConnectionProfileRepository
import dev.nettools.android.domain.repository.TransferHistoryRepository
import dev.nettools.android.service.PendingTransferParams
import dev.nettools.android.service.RemotePickerMode
import dev.nettools.android.service.SftpConnectionParams
import dev.nettools.android.service.TransferForegroundService
import dev.nettools.android.service.TransferProgressHolder
import dev.nettools.android.util.toDisplayPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

/**
 * State for a pending TOFU (trust-on-first-use) host key dialog.
 *
 * @property host The remote hostname.
 * @property fingerprint SHA-256 fingerprint of the presented key.
 * @property isChanged Whether this is a key-change warning (vs. a new host).
 * @property oldFingerprint Previously stored fingerprint, present when [isChanged] is true.
 */
data class PendingHostKey(
    val host: String,
    val fingerprint: String,
    val isChanged: Boolean = false,
    val oldFingerprint: String? = null,
)

private enum class PendingConnectionAction {
    TRANSFER,
    BROWSE,
}

/**
 * Full UI state for the SCP Transfer screen.
 */
data class TransferUiState(
    val savedProfiles: List<ConnectionProfile> = emptyList(),
    val selectedProfileId: String? = null,
    // Connection fields
    val host: String = "",
    val port: String = "22",
    val username: String = "",
    val authType: AuthType = AuthType.PASSWORD,
    val password: String = "",
    val keyPath: String = "",
    // Transfer fields
    val direction: TransferDirection = TransferDirection.UPLOAD,
    val localPath: String = "",
    val localPathDisplay: String = "",
    val remotePath: String = "",
    // Save profile
    val saveProfile: Boolean = false,
    val profileName: String = "",
    // Validation errors
    val hostError: String? = null,
    val portError: String? = null,
    val usernameError: String? = null,
    val localPathError: String? = null,
    val remotePathError: String? = null,
    // UI feedback
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val pendingHostKey: PendingHostKey? = null,
)

/**
 * ViewModel for the SCP Transfer screen.
 * Manages form state, validation, host-key TOFU dialogs, and service dispatch.
 *
 * @property context Application context used to start the foreground service.
 * @property profileRepository Source of saved connection profiles.
 * @property historyRepository Destination for completed transfer records.
 * @property credentialStore Secure storage for saved passwords.
 * @property knownHostsManager Host-key verification manager.
 * @property progressHolder In-memory queue bridging ViewModels and the service.
 */
@HiltViewModel
class TransferViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val profileRepository: ConnectionProfileRepository,
    private val historyRepository: TransferHistoryRepository,
    private val credentialStore: CredentialStore,
    private val knownHostsManager: KnownHostsManager,
    private val progressHolder: TransferProgressHolder,
    private val sshConnectionManager: SshConnectionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())

    /** Current transfer screen state. */
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    /** One-shot navigation event — emits the job ID when a transfer has been dispatched. */
    private val _navigateToProgress = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToProgress: SharedFlow<String> = _navigateToProgress.asSharedFlow()

    /** One-shot navigation event — emitted when the SFTP browser should open. */
    private val _navigateToSftpBrowser = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToSftpBrowser: SharedFlow<Unit> = _navigateToSftpBrowser.asSharedFlow()

    private var pendingConnectionAction: PendingConnectionAction? = null

    init {
        viewModelScope.launch {
            profileRepository.getAll().collect { profiles ->
                _uiState.update { it.copy(savedProfiles = profiles) }
            }
        }
    }

    // ── Form field updates ────────────────────────────────────────────────────

    /** Updates the host field and clears any existing host error. */
    fun onHostChange(v: String) = _uiState.update { it.copy(host = v, hostError = null) }
    /** Updates the port field and clears any existing port error. */
    fun onPortChange(v: String) = _uiState.update { it.copy(port = v, portError = null) }
    /** Updates the username field and clears any existing username error. */
    fun onUsernameChange(v: String) = _uiState.update { it.copy(username = v, usernameError = null) }
    /** Updates the password field. */
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v) }
    /** Updates the private key path field. */
    fun onKeyPathChange(v: String) = _uiState.update { it.copy(keyPath = v) }
    /** Switches the authentication type. */
    fun onAuthTypeChange(v: AuthType) = _uiState.update { it.copy(authType = v) }
    /** Switches the transfer direction. */
    fun onDirectionChange(v: TransferDirection) = _uiState.update { it.copy(direction = v) }
    /** Updates the local path and clears any existing error. */
    fun onLocalPathChange(v: String) = _uiState.update {
        it.copy(localPath = v, localPathDisplay = v, localPathError = null)
    }
    /** Updates the local path from a picker result while keeping a friendly display label. */
    fun onLocalPathPicked(v: String) = _uiState.update {
        it.copy(localPath = v, localPathDisplay = v.toDisplayPath(), localPathError = null)
    }
    /** Updates the remote path and clears any existing error. */
    fun onRemotePathChange(v: String) = _uiState.update { it.copy(remotePath = v, remotePathError = null) }
    /** Toggles whether to persist the connection as a saved profile. */
    fun onSaveProfileChange(v: Boolean) = _uiState.update { it.copy(saveProfile = v) }
    /** Updates the profile name used when saving. */
    fun onProfileNameChange(v: String) = _uiState.update { it.copy(profileName = v) }
    /** Dismisses the current error snackbar. */
    fun onErrorDismissed() = _uiState.update { it.copy(errorMessage = null) }

    /**
     * Pre-fills connection fields from the saved profile with [profileId].
     * Also loads the saved password from [CredentialStore] if one is stored.
     */
    fun onProfileSelected(profileId: String) {
        val profile = _uiState.value.savedProfiles.find { it.id == profileId } ?: return
        val savedPassword = if (profile.savePassword) credentialStore.getPassword(profile.id) else null
        _uiState.update {
            it.copy(
                selectedProfileId = profileId,
                host = profile.host,
                port = profile.port.toString(),
                username = profile.username,
                authType = profile.authType,
                keyPath = profile.keyPath ?: "",
                password = savedPassword ?: "",
            )
        }
    }

    // ── Host-key TOFU ─────────────────────────────────────────────────────────

    /**
     * Called when the user taps "Trust" in the host-key dialog.
     * Saves the fingerprint and retries the transfer.
     */
    fun onHostKeyAccepted() {
        val state = _uiState.value
        val pending = state.pendingHostKey ?: return
        val action = pendingConnectionAction ?: PendingConnectionAction.TRANSFER
        viewModelScope.launch {
            knownHostsManager.acceptHost(
                host = pending.host,
                port = state.port.toIntOrNull() ?: 22,
                fingerprint = pending.fingerprint,
            )
            pendingConnectionAction = null
            _uiState.update { it.copy(pendingHostKey = null) }
            executeTrustedAction(action)
        }
    }

    /** Called when the user rejects the presented host key. */
    fun onHostKeyRejected() {
        pendingConnectionAction = null
        _uiState.update { it.copy(pendingHostKey = null, isConnecting = false) }
    }

    // ── Transfer dispatch ─────────────────────────────────────────────────────

    /**
     * Validates the form and, if valid, performs a TOFU pre-flight host-key check before
     * dispatching the transfer to [TransferForegroundService].
     *
     * The pre-flight opens a TCP connection without authenticating just to capture the
     * server's host key fingerprint.  If the key is unknown or changed, a dialog is surfaced
     * to the user before any credentials are sent.
     */
    fun startTransfer() {
        if (!validate()) return
        beginConnectionAction(PendingConnectionAction.TRANSFER)
    }

    /**
     * Validates the connection fields and opens the remote-path browser after the host key is
     * trusted. This uses the same TOFU pre-flight flow as [startTransfer].
     */
    fun browseRemotePath() {
        if (!validateConnectionFields()) return
        beginConnectionAction(PendingConnectionAction.BROWSE)
    }

    private fun beginConnectionAction(action: PendingConnectionAction) {
        viewModelScope.launch {
            val state = _uiState.value
            val port = state.port.toIntOrNull() ?: 22
            _uiState.update { it.copy(isConnecting = true, errorMessage = null) }

            val liveFingerprint: String? = withContext(Dispatchers.IO) {
                sshConnectionManager.peekHostKey(state.host, port)
            }

            if (liveFingerprint == null) {
                // Couldn't reach the host before key exchange.
                _uiState.update {
                    it.copy(
                        isConnecting = false,
                        errorMessage = "Could not reach ${state.host}:$port",
                    )
                }
                return@launch
            }

            val stored = knownHostsManager.getStoredFingerprint(state.host, port)
            when {
                stored == null -> {
                    // Never seen this host — show TOFU dialog.
                    pendingConnectionAction = action
                    _uiState.update {
                        it.copy(
                            isConnecting = false,
                            pendingHostKey = PendingHostKey(
                                host = state.host,
                                fingerprint = liveFingerprint,
                            ),
                        )
                    }
                }
                stored != liveFingerprint -> {
                    // Key has changed — show warning dialog.
                    pendingConnectionAction = action
                    _uiState.update {
                        it.copy(
                            isConnecting = false,
                            pendingHostKey = PendingHostKey(
                                host = state.host,
                                fingerprint = liveFingerprint,
                                isChanged = true,
                                oldFingerprint = stored,
                            ),
                        )
                    }
                }
                else -> {
                    // Key is already trusted — proceed immediately.
                    pendingConnectionAction = null
                    _uiState.update { it.copy(isConnecting = false) }
                    executeTrustedAction(action)
                }
            }
        }
    }

    /**
     * Stores the current connection credentials in [TransferProgressHolder] so that the
     * SFTP Browser can read them on launch without passing secrets through nav arguments.
     * Must be called before navigating to [Routes.SFTP_BROWSER].
     */
    private fun prepareSftpBrowse() {
        val state = _uiState.value
        val port = state.port.toIntOrNull() ?: 22
        progressHolder.pendingSftpConnectionParams = SftpConnectionParams(
            host = state.host,
            port = port,
            username = state.username,
            authType = state.authType,
            password = state.password.ifBlank { null },
            keyPath = state.keyPath.ifBlank { null },
            pickerMode = when (state.direction) {
                TransferDirection.UPLOAD -> RemotePickerMode.PICK_DIRECTORY
                TransferDirection.DOWNLOAD -> RemotePickerMode.PICK_FILE
            },
        )
    }

    private suspend fun executeTrustedAction(action: PendingConnectionAction) {
        when (action) {
            PendingConnectionAction.TRANSFER -> dispatchTransfer()
            PendingConnectionAction.BROWSE -> {
                prepareSftpBrowse()
                _navigateToSftpBrowser.emit(Unit)
            }
        }
    }

    private suspend fun dispatchTransfer() {
        val state = _uiState.value
        _uiState.update { it.copy(isConnecting = true, errorMessage = null) }

        val jobId = UUID.randomUUID().toString()
        val port = state.port.toIntOrNull() ?: 22

        val job = TransferJob(
            id = jobId,
            profileId = state.selectedProfileId ?: "",
            direction = state.direction,
            localPath = state.localPath,
            remotePath = state.remotePath,
            status = TransferStatus.QUEUED,
        )

        if (state.saveProfile) persistProfile(state)

        val params = PendingTransferParams(
            job = job,
            host = state.host,
            port = port,
            username = state.username,
            authType = state.authType,
            password = state.password.ifBlank { null },
            keyPath = state.keyPath.ifBlank { null },
        )

        progressHolder.enqueue(params)
        context.startForegroundService(Intent(context, TransferForegroundService::class.java))
        _uiState.update { it.copy(isConnecting = false) }
        _navigateToProgress.emit(jobId)
    }

    /**
     * Surfaces a [TransferError.UnknownHostKey] as a TOFU dialog.
     * Called by the service or from an error callback when the host key is new.
     */
    fun onUnknownHostKey(error: TransferError.UnknownHostKey) {
        pendingConnectionAction = PendingConnectionAction.TRANSFER
        _uiState.update {
            it.copy(
                pendingHostKey = PendingHostKey(
                    host = error.host,
                    fingerprint = error.fingerprint,
                ),
            )
        }
    }

    /**
     * Surfaces a [TransferError.HostKeyChanged] as a warning dialog.
     */
    fun onHostKeyChanged(error: TransferError.HostKeyChanged) {
        pendingConnectionAction = PendingConnectionAction.TRANSFER
        _uiState.update {
            it.copy(
                pendingHostKey = PendingHostKey(
                    host = error.host,
                    fingerprint = error.newFingerprint,
                    isChanged = true,
                    oldFingerprint = error.oldFingerprint,
                ),
            )
        }
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private fun persistProfile(state: TransferUiState) {
        viewModelScope.launch {
            val profile = ConnectionProfile(
                id = UUID.randomUUID().toString(),
                name = state.profileName.ifBlank { state.host },
                host = state.host,
                port = state.port.toIntOrNull() ?: 22,
                username = state.username,
                authType = state.authType,
                keyPath = state.keyPath.ifBlank { null },
                savePassword = state.password.isNotBlank(),
            )
            profileRepository.save(profile)
            if (state.password.isNotBlank()) {
                credentialStore.savePassword(profile.id, state.password)
            }
        }
    }

    private fun validate(): Boolean {
        val s = _uiState.value
        var valid = true
        valid = validateConnectionFields() && valid
        if (s.localPath.isBlank()) { _uiState.update { it.copy(localPathError = "Local path is required") }; valid = false }
        if (s.remotePath.isBlank()) { _uiState.update { it.copy(remotePathError = "Remote path is required") }; valid = false }
        return valid
    }

    private fun validateConnectionFields(): Boolean {
        val s = _uiState.value
        var valid = true
        if (s.host.isBlank()) { _uiState.update { it.copy(hostError = "Host is required") }; valid = false }
        val port = s.port.toIntOrNull()
        if (port == null || port !in 1..65535) { _uiState.update { it.copy(portError = "Port must be 1–65535") }; valid = false }
        if (s.username.isBlank()) { _uiState.update { it.copy(usernameError = "Username is required") }; valid = false }
        return valid
    }
}
