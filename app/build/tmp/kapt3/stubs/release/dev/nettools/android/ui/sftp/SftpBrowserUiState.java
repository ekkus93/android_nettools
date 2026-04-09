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
 * UI state for the SFTP Browser screen.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b)\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0093\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\b\u0012\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0006\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0006\u0012\b\b\u0002\u0010\u0010\u001a\u00020\b\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0012\u0010\u0013J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\t\u0010$\u001a\u00020\bH\u00c6\u0003J\u000b\u0010%\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010&\u001a\u00020\bH\u00c6\u0003J\u000f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005H\u00c6\u0003J\u000b\u0010(\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010)\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\t\u0010*\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010+\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\t\u0010,\u001a\u00020\bH\u00c6\u0003J\t\u0010-\u001a\u00020\u0003H\u00c6\u0003J\u0095\u0001\u0010.\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\n\u001a\u00020\b2\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00030\u00052\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\u000e\u001a\u00020\u00032\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\u0010\u001a\u00020\b2\b\b\u0002\u0010\u0011\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010/\u001a\u00020\b2\b\u00100\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00101\u001a\u000202H\u00d6\u0001J\t\u00103\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\u0018R\u0013\u0010\t\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0015R\u0011\u0010\n\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u0018R\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0017R\u0013\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0015R\u0013\u0010\r\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u000e\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0015R\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001dR\u0011\u0010\u0010\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0018R\u0011\u0010\u0011\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0015\u00a8\u00064"}, d2 = {"Ldev/nettools/android/ui/sftp/SftpBrowserUiState;", "", "currentPath", "", "entries", "", "Ldev/nettools/android/domain/model/RemoteFileEntry;", "isLoading", "", "errorMessage", "isConnected", "breadcrumbs", "selectedPath", "renameTarget", "renameNewName", "deleteTarget", "showNewDirDialog", "newDirName", "<init>", "(Ljava/lang/String;Ljava/util/List;ZLjava/lang/String;ZLjava/util/List;Ljava/lang/String;Ldev/nettools/android/domain/model/RemoteFileEntry;Ljava/lang/String;Ldev/nettools/android/domain/model/RemoteFileEntry;ZLjava/lang/String;)V", "getCurrentPath", "()Ljava/lang/String;", "getEntries", "()Ljava/util/List;", "()Z", "getErrorMessage", "getBreadcrumbs", "getSelectedPath", "getRenameTarget", "()Ldev/nettools/android/domain/model/RemoteFileEntry;", "getRenameNewName", "getDeleteTarget", "getShowNewDirDialog", "getNewDirName", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "copy", "equals", "other", "hashCode", "", "toString", "app_release"})
public final class SftpBrowserUiState {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String currentPath = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<dev.nettools.android.domain.model.RemoteFileEntry> entries = null;
    private final boolean isLoading = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    private final boolean isConnected = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> breadcrumbs = null;
    
    /**
     * Non-null when the browser is in "pick" mode and an item has been selected.
     */
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String selectedPath = null;
    
    /**
     * Dialog state for rename operation.
     */
    @org.jetbrains.annotations.Nullable()
    private final dev.nettools.android.domain.model.RemoteFileEntry renameTarget = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String renameNewName = null;
    
    /**
     * Dialog state for delete confirmation.
     */
    @org.jetbrains.annotations.Nullable()
    private final dev.nettools.android.domain.model.RemoteFileEntry deleteTarget = null;
    
    /**
     * Whether the new-directory dialog is visible.
     */
    private final boolean showNewDirDialog = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String newDirName = null;
    
    public SftpBrowserUiState(@org.jetbrains.annotations.NotNull()
    java.lang.String currentPath, @org.jetbrains.annotations.NotNull()
    java.util.List<dev.nettools.android.domain.model.RemoteFileEntry> entries, boolean isLoading, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, boolean isConnected, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> breadcrumbs, @org.jetbrains.annotations.Nullable()
    java.lang.String selectedPath, @org.jetbrains.annotations.Nullable()
    dev.nettools.android.domain.model.RemoteFileEntry renameTarget, @org.jetbrains.annotations.NotNull()
    java.lang.String renameNewName, @org.jetbrains.annotations.Nullable()
    dev.nettools.android.domain.model.RemoteFileEntry deleteTarget, boolean showNewDirDialog, @org.jetbrains.annotations.NotNull()
    java.lang.String newDirName) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCurrentPath() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<dev.nettools.android.domain.model.RemoteFileEntry> getEntries() {
        return null;
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    public final boolean isConnected() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getBreadcrumbs() {
        return null;
    }
    
    /**
     * Non-null when the browser is in "pick" mode and an item has been selected.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSelectedPath() {
        return null;
    }
    
    /**
     * Dialog state for rename operation.
     */
    @org.jetbrains.annotations.Nullable()
    public final dev.nettools.android.domain.model.RemoteFileEntry getRenameTarget() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRenameNewName() {
        return null;
    }
    
    /**
     * Dialog state for delete confirmation.
     */
    @org.jetbrains.annotations.Nullable()
    public final dev.nettools.android.domain.model.RemoteFileEntry getDeleteTarget() {
        return null;
    }
    
    /**
     * Whether the new-directory dialog is visible.
     */
    public final boolean getShowNewDirDialog() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getNewDirName() {
        return null;
    }
    
    public SftpBrowserUiState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final dev.nettools.android.domain.model.RemoteFileEntry component10() {
        return null;
    }
    
    public final boolean component11() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<dev.nettools.android.domain.model.RemoteFileEntry> component2() {
        return null;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    public final boolean component5() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final dev.nettools.android.domain.model.RemoteFileEntry component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.ui.sftp.SftpBrowserUiState copy(@org.jetbrains.annotations.NotNull()
    java.lang.String currentPath, @org.jetbrains.annotations.NotNull()
    java.util.List<dev.nettools.android.domain.model.RemoteFileEntry> entries, boolean isLoading, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, boolean isConnected, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> breadcrumbs, @org.jetbrains.annotations.Nullable()
    java.lang.String selectedPath, @org.jetbrains.annotations.Nullable()
    dev.nettools.android.domain.model.RemoteFileEntry renameTarget, @org.jetbrains.annotations.NotNull()
    java.lang.String renameNewName, @org.jetbrains.annotations.Nullable()
    dev.nettools.android.domain.model.RemoteFileEntry deleteTarget, boolean showNewDirDialog, @org.jetbrains.annotations.NotNull()
    java.lang.String newDirName) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}