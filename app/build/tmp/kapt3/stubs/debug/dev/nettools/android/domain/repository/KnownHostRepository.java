package dev.nettools.android.domain.repository;

/**
 * Repository interface for known SSH host key fingerprints (TOFU store).
 * Fingerprints are stored as "SHA256:xxxx" strings keyed by host+port.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J \u0010\u0002\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u0006H\u00a6@\u00a2\u0006\u0002\u0010\u0007J&\u0010\b\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u000bJ\u001e\u0010\f\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u0006H\u00a6@\u00a2\u0006\u0002\u0010\u0007\u00a8\u0006\r\u00c0\u0006\u0003"}, d2 = {"Ldev/nettools/android/domain/repository/KnownHostRepository;", "", "getByHost", "", "host", "port", "", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "save", "", "fingerprint", "(Ljava/lang/String;ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "app_debug"})
public abstract interface KnownHostRepository {
    
    /**
     * Returns the stored fingerprint for the given host and port, or null if unknown.
     *
     * @param host Hostname or IP address.
     * @param port SSH port number.
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getByHost(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    /**
     * Persists a trusted fingerprint for the given host and port.
     *
     * @param host Hostname or IP address.
     * @param port SSH port number.
     * @param fingerprint SHA-256 fingerprint string.
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object save(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    java.lang.String fingerprint, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Removes the known-host entry for the given host and port.
     *
     * @param host Hostname or IP address.
     * @param port SSH port number.
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    java.lang.String host, int port, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}