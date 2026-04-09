package dev.nettools.android.data.ssh;

/**
 * Snapshot of transfer progress for a single file.
 *
 * @property fileName Name of the file being transferred.
 * @property bytesTransferred Number of bytes transferred so far.
 * @property totalBytes Total file size in bytes; -1 if unknown.
 * @property speedBytesPerSec Current transfer speed in bytes per second.
 * @property isResuming Whether the transfer is resuming a previous partial transfer.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0013\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B1\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u00a2\u0006\u0004\b\u000b\u0010\fJ\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\bH\u00c6\u0003J\t\u0010\u0019\u001a\u00020\nH\u00c6\u0003J;\u0010\u001a\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\nH\u00c6\u0001J\u0013\u0010\u001b\u001a\u00020\n2\b\u0010\u001c\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001d\u001a\u00020\u001eH\u00d6\u0001J\t\u0010\u001f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0010R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u0014\u00a8\u0006 "}, d2 = {"Ldev/nettools/android/data/ssh/TransferProgress;", "", "fileName", "", "bytesTransferred", "", "totalBytes", "speedBytesPerSec", "", "isResuming", "", "<init>", "(Ljava/lang/String;JJDZ)V", "getFileName", "()Ljava/lang/String;", "getBytesTransferred", "()J", "getTotalBytes", "getSpeedBytesPerSec", "()D", "()Z", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class TransferProgress {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String fileName = null;
    private final long bytesTransferred = 0L;
    private final long totalBytes = 0L;
    private final double speedBytesPerSec = 0.0;
    private final boolean isResuming = false;
    
    public TransferProgress(@org.jetbrains.annotations.NotNull()
    java.lang.String fileName, long bytesTransferred, long totalBytes, double speedBytesPerSec, boolean isResuming) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFileName() {
        return null;
    }
    
    public final long getBytesTransferred() {
        return 0L;
    }
    
    public final long getTotalBytes() {
        return 0L;
    }
    
    public final double getSpeedBytesPerSec() {
        return 0.0;
    }
    
    public final boolean isResuming() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final long component2() {
        return 0L;
    }
    
    public final long component3() {
        return 0L;
    }
    
    public final double component4() {
        return 0.0;
    }
    
    public final boolean component5() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.data.ssh.TransferProgress copy(@org.jetbrains.annotations.NotNull()
    java.lang.String fileName, long bytesTransferred, long totalBytes, double speedBytesPerSec, boolean isResuming) {
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