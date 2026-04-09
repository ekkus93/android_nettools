package dev.nettools.android.data.ssh;

import dev.nettools.android.data.security.KnownHostsManager;
import dev.nettools.android.data.security.KnownHostsManager.VerificationResult;
import dev.nettools.android.domain.model.AuthType;
import dev.nettools.android.domain.model.TransferError;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import java.security.PublicKey;
import javax.inject.Inject;

/**
 * Manages SSHJ [SSHClient] connections.
 * Handles host key verification via [KnownHostsManager] and supports
 * both password and private-key authentication.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003JF\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00072\u0006\u0010\u000b\u001a\u00020\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u000f\u001a\u00020\u0010J \u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u000f\u001a\u00020\u0010H\u0002\u00a8\u0006\u0014"}, d2 = {"Ldev/nettools/android/data/ssh/SshConnectionManager;", "", "<init>", "()V", "connect", "Lnet/schmizz/sshj/SSHClient;", "host", "", "port", "", "username", "authType", "Ldev/nettools/android/domain/model/AuthType;", "password", "keyPath", "knownHostsManager", "Ldev/nettools/android/data/security/KnownHostsManager;", "buildHostKeyVerifier", "Lnet/schmizz/sshj/transport/verification/HostKeyVerifier;", "Companion", "app_debug"})
public final class SshConnectionManager {
    private static final int CONNECT_TIMEOUT_MS = 30000;
    @org.jetbrains.annotations.NotNull()
    public static final dev.nettools.android.data.ssh.SshConnectionManager.Companion Companion = null;
    
    @javax.inject.Inject()
    public SshConnectionManager() {
        super();
    }
    
    /**
     * Opens and authenticates an SSH connection.
     *
     * The caller is responsible for closing the returned [SSHClient] in a
     * `finally` block or `use {}` pattern.
     *
     * @param host Remote hostname or IP address.
     * @param port SSH port number.
     * @param username Remote account username.
     * @param authType Authentication method.
     * @param password Password string; required when [authType] is [AuthType.PASSWORD].
     * @param keyPath Path to a private key file; required when [authType] is [AuthType.PRIVATE_KEY].
     * @param knownHostsManager Manager used to verify the remote host key.
     * @return An authenticated, connected [SSHClient].
     * @throws TransferError on authentication, host-key, or connectivity failure.
     */
    @kotlin.jvm.Throws(exceptionClasses = {dev.nettools.android.domain.model.TransferError.class})
    @org.jetbrains.annotations.NotNull()
    public final net.schmizz.sshj.SSHClient connect(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.AuthType authType, @org.jetbrains.annotations.Nullable()
    java.lang.String password, @org.jetbrains.annotations.Nullable()
    java.lang.String keyPath, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.data.security.KnownHostsManager knownHostsManager) throws dev.nettools.android.domain.model.TransferError {
        return null;
    }
    
    /**
     * Builds a [HostKeyVerifier] that delegates trust decisions to [KnownHostsManager].
     *
     * @param host Remote hostname.
     * @param port SSH port.
     * @param knownHostsManager The manager holding persisted fingerprints.
     */
    private final net.schmizz.sshj.transport.verification.HostKeyVerifier buildHostKeyVerifier(java.lang.String host, int port, dev.nettools.android.data.security.KnownHostsManager knownHostsManager) {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Ldev/nettools/android/data/ssh/SshConnectionManager$Companion;", "", "<init>", "()V", "CONNECT_TIMEOUT_MS", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}