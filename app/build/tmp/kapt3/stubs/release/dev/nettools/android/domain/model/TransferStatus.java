package dev.nettools.android.domain.model;

/**
 * Status lifecycle of a [TransferJob].
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\t\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\t\u00a8\u0006\n"}, d2 = {"Ldev/nettools/android/domain/model/TransferStatus;", "", "<init>", "(Ljava/lang/String;I)V", "QUEUED", "IN_PROGRESS", "PAUSED", "COMPLETED", "FAILED", "CANCELLED", "app_release"})
public enum TransferStatus {
    /*public static final*/ QUEUED /* = new QUEUED() */,
    /*public static final*/ IN_PROGRESS /* = new IN_PROGRESS() */,
    /*public static final*/ PAUSED /* = new PAUSED() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ FAILED /* = new FAILED() */,
    /*public static final*/ CANCELLED /* = new CANCELLED() */;
    
    TransferStatus() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<dev.nettools.android.domain.model.TransferStatus> getEntries() {
        return null;
    }
}