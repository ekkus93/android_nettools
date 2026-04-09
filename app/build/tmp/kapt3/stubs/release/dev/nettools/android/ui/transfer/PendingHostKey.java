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
 * State for a pending TOFU (trust-on-first-use) host key dialog.
 *
 * @property host The remote hostname.
 * @property fingerprint SHA-256 fingerprint of the presented key.
 * @property isChanged Whether this is a key-change warning (vs. a new host).
 * @property oldFingerprint Previously stored fingerprint, present when [isChanged] is true.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0010\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0004\b\b\u0010\tJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0006H\u00c6\u0003J\u000b\u0010\u0012\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J3\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010\u0014\u001a\u00020\u00062\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0016\u001a\u00020\u0017H\u00d6\u0001J\t\u0010\u0018\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000bR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\rR\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000b\u00a8\u0006\u0019"}, d2 = {"Ldev/nettools/android/ui/transfer/PendingHostKey;", "", "host", "", "fingerprint", "isChanged", "", "oldFingerprint", "<init>", "(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)V", "getHost", "()Ljava/lang/String;", "getFingerprint", "()Z", "getOldFingerprint", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "", "toString", "app_release"})
public final class PendingHostKey {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String host = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String fingerprint = null;
    private final boolean isChanged = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String oldFingerprint = null;
    
    public PendingHostKey(@org.jetbrains.annotations.NotNull()
    java.lang.String host, @org.jetbrains.annotations.NotNull()
    java.lang.String fingerprint, boolean isChanged, @org.jetbrains.annotations.Nullable()
    java.lang.String oldFingerprint) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getHost() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFingerprint() {
        return null;
    }
    
    public final boolean isChanged() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getOldFingerprint() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.ui.transfer.PendingHostKey copy(@org.jetbrains.annotations.NotNull()
    java.lang.String host, @org.jetbrains.annotations.NotNull()
    java.lang.String fingerprint, boolean isChanged, @org.jetbrains.annotations.Nullable()
    java.lang.String oldFingerprint) {
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