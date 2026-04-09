package dev.nettools.android.data.ssh;

import dev.nettools.android.domain.model.TransferError;

/**
 * Maps low-level SSH and IO exceptions to [TransferError] sealed class values,
 * so that the UI layer never has to handle raw exceptions.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0004\u001a\u00020\u00052\n\u0010\u0006\u001a\u00060\u0007j\u0002`\bJ\u0014\u0010\t\u001a\u00020\n2\n\u0010\u0006\u001a\u00060\u0007j\u0002`\bH\u0002J\u0014\u0010\u000b\u001a\u00020\n2\n\u0010\u0006\u001a\u00060\u0007j\u0002`\bH\u0002J\u0014\u0010\f\u001a\u00020\n2\n\u0010\u0006\u001a\u00060\u0007j\u0002`\bH\u0002J\u0014\u0010\r\u001a\u00020\n2\n\u0010\u0006\u001a\u00060\u0007j\u0002`\bH\u0002\u00a8\u0006\u000e"}, d2 = {"Ldev/nettools/android/data/ssh/ErrorMapper;", "", "<init>", "()V", "mapException", "Ldev/nettools/android/domain/model/TransferError;", "e", "Ljava/lang/Exception;", "Lkotlin/Exception;", "isAuthFailure", "", "isHostUnreachable", "isPermissionDenied", "isDiskFull", "app_debug"})
public final class ErrorMapper {
    @org.jetbrains.annotations.NotNull()
    public static final dev.nettools.android.data.ssh.ErrorMapper INSTANCE = null;
    
    private ErrorMapper() {
        super();
    }
    
    /**
     * Converts any [Exception] to the most specific [TransferError] subclass.
     *
     * @param e The exception to map.
     * @return A [TransferError] representing the failure.
     */
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.model.TransferError mapException(@org.jetbrains.annotations.NotNull()
    java.lang.Exception e) {
        return null;
    }
    
    private final boolean isAuthFailure(java.lang.Exception e) {
        return false;
    }
    
    private final boolean isHostUnreachable(java.lang.Exception e) {
        return false;
    }
    
    private final boolean isPermissionDenied(java.lang.Exception e) {
        return false;
    }
    
    private final boolean isDiskFull(java.lang.Exception e) {
        return false;
    }
}