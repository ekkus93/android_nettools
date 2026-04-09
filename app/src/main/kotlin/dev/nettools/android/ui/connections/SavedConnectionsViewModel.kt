package dev.nettools.android.ui.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.data.security.CredentialStore
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.ConnectionProfile
import dev.nettools.android.domain.repository.ConnectionProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** UI state for the edit / create profile dialog. */
data class ProfileEditState(
    val id: String = "",
    val name: String = "",
    val host: String = "",
    val port: String = "22",
    val username: String = "",
    val authType: AuthType = AuthType.PASSWORD,
    val keyPath: String = "",
    val savePassword: Boolean = false,
    val password: String = "",
    val nameError: String? = null,
    val hostError: String? = null,
    val portError: String? = null,
    val usernameError: String? = null,
    val isNew: Boolean = true,
)

/**
 * ViewModel for the Saved Connections screen.
 * Manages CRUD operations on [ConnectionProfile] and editing dialog state.
 *
 * @property profileRepository Repository for profile persistence.
 * @property credentialStore Secure storage for profile passwords.
 */
@HiltViewModel
class SavedConnectionsViewModel @Inject constructor(
    private val profileRepository: ConnectionProfileRepository,
    private val credentialStore: CredentialStore,
) : ViewModel() {

    /** All saved connection profiles. */
    val profiles: StateFlow<List<ConnectionProfile>> = profileRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editState = MutableStateFlow<ProfileEditState?>(null)

    /** Non-null while the edit/create dialog is open. */
    val editState: StateFlow<ProfileEditState?> = _editState.asStateFlow()

    private val _deleteConfirmId = MutableStateFlow<String?>(null)

    /** Non-null when a delete-confirmation dialog should be shown. */
    val deleteConfirmId: StateFlow<String?> = _deleteConfirmId.asStateFlow()

    /** Opens the edit dialog pre-populated for [profile], or blank for a new profile. */
    fun openEditor(profile: ConnectionProfile? = null) {
        if (profile == null) {
            _editState.value = ProfileEditState()
        } else {
            val savedPw = if (profile.savePassword) credentialStore.getPassword(profile.id) ?: "" else ""
            _editState.value = ProfileEditState(
                id = profile.id,
                name = profile.name,
                host = profile.host,
                port = profile.port.toString(),
                username = profile.username,
                authType = profile.authType,
                keyPath = profile.keyPath ?: "",
                savePassword = profile.savePassword,
                password = savedPw,
                isNew = false,
            )
        }
    }

    /** Dismisses the edit dialog without saving. */
    fun dismissEditor() { _editState.value = null }

    fun onNameChange(v: String) = _editState.update { it?.copy(name = v, nameError = null) }
    fun onHostChange(v: String) = _editState.update { it?.copy(host = v, hostError = null) }
    fun onPortChange(v: String) = _editState.update { it?.copy(port = v, portError = null) }
    fun onUsernameChange(v: String) = _editState.update { it?.copy(username = v, usernameError = null) }
    fun onAuthTypeChange(v: AuthType) = _editState.update { it?.copy(authType = v) }
    fun onKeyPathChange(v: String) = _editState.update { it?.copy(keyPath = v) }
    fun onSavePasswordChange(v: Boolean) = _editState.update { it?.copy(savePassword = v) }
    fun onPasswordChange(v: String) = _editState.update { it?.copy(password = v) }

    /** Validates and saves the current edit state. */
    fun saveProfile() {
        val state = _editState.value ?: return
        var valid = true
        var updated = state
        if (state.name.isBlank()) { updated = updated.copy(nameError = "Name is required"); valid = false }
        if (state.host.isBlank()) { updated = updated.copy(hostError = "Host is required"); valid = false }
        val port = state.port.toIntOrNull()
        if (port == null || port !in 1..65535) { updated = updated.copy(portError = "Port must be 1–65535"); valid = false }
        if (state.username.isBlank()) { updated = updated.copy(usernameError = "Username is required"); valid = false }
        if (!valid) { _editState.value = updated; return }

        viewModelScope.launch {
            val profileId = if (state.isNew) UUID.randomUUID().toString() else state.id
            val profile = ConnectionProfile(
                id = profileId,
                name = state.name.trim(),
                host = state.host.trim(),
                port = port!!,
                username = state.username.trim(),
                authType = state.authType,
                keyPath = state.keyPath.ifBlank { null },
                savePassword = state.savePassword,
            )
            profileRepository.save(profile)
            if (state.savePassword && state.password.isNotBlank()) {
                credentialStore.savePassword(profileId, state.password)
            } else if (!state.savePassword) {
                credentialStore.deletePassword(profileId)
            }
            _editState.value = null
        }
    }

    /** Shows the delete confirmation dialog for [profileId]. */
    fun requestDelete(profileId: String) { _deleteConfirmId.value = profileId }

    /** Dismisses the delete confirmation dialog. */
    fun dismissDelete() { _deleteConfirmId.value = null }

    /** Permanently deletes the profile with [profileId] and its stored credentials. */
    fun confirmDelete(profileId: String) {
        viewModelScope.launch {
            profileRepository.delete(profileId)
            credentialStore.deletePassword(profileId)
            _deleteConfirmId.value = null
        }
    }
}
