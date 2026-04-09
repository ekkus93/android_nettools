package dev.nettools.android.data.ssh;

import dev.nettools.android.domain.model.RemoteFileEntry;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import javax.inject.Inject;

/**
 * Provides SFTP directory and file management operations using SSHJ.
 * Every method opens a dedicated SFTP client and closes it via [use].
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\t\n\u0000\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J$\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ \u0010\f\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ\u001e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ&\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u0012J\u001e\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ \u0010\u0014\u001a\u0004\u0018\u00010\u00152\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000b\u00a8\u0006\u0016"}, d2 = {"Ldev/nettools/android/data/ssh/SftpClient;", "", "<init>", "()V", "listDirectory", "", "Ldev/nettools/android/domain/model/RemoteFileEntry;", "sshClient", "Lnet/schmizz/sshj/SSHClient;", "path", "", "(Lnet/schmizz/sshj/SSHClient;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "stat", "mkdir", "", "rename", "fromPath", "toPath", "(Lnet/schmizz/sshj/SSHClient;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "getFileSize", "", "app_release"})
public final class SftpClient {
    
    @javax.inject.Inject()
    public SftpClient() {
        super();
    }
    
    /**
     * Lists the contents of a remote directory.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param path Absolute remote directory path.
     * @return List of [RemoteFileEntry] objects.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object listDirectory(@org.jetbrains.annotations.NotNull()
    net.schmizz.sshj.SSHClient sshClient, @org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<dev.nettools.android.domain.model.RemoteFileEntry>> $completion) {
        return null;
    }
    
    /**
     * Returns metadata for a single remote file or directory, or null if not found.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param path Absolute remote path.
     * @return [RemoteFileEntry] or null.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object stat(@org.jetbrains.annotations.NotNull()
    net.schmizz.sshj.SSHClient sshClient, @org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super dev.nettools.android.domain.model.RemoteFileEntry> $completion) {
        return null;
    }
    
    /**
     * Creates a directory on the remote host.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param path Absolute remote path of the directory to create.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object mkdir(@org.jetbrains.annotations.NotNull()
    net.schmizz.sshj.SSHClient sshClient, @org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Renames (moves) a remote file or directory.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param fromPath Source path.
     * @param toPath Destination path.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object rename(@org.jetbrains.annotations.NotNull()
    net.schmizz.sshj.SSHClient sshClient, @org.jetbrains.annotations.NotNull()
    java.lang.String fromPath, @org.jetbrains.annotations.NotNull()
    java.lang.String toPath, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Deletes a remote file.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param path Absolute remote path to delete.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    net.schmizz.sshj.SSHClient sshClient, @org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Returns the size of a remote file in bytes, or null if the file does not exist.
     *
     * @param sshClient An authenticated [SSHClient].
     * @param path Absolute remote path.
     * @return File size in bytes, or null.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getFileSize(@org.jetbrains.annotations.NotNull()
    net.schmizz.sshj.SSHClient sshClient, @org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
}