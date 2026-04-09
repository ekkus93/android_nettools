package dev.nettools.android.ui.sftp;

import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dev.nettools.android.data.ssh.SftpClient;
import dev.nettools.android.data.ssh.SshConnectionManager;
import dev.nettools.android.data.security.KnownHostsManager;
import dev.nettools.android.domain.model.AuthType;
import dev.nettools.android.domain.model.RemoteFileEntry;
import dev.nettools.android.domain.model.TransferError;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import net.schmizz.sshj.SSHClient;
import javax.inject.Inject;

/**
 * ViewModel for the SFTP Browser screen.
 * Opens an SSH session on demand and navigates the remote file system.
 *
 * @property sshConnectionManager Factory for SSH sessions.
 * @property sftpClient SFTP operations.
 * @property knownHostsManager Host-key trust management.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u0003\n\u0000\b\u0007\u0018\u00002\u00020\u0001B!\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0004\b\b\u0010\tJH\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00142\u0006\u0010\u0018\u001a\u00020\u00192\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u00142\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00142\b\b\u0002\u0010\u001e\u001a\u00020\u0014J\u000e\u0010\u001f\u001a\u00020\u001d2\u0006\u0010 \u001a\u00020\u0014J\u0006\u0010!\u001a\u00020\u001dJ\u0006\u0010\"\u001a\u00020\u001dJ\u0006\u0010#\u001a\u00020\u001dJ\u000e\u0010$\u001a\u00020\u001d2\u0006\u0010%\u001a\u00020&J\u000e\u0010\'\u001a\u00020\u001d2\u0006\u0010%\u001a\u00020&J\u000e\u0010(\u001a\u00020\u001d2\u0006\u0010)\u001a\u00020\u0014J\u0006\u0010*\u001a\u00020\u001dJ\u0006\u0010+\u001a\u00020\u001dJ\u000e\u0010,\u001a\u00020\u001d2\u0006\u0010%\u001a\u00020&J\u0006\u0010-\u001a\u00020\u001dJ\u0006\u0010.\u001a\u00020\u001dJ\u0006\u0010/\u001a\u00020\u001dJ\u000e\u00100\u001a\u00020\u001d2\u0006\u0010)\u001a\u00020\u0014J\u0006\u00101\u001a\u00020\u001dJ\u0006\u00102\u001a\u00020\u001dJ\u0006\u00103\u001a\u00020\u001dJ\b\u00104\u001a\u00020\u001dH\u0014J\u0010\u00105\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0014H\u0002J\b\u00106\u001a\u00020\u0012H\u0002J\u0016\u00107\u001a\b\u0012\u0004\u0012\u00020\u0014082\u0006\u0010 \u001a\u00020\u0014H\u0002J\u0010\u00109\u001a\u00020\u001d2\u0006\u0010:\u001a\u00020;H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006<"}, d2 = {"Ldev/nettools/android/ui/sftp/SftpBrowserViewModel;", "Landroidx/lifecycle/ViewModel;", "sshConnectionManager", "Ldev/nettools/android/data/ssh/SshConnectionManager;", "sftpClient", "Ldev/nettools/android/data/ssh/SftpClient;", "knownHostsManager", "Ldev/nettools/android/data/security/KnownHostsManager;", "<init>", "(Ldev/nettools/android/data/ssh/SshConnectionManager;Ldev/nettools/android/data/ssh/SftpClient;Ldev/nettools/android/data/security/KnownHostsManager;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Ldev/nettools/android/ui/sftp/SftpBrowserUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "sshClient", "Lnet/schmizz/sshj/SSHClient;", "host", "", "port", "", "username", "authType", "Ldev/nettools/android/domain/model/AuthType;", "password", "keyPath", "connect", "", "initialPath", "navigate", "path", "navigateUp", "navigateHome", "refresh", "selectEntry", "entry", "Ldev/nettools/android/domain/model/RemoteFileEntry;", "requestRename", "onRenameNameChange", "v", "dismissRename", "confirmRename", "requestDelete", "dismissDelete", "confirmDelete", "requestNewDir", "onNewDirNameChange", "dismissNewDir", "confirmNewDir", "onErrorDismissed", "onCleared", "openSshAndNavigate", "requireClient", "buildBreadcrumbs", "", "handleError", "t", "", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SftpBrowserViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.data.ssh.SshConnectionManager sshConnectionManager = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.data.ssh.SftpClient sftpClient = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.data.security.KnownHostsManager knownHostsManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<dev.nettools.android.ui.sftp.SftpBrowserUiState> _uiState = null;
    
    /**
     * Current SFTP browser state.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<dev.nettools.android.ui.sftp.SftpBrowserUiState> uiState = null;
    @org.jetbrains.annotations.Nullable()
    private net.schmizz.sshj.SSHClient sshClient;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String host = "";
    private int port = 22;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String username = "";
    @org.jetbrains.annotations.NotNull()
    private dev.nettools.android.domain.model.AuthType authType = dev.nettools.android.domain.model.AuthType.PASSWORD;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String password;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String keyPath;
    
    @javax.inject.Inject()
    public SftpBrowserViewModel(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.ssh.SshConnectionManager sshConnectionManager, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.ssh.SftpClient sftpClient, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.security.KnownHostsManager knownHostsManager) {
        super();
    }
    
    /**
     * Current SFTP browser state.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<dev.nettools.android.ui.sftp.SftpBrowserUiState> getUiState() {
        return null;
    }
    
    /**
     * Initialises the browser with connection parameters and opens the SSH session.
     * Should be called once, immediately after the ViewModel is created.
     *
     * @param host Remote hostname.
     * @param port SSH port.
     * @param username Remote username.
     * @param authType Authentication method.
     * @param password Plaintext password (for [AuthType.PASSWORD]).
     * @param keyPath Path to private key (for [AuthType.PRIVATE_KEY]).
     * @param initialPath Starting directory (default: home directory).
     */
    public final void connect(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.AuthType authType, @org.jetbrains.annotations.Nullable()
    java.lang.String password, @org.jetbrains.annotations.Nullable()
    java.lang.String keyPath, @org.jetbrains.annotations.NotNull()
    java.lang.String initialPath) {
    }
    
    /**
     * Navigates into [path], adding it to the breadcrumb trail.
     *
     * @param path Absolute path on the remote host.
     */
    public final void navigate(@org.jetbrains.annotations.NotNull()
    java.lang.String path) {
    }
    
    /**
     * Navigates to the parent of the current directory.
     */
    public final void navigateUp() {
    }
    
    /**
     * Navigates to the remote home directory.
     */
    public final void navigateHome() {
    }
    
    /**
     * Refreshes the current directory listing.
     */
    public final void refresh() {
    }
    
    /**
     * Selects [entry] as the result when the browser is in picker mode.
     * The selection is surfaced via [SftpBrowserUiState.selectedPath].
     */
    public final void selectEntry(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.RemoteFileEntry entry) {
    }
    
    /**
     * Opens the rename dialog for [entry].
     */
    public final void requestRename(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.RemoteFileEntry entry) {
    }
    
    /**
     * Updates the rename text field value.
     */
    public final void onRenameNameChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    /**
     * Dismisses the rename dialog without making changes.
     */
    public final void dismissRename() {
    }
    
    /**
     * Renames [renameTarget] to [SftpBrowserUiState.renameNewName] and refreshes.
     */
    public final void confirmRename() {
    }
    
    /**
     * Opens the delete-confirmation dialog for [entry].
     */
    public final void requestDelete(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.RemoteFileEntry entry) {
    }
    
    /**
     * Dismisses the delete confirmation dialog.
     */
    public final void dismissDelete() {
    }
    
    /**
     * Deletes [deleteTarget] and refreshes the current listing.
     */
    public final void confirmDelete() {
    }
    
    /**
     * Opens the new-directory dialog.
     */
    public final void requestNewDir() {
    }
    
    /**
     * Updates the new-directory name field.
     */
    public final void onNewDirNameChange(@org.jetbrains.annotations.NotNull()
    java.lang.String v) {
    }
    
    /**
     * Dismisses the new-directory dialog.
     */
    public final void dismissNewDir() {
    }
    
    /**
     * Creates the new directory and refreshes.
     */
    public final void confirmNewDir() {
    }
    
    /**
     * Dismisses the current error message.
     */
    public final void onErrorDismissed() {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
    
    private final void openSshAndNavigate(java.lang.String initialPath) {
    }
    
    private final net.schmizz.sshj.SSHClient requireClient() {
        return null;
    }
    
    private final java.util.List<java.lang.String> buildBreadcrumbs(java.lang.String path) {
        return null;
    }
    
    private final void handleError(java.lang.Throwable t) {
    }
}