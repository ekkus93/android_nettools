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
 * Full UI state for the SCP Transfer screen.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b5\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u00ef\u0001\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0006\u0012\b\b\u0002\u0010\b\u001a\u00020\u0006\u0012\b\b\u0002\u0010\t\u001a\u00020\u0006\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u0006\u0012\b\b\u0002\u0010\r\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0013\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0006\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u0006\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u0013\u0012\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u001d\u00a2\u0006\u0004\b\u001e\u0010\u001fJ\u000f\u0010:\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u000b\u0010;\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\t\u0010<\u001a\u00020\u0006H\u00c6\u0003J\t\u0010=\u001a\u00020\u0006H\u00c6\u0003J\t\u0010>\u001a\u00020\u0006H\u00c6\u0003J\t\u0010?\u001a\u00020\u000bH\u00c6\u0003J\t\u0010@\u001a\u00020\u0006H\u00c6\u0003J\t\u0010A\u001a\u00020\u0006H\u00c6\u0003J\t\u0010B\u001a\u00020\u000fH\u00c6\u0003J\t\u0010C\u001a\u00020\u0006H\u00c6\u0003J\t\u0010D\u001a\u00020\u0006H\u00c6\u0003J\t\u0010E\u001a\u00020\u0013H\u00c6\u0003J\t\u0010F\u001a\u00020\u0006H\u00c6\u0003J\u000b\u0010G\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000b\u0010H\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000b\u0010I\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000b\u0010J\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000b\u0010K\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\t\u0010L\u001a\u00020\u0013H\u00c6\u0003J\u000b\u0010M\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000b\u0010N\u001a\u0004\u0018\u00010\u001dH\u00c6\u0003J\u00f1\u0001\u0010O\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\u00062\b\b\u0002\u0010\t\u001a\u00020\u00062\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u00062\b\b\u0002\u0010\r\u001a\u00020\u00062\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00062\b\b\u0002\u0010\u0011\u001a\u00020\u00062\b\b\u0002\u0010\u0012\u001a\u00020\u00132\b\b\u0002\u0010\u0014\u001a\u00020\u00062\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\u001a\u001a\u00020\u00132\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u001dH\u00c6\u0001J\u0013\u0010P\u001a\u00020\u00132\b\u0010Q\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010R\u001a\u00020SH\u00d6\u0001J\t\u0010T\u001a\u00020\u0006H\u00d6\u0001R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010#R\u0011\u0010\b\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010#R\u0011\u0010\t\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010#R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010(R\u0011\u0010\f\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010#R\u0011\u0010\r\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010#R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010,R\u0011\u0010\u0010\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010#R\u0011\u0010\u0011\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010#R\u0011\u0010\u0012\u001a\u00020\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u00100R\u0011\u0010\u0014\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010#R\u0013\u0010\u0015\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010#R\u0013\u0010\u0016\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010#R\u0013\u0010\u0017\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010#R\u0013\u0010\u0018\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010#R\u0013\u0010\u0019\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010#R\u0011\u0010\u001a\u001a\u00020\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u00100R\u0013\u0010\u001b\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u0010#R\u0013\u0010\u001c\u001a\u0004\u0018\u00010\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u00109\u00a8\u0006U"}, d2 = {"Ldev/nettools/android/ui/transfer/TransferUiState;", "", "savedProfiles", "", "Ldev/nettools/android/domain/model/ConnectionProfile;", "selectedProfileId", "", "host", "port", "username", "authType", "Ldev/nettools/android/domain/model/AuthType;", "password", "keyPath", "direction", "Ldev/nettools/android/domain/model/TransferDirection;", "localPath", "remotePath", "saveProfile", "", "profileName", "hostError", "portError", "usernameError", "localPathError", "remotePathError", "isConnecting", "errorMessage", "pendingHostKey", "Ldev/nettools/android/ui/transfer/PendingHostKey;", "<init>", "(Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ldev/nettools/android/domain/model/AuthType;Ljava/lang/String;Ljava/lang/String;Ldev/nettools/android/domain/model/TransferDirection;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;Ldev/nettools/android/ui/transfer/PendingHostKey;)V", "getSavedProfiles", "()Ljava/util/List;", "getSelectedProfileId", "()Ljava/lang/String;", "getHost", "getPort", "getUsername", "getAuthType", "()Ldev/nettools/android/domain/model/AuthType;", "getPassword", "getKeyPath", "getDirection", "()Ldev/nettools/android/domain/model/TransferDirection;", "getLocalPath", "getRemotePath", "getSaveProfile", "()Z", "getProfileName", "getHostError", "getPortError", "getUsernameError", "getLocalPathError", "getRemotePathError", "getErrorMessage", "getPendingHostKey", "()Ldev/nettools/android/ui/transfer/PendingHostKey;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component20", "component21", "copy", "equals", "other", "hashCode", "", "toString", "app_release"})
