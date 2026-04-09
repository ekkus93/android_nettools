package dev.nettools.android.ui.connections;

import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dev.nettools.android.data.security.CredentialStore;
import dev.nettools.android.domain.model.AuthType;
import dev.nettools.android.domain.model.ConnectionProfile;
import dev.nettools.android.domain.repository.ConnectionProfileRepository;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import java.util.UUID;
import javax.inject.Inject;

/**
 * ViewModel for the Saved Connections screen.
 * Manages CRUD operations on [ConnectionProfile] and editing dialog state.
 *
 * @property profileRepository Repository for profile persistence.
 * @property credentialStore Secure storage for profile passwords.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u0012\u0010\u0017\u001a\u00020\u00182\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u000bJ\u0006\u0010\u001a\u001a\u00020\u0018J\u000e\u0010\u001b\u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020\u0014J\u000e\u0010\u001d\u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020\u0014J\u000e\u0010\u001e\u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020\u0014J\u000e\u0010\u001f\u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020\u0014J\u000e\u0010 \u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020!J\u000e\u0010\"\u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020\u0014J\u000e\u0010#\u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020$J\u000e\u0010%\u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020\u0014J\u0006\u0010&\u001a\u00020\u0018J\u000e\u0010\'\u001a\u00020\u00182\u0006\u0010(\u001a\u00020\u0014J\u0006\u0010)\u001a\u00020\u0018J\u000e\u0010*\u001a\u00020\u00182\u0006\u0010(\u001a\u00020\u0014R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0016\u0010\u000e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\rR\u0016\u0010\u0013\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00140\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00140\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\r\u00a8\u0006+"}, d2 = {"Ldev/nettools/android/ui/connections/SavedConnectionsViewModel;", "Landroidx/lifecycle/ViewModel;", "profileRepository", "Ldev/nettools/android/domain/repository/ConnectionProfileRepository;", "credentialStore", "Ldev/nettools/android/data/security/CredentialStore;", "<init>", "(Ldev/nettools/android/domain/repository/ConnectionProfileRepository;Ldev/nettools/android/data/security/CredentialStore;)V", "profiles", "Lkotlinx/coroutines/flow/StateFlow;", "", "Ldev/nettools/android/domain/model/ConnectionProfile;", "getProfiles", "()Lkotlinx/coroutines/flow/StateFlow;", "_editState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Ldev/nettools/android/ui/connections/ProfileEditState;", "editState", "getEditState", "_deleteConfirmId", "", "deleteConfirmId", "getDeleteConfirmId", "openEditor", "", "profile", "dismissEditor", "onNameChange", "v", "onHostChange", "onPortChange", "onUsernameChange", "onAuthTypeChange", "Ldev/nettools/android/domain/model/AuthType;", "onKeyPathChange", "onSavePasswordChange", "", "onPasswordChange", "saveProfile", "requestDelete", "profileId", "dismissDelete", "confirmDelete", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SavedConnectionsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.domain.repository.ConnectionProfileRepository profileRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.data.security.CredentialStore credentialStore = null;
    
    /**
     * All saved connection profiles.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<dev.nettools.android.domain.model.ConnectionProfile>> profiles = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<dev.nettools.android.ui.connections.ProfileEditState> _editState = null;
    
    /**
     * Non-null while the edit/create dialog is open.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<dev.nettools.android.ui.connections.ProfileEditState> editState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _deleteConfirmId = null;
    
    /**
     * Non-null when a delete-confirmation dialog should be shown.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> deleteConfirmId = null;
    
    @javax.inject.Inject()
    public SavedConnectionsViewModel(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.repository.ConnectionProfileRepository profileRepository, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.security.CredentialStore credentialStore) {
        super();
    }
    
    /**
     * All saved connection profiles.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<dev.nettools.android.domain.model.ConnectionProfile>> getProfiles() {
        return null;
    }
    
    /**
     * Non-null while the edit/create dialog is open.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<dev.nettools.android.ui.connections.ProfileEditState> getEditState() {
        return null;
    }
    
    /**
     * Non-null when a delete-confirmation dialog should be shown.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getDeleteConfirmId() {
        return null;
    }
    
    /**
     * Opens the edit dialog pre-populated for [profile], or blank for a new profile.
     */
    public final void openEditor(@org.jetbrains.annotations.Nullable()
    dev.nettools.android.domain.model.ConnectionProfile profile) {
    }
    
    /**
     * Dismisses the edit dialog without saving.
     */
    public final void dismissEditor() {
    }
    
    public final void onNameChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    public final void onHostChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    public final void onPortChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    public final void onUsernameChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    public final void onAuthTypeChange(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.AuthType v) {
    }
    
    public final void onKeyPathChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    public final void onSavePasswordChange(boolean v) {
    }
    
    public final void onPasswordChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    /**
     * Validates and saves the current edit state.
     */
    public final void saveProfile() {
    }
    
    /**
     * Shows the delete confirmation dialog for [profileId].
     */
    public final void requestDelete(@org.jetbrains.annotations.NotNull()
    java.lang.String profileId) {
    }
    
    /**
     * Dismisses the delete confirmation dialog.
     */
    public final void dismissDelete() {
    }
    
    /**
     * Permanently deletes the profile with [profileId] and its stored credentials.
     */
    public final void confirmDelete(@org.jetbrains.annotations.NotNull()
    java.lang.String profileId) {
    }
}