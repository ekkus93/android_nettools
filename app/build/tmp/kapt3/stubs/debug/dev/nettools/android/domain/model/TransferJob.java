package dev.nettools.android.domain.model;

/**
 * Represents an in-progress or queued file transfer job.
 *
 * @property id Unique job identifier.
 * @property profileId ID of the [ConnectionProfile] to use.
 * @property direction Whether this is an upload or download.
 * @property localPath Absolute path on the Android device.
 * @property remotePath Absolute or relative path on the remote host.
 * @property status Current status of the job.
 * @property bytesTransferred Number of bytes transferred so far.
 * @property totalBytes Total file size in bytes; -1 if unknown.
 * @property errorMessage Human-readable error if the job failed.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u001c\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BY\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\f\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0004\b\u000f\u0010\u0010J\t\u0010\u001e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010 \u001a\u00020\u0006H\u00c6\u0003J\t\u0010!\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\t\u0010#\u001a\u00020\nH\u00c6\u0003J\t\u0010$\u001a\u00020\fH\u00c6\u0003J\t\u0010%\u001a\u00020\fH\u00c6\u0003J\u000b\u0010&\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003Je\u0010\'\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\f2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010(\u001a\u00020)2\b\u0010*\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010+\u001a\u00020,H\u00d6\u0001J\t\u0010-\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0012R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0012R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0012R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\r\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001bR\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0012\u00a8\u0006."}, d2 = {"Ldev/nettools/android/domain/model/TransferJob;", "", "id", "", "profileId", "direction", "Ldev/nettools/android/domain/model/TransferDirection;", "localPath", "remotePath", "status", "Ldev/nettools/android/domain/model/TransferStatus;", "bytesTransferred", "", "totalBytes", "errorMessage", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ldev/nettools/android/domain/model/TransferDirection;Ljava/lang/String;Ljava/lang/String;Ldev/nettools/android/domain/model/TransferStatus;JJLjava/lang/String;)V", "getId", "()Ljava/lang/String;", "getProfileId", "getDirection", "()Ldev/nettools/android/domain/model/TransferDirection;", "getLocalPath", "getRemotePath", "getStatus", "()Ldev/nettools/android/domain/model/TransferStatus;", "getBytesTransferred", "()J", "getTotalBytes", "getErrorMessage", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class TransferJob {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String profileId = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.domain.model.TransferDirection direction = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String localPath = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String remotePath = null;
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.domain.model.TransferStatus status = null;
    private final long bytesTransferred = 0L;
    private final long totalBytes = 0L;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    
    public TransferJob(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String profileId, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferDirection direction, @org.jetbrains.annotations.NotNull()
    java.lang.String localPath, @org.jetbrains.annotations.NotNull()
    java.lang.String remotePath, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferStatus status, long bytesTransferred, long totalBytes, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getProfileId() {
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
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.model.TransferStatus getStatus() {
        return null;
    }
    
    public final long getBytesTransferred() {
        return 0L;
    }
    
    public final long getTotalBytes() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
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
    public final dev.nettools.android.domain.model.TransferDirection component3() {
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
    public final dev.nettools.android.domain.model.TransferStatus component6() {
        return null;
    }
    
    public final long component7() {
        return 0L;
    }
    
    public final long component8() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.domain.model.TransferJob copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String profileId, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferDirection direction, @org.jetbrains.annotations.NotNull()
    java.lang.String localPath, @org.jetbrains.annotations.NotNull()
    java.lang.String remotePath, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.model.TransferStatus status, long bytesTransferred, long totalBytes, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage) {
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