public final class TransferUiState {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<dev.nettools.android.domain.model.ConnectionProfile> savedProfiles = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String selectedProfileId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String host = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String port = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String username = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.domain.model.AuthType authType = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String password = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String keyPath = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.domain.model.TransferDirection direction = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String localPath = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String remotePath = null;
    private final boolean saveProfile = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String profileName = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String hostError = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String portError = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String usernameError = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String localPathError = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String remotePathError = null;
    private final boolean isConnecting = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    @org.jetbrains.annotations.Nullable()
    private final dev.nettools.android.ui.transfer.PendingHostKey pendingHostKey = null;
    
    public TransferUiState(@org.jetbrains.annotations.NotNull()
    java.util.List<dev.nettools.android.domain.model.ConnectionProfile> savedProfiles, @org.jetbrains.annotations.Nullable()
    java.lang.String selectedProfileId, @org.jetbrains.annotations.NotNull()
    java.lang.String host, @org.jetbrains.annotations.NotNull()
    java.lang.String port, @org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.AuthType authType, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    java.lang.String keyPath, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferDirection direction, @org.jetbrains.annotations.NotNull()
    java.lang.String localPath, @org.jetbrains.annotations.NotNull()
    java.lang.String remotePath, boolean saveProfile, @org.jetbrains.annotations.NotNull()
    java.lang.String profileName, @org.jetbrains.annotations.Nullable()
    java.lang.String hostError, @org.jetbrains.annotations.Nullable()
    java.lang.String portError, @org.jetbrains.annotations.Nullable()
    java.lang.String usernameError, @org.jetbrains.annotations.Nullable()
    java.lang.String localPathError, @org.jetbrains.annotations.Nullable()
    java.lang.String remotePathError, boolean isConnecting, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, @org.jetbrains.annotations.Nullable()
    dev.nettools.android.ui.transfer.PendingHostKey pendingHostKey) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<dev.nettools.android.domain.model.ConnectionProfile> getSavedProfiles() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSelectedProfileId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getHost() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPort() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUsername() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.model.AuthType getAuthType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPassword() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getKeyPath() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.model.TransferDirection getDirection() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLocalPath() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRemotePath() {
        return null;
    }
    
    public final boolean getSaveProfile() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getProfileName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getHostError() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPortError() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUsernameError() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLocalPathError() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getRemotePathError() {
        return null;
    }
    
    public final boolean isConnecting() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final dev.nettools.android.ui.transfer.PendingHostKey getPendingHostKey() {
        return null;
    }
    
    public TransferUiState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<dev.nettools.android.domain.model.ConnectionProfile> component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component11() {
        return null;
    }
    
    public final boolean component12() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component16() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component17() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component18() {
        return null;
    }
    
    public final boolean component19() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component20() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final dev.nettools.android.ui.transfer.PendingHostKey component21() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.model.AuthType component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.model.TransferDirection component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.ui.transfer.TransferUiState copy(@org.jetbrains.annotations.NotNull()
    java.util.List<dev.nettools.android.domain.model.ConnectionProfile> savedProfiles, @org.jetbrains.annotations.Nullable()
    java.lang.String selectedProfileId, @org.jetbrains.annotations.NotNull()
    java.lang.String host, @org.jetbrains.annotations.NotNull()
    java.lang.String port, @org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.AuthType authType, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    java.lang.String keyPath, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferDirection direction, @org.jetbrains.annotations.NotNull()
    java.lang.String localPath, @org.jetbrains.annotations.NotNull()
    java.lang.String remotePath, boolean saveProfile, @org.jetbrains.annotations.NotNull()
    java.lang.String profileName, @org.jetbrains.annotations.Nullable()
    java.lang.String hostError, @org.jetbrains.annotations.Nullable()
    java.lang.String portError, @org.jetbrains.annotations.Nullable()
    java.lang.String usernameError, @org.jetbrains.annotations.Nullable()
    java.lang.String localPathError, @org.jetbrains.annotations.Nullable()
    java.lang.String remotePathError, boolean isConnecting, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, @org.jetbrains.annotations.Nullable()
    dev.nettools.android.ui.transfer.PendingHostKey pendingHostKey) {
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