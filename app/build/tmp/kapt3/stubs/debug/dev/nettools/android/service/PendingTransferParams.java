package dev.nettools.android.service;

import dev.nettools.android.domain.model.AuthType;
import dev.nettools.android.domain.model.TransferJob;

/**
 * Holds all parameters required to execute an SSH transfer.
 * Stored in memory (never serialised to disk) so that credentials
 * never leave the process boundary.
 *
 * @property job The [TransferJob] descriptor.
 * @property host Remote hostname or IP address.
 * @property port SSH port number.
 * @property username Remote account username.
 * @property authType Authentication method.
 * @property password Plaintext password; present only for [AuthType.PASSWORD].
 * @property keyPath Local filesystem path to the private key file.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0018\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001BC\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0005\u0012\u0006\u0010\t\u001a\u00020\n\u0012\b\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\f\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0004\b\r\u0010\u000eJ\t\u0010\u001a\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\nH\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\u0005H\u00c6\u0003JS\u0010!\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\n2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001J\u0013\u0010\"\u001a\u00020#2\b\u0010$\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010%\u001a\u00020\u0007H\u00d6\u0001J\t\u0010&\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0012R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0012R\u0013\u0010\f\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0012\u00a8\u0006\'"}, d2 = {"Ldev/nettools/android/service/PendingTransferParams;", "", "job", "Ldev/nettools/android/domain/model/TransferJob;", "host", "", "port", "", "username", "authType", "Ldev/nettools/android/domain/model/AuthType;", "password", "keyPath", "<init>", "(Ldev/nettools/android/domain/model/TransferJob;Ljava/lang/String;ILjava/lang/String;Ldev/nettools/android/domain/model/AuthType;Ljava/lang/String;Ljava/lang/String;)V", "getJob", "()Ldev/nettools/android/domain/model/TransferJob;", "getHost", "()Ljava/lang/String;", "getPort", "()I", "getUsername", "getAuthType", "()Ldev/nettools/android/domain/model/AuthType;", "getPassword", "getKeyPath", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class PendingTransferParams {
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.domain.model.TransferJob job = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String host = null;
    private final int port = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String username = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.domain.model.AuthType authType = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String password = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String keyPath = null;
    
    public PendingTransferParams(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferJob job, @org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.AuthType authType, @org.jetbrains.annotations.Nullable()
    java.lang.String password, @org.jetbrains.annotations.Nullable()
    java.lang.String keyPath) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.model.TransferJob getJob() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getHost() {
        return null;
    }
    
    public final int getPort() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUsername() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.model.AuthType getAuthType() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPassword() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getKeyPath() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.model.TransferJob component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    public final int component3() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.model.AuthType component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.service.PendingTransferParams copy(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferJob job, @org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.AuthType authType, @org.jetbrains.annotations.Nullable()
    java.lang.String password, @org.jetbrains.annotations.Nullable()
    java.lang.String keyPath) {
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