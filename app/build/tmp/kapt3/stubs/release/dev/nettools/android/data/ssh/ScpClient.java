package dev.nettools.android.data.ssh;

import kotlinx.coroutines.flow.Flow;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.FileSystemFile;
import java.io.File;
import javax.inject.Inject;

/**
 * Provides streaming SCP file transfers using SSHJ's built-in SCP support.
 * All methods return a [Flow] of [TransferProgress] and support coroutine cancellation.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J$\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ$\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\t\u001a\u00020\nJ$\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ$\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\n\u00a8\u0006\u0011"}, d2 = {"Ldev/nettools/android/data/ssh/ScpClient;", "", "<init>", "()V", "upload", "Lkotlinx/coroutines/flow/Flow;", "Ldev/nettools/android/data/ssh/TransferProgress;", "sshClient", "Lnet/schmizz/sshj/SSHClient;", "localFile", "Ljava/io/File;", "remotePath", "", "download", "uploadDirectory", "localDir", "downloadDirectory", "app_release"})
public final class ScpClient {
    
    @javax.inject.Inject()
    public ScpClient() {
        super();
    }
    
    /**
     * Uploads a single local file to the remote host via SCP.
     * Uses a `.part` suffix during upload; renames to [remotePath] on completion.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param localFile Local file to upload.
     * @param remotePath Destination path on the remote host (without `.part` suffix).
     * @return Flow of [TransferProgress] updates.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<dev.nettools.android.data.ssh.TransferProgress> upload(@org.jetbrains.annotations.NotNull()
    net.schmizz.sshj.SSHClient sshClient, @org.jetbrains.annotations.NotNull()
    java.io.File localFile, @org.jetbrains.annotations.NotNull()
    java.lang.String remotePath) {
        return null;
    }
    
    /**
     * Downloads a remote file to a local destination via SCP.
     * Checks existing local file size to support resume detection.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param remotePath Source path on the remote host.
     * @param localFile Local destination file.
     * @return Flow of [TransferProgress] updates.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<dev.nettools.android.data.ssh.TransferProgress> download(@org.jetbrains.annotations.NotNull()
    net.schmizz.sshj.SSHClient sshClient, @org.jetbrains.annotations.NotNull()
    java.lang.String remotePath, @org.jetbrains.annotations.NotNull()
    java.io.File localFile) {
        return null;
    }
    
    /**
     * Recursively uploads a local directory to the remote host via SCP.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param localDir Local directory to upload.
     * @param remotePath Destination directory path on the remote host.
     * @return Flow of [TransferProgress] updates.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<dev.nettools.android.data.ssh.TransferProgress> uploadDirectory(@org.jetbrains.annotations.NotNull()
    net.schmizz.sshj.SSHClient sshClient, @org.jetbrains.annotations.NotNull()
    java.io.File localDir, @org.jetbrains.annotations.NotNull()
    java.lang.String remotePath) {
        return null;
    }
    
    /**
     * Recursively downloads a remote directory to the local filesystem via SCP.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param remotePath Source directory path on the remote host.
     * @param localDir Local destination directory.
     * @return Flow of [TransferProgress] updates.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<dev.nettools.android.data.ssh.TransferProgress> downloadDirectory(@org.jetbrains.annotations.NotNull()
    net.schmizz.sshj.SSHClient sshClient, @org.jetbrains.annotations.NotNull()
    java.lang.String remotePath, @org.jetbrains.annotations.NotNull()
    java.io.File localDir) {
        return null;
    }
}