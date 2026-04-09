package dev.nettools.android.ui.transfer;

import android.content.Context;
import android.content.Intent;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dev.nettools.android.data.security.CredentialStore;
import dev.nettools.android.data.security.KnownHostsManager;
import dev.nettools.android.data.security.KnownHostsManager.VerificationResult;
import dev.nettools.android.domain.model.AuthType;
import dev.nettools.android.domain.model.ConnectionProfile;
import dev.nettools.android.domain.model.TransferDirection;
import dev.nettools.android.domain.model.TransferError;
import dev.nettools.android.domain.model.TransferJob;
import dev.nettools.android.domain.model.TransferStatus;
import dev.nettools.android.domain.repository.ConnectionProfileRepository;
import dev.nettools.android.domain.repository.TransferHistoryRepository;
import dev.nettools.android.service.PendingTransferParams;
import dev.nettools.android.service.TransferForegroundService;
import dev.nettools.android.service.TransferProgressHolder;
import kotlinx.coroutines.flow.SharedFlow;
import kotlinx.coroutines.flow.StateFlow;
import java.util.UUID;
import javax.inject.Inject;

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
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B;\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0004\b\u000e\u0010\u000fJ\u000e\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u0019J\u000e\u0010!\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u0019J\u000e\u0010\"\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u0019J\u000e\u0010#\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u0019J\u000e\u0010$\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u0019J\u000e\u0010%\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020&J\u000e\u0010\'\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020(J\u000e\u0010)\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u0019J\u000e\u0010*\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u0019J\u000e\u0010+\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020,J\u000e\u0010-\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u0019J\u0006\u0010.\u001a\u00020\u001fJ\u000e\u0010/\u001a\u00020\u001f2\u0006\u00100\u001a\u00020\u0019J\u0006\u00101\u001a\u00020\u001fJ\u0006\u00102\u001a\u00020\u001fJ\u0006\u00103\u001a\u00020\u001fJ\u000e\u00104\u001a\u00020\u001fH\u0082@\u00a2\u0006\u0002\u00105J\u000e\u00106\u001a\u00020\u001f2\u0006\u00107\u001a\u000208J\u000e\u00109\u001a\u00020\u001f2\u0006\u00107\u001a\u00020:J\u0010\u0010;\u001a\u00020\u001f2\u0006\u0010<\u001a\u00020\u0012H\u0002J\b\u0010=\u001a\u00020,H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00190\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00190\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001d\u00a8\u0006>"}, d2 = {"Ldev/nettools/android/ui/transfer/TransferViewModel;", "Landroidx/lifecycle/ViewModel;", "context", "Landroid/content/Context;", "profileRepository", "Ldev/nettools/android/domain/repository/ConnectionProfileRepository;", "historyRepository", "Ldev/nettools/android/domain/repository/TransferHistoryRepository;", "credentialStore", "Ldev/nettools/android/data/security/CredentialStore;", "knownHostsManager", "Ldev/nettools/android/data/security/KnownHostsManager;", "progressHolder", "Ldev/nettools/android/service/TransferProgressHolder;", "<init>", "(Landroid/content/Context;Ldev/nettools/android/domain/repository/ConnectionProfileRepository;Ldev/nettools/android/domain/repository/TransferHistoryRepository;Ldev/nettools/android/data/security/CredentialStore;Ldev/nettools/android/data/security/KnownHostsManager;Ldev/nettools/android/service/TransferProgressHolder;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Ldev/nettools/android/ui/transfer/TransferUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "_navigateToProgress", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "", "navigateToProgress", "Lkotlinx/coroutines/flow/SharedFlow;", "getNavigateToProgress", "()Lkotlinx/coroutines/flow/SharedFlow;", "onHostChange", "", "v", "onPortChange", "onUsernameChange", "onPasswordChange", "onKeyPathChange", "onAuthTypeChange", "Ldev/nettools/android/domain/model/AuthType;", "onDirectionChange", "Ldev/nettools/android/domain/model/TransferDirection;", "onLocalPathChange", "onRemotePathChange", "onSaveProfileChange", "", "onProfileNameChange", "onErrorDismissed", "onProfileSelected", "profileId", "onHostKeyAccepted", "onHostKeyRejected", "startTransfer", "dispatchTransfer", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "onUnknownHostKey", "error", "Ldev/nettools/android/domain/model/TransferError$UnknownHostKey;", "onHostKeyChanged", "Ldev/nettools/android/domain/model/TransferError$HostKeyChanged;", "persistProfile", "state", "validate", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class TransferViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.domain.repository.ConnectionProfileRepository profileRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.domain.repository.TransferHistoryRepository historyRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.data.security.CredentialStore credentialStore = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.data.security.KnownHostsManager knownHostsManager = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.service.TransferProgressHolder progressHolder = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<dev.nettools.android.ui.transfer.TransferUiState> _uiState = null;
    
    /**
     * Current transfer screen state.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<dev.nettools.android.ui.transfer.TransferUiState> uiState = null;
    
    /**
     * One-shot navigation event — emits the job ID when a transfer has been dispatched.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<java.lang.String> _navigateToProgress = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<java.lang.String> navigateToProgress = null;
    
    @javax.inject.Inject()
    public TransferViewModel(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.repository.ConnectionProfileRepository profileRepository, @kotlin.Suppress(names = {"UnusedPrivateMember"})
    @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.repository.TransferHistoryRepository historyRepository, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.security.CredentialStore credentialStore, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.security.KnownHostsManager knownHostsManager, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.service.TransferProgressHolder progressHolder) {
        super();
    }
    
    /**
     * Current transfer screen state.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<dev.nettools.android.ui.transfer.TransferUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<java.lang.String> getNavigateToProgress() {
        return null;
    }
    
    /**
     * Updates the host field and clears any existing host error.
     */
    public final void onHostChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    /**
     * Updates the port field and clears any existing port error.
     */
    public final void onPortChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    /**
     * Updates the username field and clears any existing username error.
     */
    public final void onUsernameChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    /**
     * Updates the password field.
     */
    public final void onPasswordChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    /**
     * Updates the private key path field.
     */
    public final void onKeyPathChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    /**
     * Switches the authentication type.
     */
    public final void onAuthTypeChange(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.AuthType v) {
    }
    
    /**
     * Switches the transfer direction.
     */
    public final void onDirectionChange(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferDirection v) {
    }
    
    /**
     * Updates the local path and clears any existing error.
     */
    public final void onLocalPathChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    /**
     * Updates the remote path and clears any existing error.
     */
    public final void onRemotePathChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    /**
     * Toggles whether to persist the connection as a saved profile.
     */
    public final void onSaveProfileChange(boolean v) {
    }
    
    /**
     * Updates the profile name used when saving.
     */
    public final void onProfileNameChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    /**
     * Dismisses the current error snackbar.
     */
    public final void onErrorDismissed() {
    }
    
    /**
     * Pre-fills connection fields from the saved profile with [profileId].
     * Also loads the saved password from [CredentialStore] if one is stored.
     */
    public final void onProfileSelected(@org.jetbrains.annotations.NotNull()
    java.lang.String profileId) {
    }
    
    /**
     * Called when the user taps "Trust" in the host-key dialog.
     * Saves the fingerprint and retries the transfer.
     */
    public final void onHostKeyAccepted() {
    }
    
    /**
     * Called when the user rejects the presented host key.
     */
    public final void onHostKeyRejected() {
    }
    
    /**
     * Validates the form and, if valid, checks the remote host key then
     * dispatches the transfer to [TransferForegroundService].
     */
    public final void startTransfer() {
    }
    
    private final java.lang.Object dispatchTransfer(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Surfaces a [TransferError.UnknownHostKey] as a TOFU dialog.
     * Called by the service or from an error callback when the host key is new.
     */
    public final void onUnknownHostKey(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferError.UnknownHostKey error) {
    }
    
    /**
     * Surfaces a [TransferError.HostKeyChanged] as a warning dialog.
     */
    public final void onHostKeyChanged(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferError.HostKeyChanged error) {
    }
    
    private final void persistProfile(dev.nettools.android.ui.transfer.TransferUiState state) {
    }
    
    private final boolean validate() {
        return false;
    }
}