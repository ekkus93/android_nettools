package dev.nettools.android.data.security;

import dev.nettools.android.domain.repository.KnownHostRepository;
import java.security.MessageDigest;
import java.util.Base64;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Manages SSH host key verification using a Trust-On-First-Use (TOFU) policy.
 * Fingerprints are stored via [KnownHostRepository] and compared on subsequent connections.
 *
 * @param repository Persistent storage for known host fingerprints.
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001:\u0001\u0015B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J&\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\t2\u0006\u0010\r\u001a\u00020\u000eJ\u001e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\tJ\u0006\u0010\u0012\u001a\u00020\u0010J\u0018\u0010\u0013\u001a\u0004\u0018\u00010\t2\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\u0014\u001a\u00020\t2\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Ldev/nettools/android/data/security/KnownHostsManager;", "", "repository", "Ldev/nettools/android/domain/repository/KnownHostRepository;", "<init>", "(Ldev/nettools/android/domain/repository/KnownHostRepository;)V", "checkAndVerify", "Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult;", "host", "", "port", "", "keyType", "keyBytes", "", "acceptHost", "", "fingerprint", "rejectHost", "getStoredFingerprint", "computeFingerprint", "VerificationResult", "app_release"})
public final class KnownHostsManager {
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.domain.repository.KnownHostRepository repository = null;
    
    @javax.inject.Inject()
    public KnownHostsManager(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.repository.KnownHostRepository repository) {
        super();
    }
    
    /**
     * Checks whether the presented host key should be trusted.
     *
     * @param host Remote hostname or IP address.
     * @param port SSH port number.
     * @param keyType Key algorithm (e.g. "RSA", "EC").
     * @param keyBytes Raw encoded public key bytes.
     * @return A [VerificationResult] indicating the trust decision.
     */
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.data.security.KnownHostsManager.VerificationResult checkAndVerify(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    java.lang.String keyType, @org.jetbrains.annotations.NotNull()
    byte[] keyBytes) {
        return null;
    }
    
    /**
     * Persists a trusted fingerprint for the given host, accepting it for future connections.
     *
     * @param host Remote hostname or IP address.
     * @param port SSH port number.
     * @param fingerprint SHA-256 fingerprint to trust.
     */
    public final void acceptHost(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    java.lang.String fingerprint) {
    }
    
    /**
     * Rejects the host key — no-op, nothing is persisted.
     */
    public final void rejectHost() {
    }
    
    /**
     * Returns the stored fingerprint for [host]:[port], or null if not yet trusted.
     *
     * @param host Remote hostname or IP address.
     * @param port SSH port number.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getStoredFingerprint(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port) {
        return null;
    }
    
    /**
     * Computes the SHA-256 fingerprint of a public key in "SHA256:xxxx" format,
     * matching the OpenSSH fingerprint style.
     *
     * @param keyBytes Raw encoded public key bytes.
     * @return Fingerprint string prefixed with "SHA256:".
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String computeFingerprint(@org.jetbrains.annotations.NotNull()
    byte[] keyBytes) {
        return null;
    }
    
    /**
     * Result of a host key verification attempt.
     */
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0004\u0004\u0005\u0006\u0007B\t\b\u0004\u00a2\u0006\u0004\b\u0002\u0010\u0003\u0082\u0001\u0004\b\t\n\u000b\u00a8\u0006\f"}, d2 = {"Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult;", "", "<init>", "()V", "Trusted", "FirstConnect", "KeyChanged", "Rejected", "Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult$FirstConnect;", "Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult$KeyChanged;", "Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult$Rejected;", "Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult$Trusted;", "app_release"})
    public static abstract class VerificationResult {
        
        private VerificationResult() {
            super();
        }
        
        /**
         * No key has been seen for this host before.
         * @property fingerprint SHA-256 fingerprint of the presented key.
         */
        @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\t\u0010\b\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\t\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u00d6\u0003J\t\u0010\u000e\u001a\u00020\u000fH\u00d6\u0001J\t\u0010\u0010\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0011"}, d2 = {"Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult$FirstConnect;", "Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult;", "fingerprint", "", "<init>", "(Ljava/lang/String;)V", "getFingerprint", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_release"})
        public static final class FirstConnect extends dev.nettools.android.data.security.KnownHostsManager.VerificationResult {
            @org.jetbrains.annotations.NotNull()
            private final java.lang.String fingerprint = null;
            
            public FirstConnect(@org.jetbrains.annotations.NotNull()
            java.lang.String fingerprint) {
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String getFingerprint() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final dev.nettools.android.data.security.KnownHostsManager.VerificationResult.FirstConnect copy(@org.jetbrains.annotations.NotNull()
            java.lang.String fingerprint) {
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
        
        /**
         * The key has changed since the last trusted connection — possible MITM attack.
         * @property old Previously trusted fingerprint.
         * @property new Fingerprint of the currently presented key.
         */
        @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\n\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\u001d\u0010\f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u00d6\u0003J\t\u0010\u0011\u001a\u00020\u0012H\u00d6\u0001J\t\u0010\u0013\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b\u00a8\u0006\u0014"}, d2 = {"Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult$KeyChanged;", "Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult;", "old", "", "new", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "getOld", "()Ljava/lang/String;", "getNew", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_release"})
        public static final class KeyChanged extends dev.nettools.android.data.security.KnownHostsManager.VerificationResult {
            @org.jetbrains.annotations.NotNull()
            private final java.lang.String old = null;
            
            public KeyChanged(@org.jetbrains.annotations.NotNull()
            java.lang.String old, @org.jetbrains.annotations.NotNull()
            java.lang.String p1_54480) {
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String getOld() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String getNew() {
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
            
            @org.jetbrains.annotations.NotNull()
            public final dev.nettools.android.data.security.KnownHostsManager.VerificationResult.KeyChanged copy(@org.jetbrains.annotations.NotNull()
            java.lang.String old, @org.jetbrains.annotations.NotNull()
            java.lang.String p1_54480) {
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
        
        /**
         * The key was explicitly rejected.
         */
        @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\n\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0013\u0010\u0004\u001a\u00020\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u00d6\u0003J\t\u0010\b\u001a\u00020\tH\u00d6\u0001J\t\u0010\n\u001a\u00020\u000bH\u00d6\u0001\u00a8\u0006\f"}, d2 = {"Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult$Rejected;", "Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult;", "<init>", "()V", "equals", "", "other", "", "hashCode", "", "toString", "", "app_release"})
        public static final class Rejected extends dev.nettools.android.data.security.KnownHostsManager.VerificationResult {
            @org.jetbrains.annotations.NotNull()
            public static final dev.nettools.android.data.security.KnownHostsManager.VerificationResult.Rejected INSTANCE = null;
            
            private Rejected() {
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
        
        /**
         * The key matches a previously trusted fingerprint.
         */
        @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\n\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0013\u0010\u0004\u001a\u00020\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u00d6\u0003J\t\u0010\b\u001a\u00020\tH\u00d6\u0001J\t\u0010\n\u001a\u00020\u000bH\u00d6\u0001\u00a8\u0006\f"}, d2 = {"Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult$Trusted;", "Ldev/nettools/android/data/security/KnownHostsManager$VerificationResult;", "<init>", "()V", "equals", "", "other", "", "hashCode", "", "toString", "", "app_release"})
        public static final class Trusted extends dev.nettools.android.data.security.KnownHostsManager.VerificationResult {
            @org.jetbrains.annotations.NotNull()
            public static final dev.nettools.android.data.security.KnownHostsManager.VerificationResult.Trusted INSTANCE = null;
            
            private Trusted() {
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
    }
}