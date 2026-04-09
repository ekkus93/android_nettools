package dev.nettools.android.di;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dev.nettools.android.data.security.CredentialStore;
import dev.nettools.android.data.security.KnownHostsManager;
import dev.nettools.android.data.ssh.ScpClient;
import dev.nettools.android.data.ssh.SftpClient;
import dev.nettools.android.data.ssh.SshConnectionManager;
import dev.nettools.android.domain.repository.KnownHostRepository;
import javax.inject.Singleton;

/**
 * Hilt DI module that provides SSH/SCP/SFTP infrastructure and security components.
 */
@dagger.Module()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0007J\b\u0010\u0006\u001a\u00020\u0007H\u0007J\b\u0010\b\u001a\u00020\tH\u0007J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0007\u00a8\u0006\u000e"}, d2 = {"Ldev/nettools/android/di/SshModule;", "", "<init>", "()V", "provideSshConnectionManager", "Ldev/nettools/android/data/ssh/SshConnectionManager;", "provideScpClient", "Ldev/nettools/android/data/ssh/ScpClient;", "provideSftpClient", "Ldev/nettools/android/data/ssh/SftpClient;", "provideKnownHostsManager", "Ldev/nettools/android/data/security/KnownHostsManager;", "repository", "Ldev/nettools/android/domain/repository/KnownHostRepository;", "app_release"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class SshModule {
    @org.jetbrains.annotations.NotNull()
    public static final dev.nettools.android.di.SshModule INSTANCE = null;
    
    private SshModule() {
        super();
    }
    
    /**
     * Provides the singleton [SshConnectionManager].
     */
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.data.ssh.SshConnectionManager provideSshConnectionManager() {
        return null;
    }
    
    /**
     * Provides the singleton [ScpClient].
     */
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.data.ssh.ScpClient provideScpClient() {
        return null;
    }
    
    /**
     * Provides the singleton [SftpClient].
     */
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.data.ssh.SftpClient provideSftpClient() {
        return null;
    }
    
    /**
     * Provides the singleton [KnownHostsManager].
     *
     * @param repository The [KnownHostRepository] used to persist fingerprints.
     */
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final dev.nettools.android.data.security.KnownHostsManager provideKnownHostsManager(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.repository.KnownHostRepository repository) {
        return null;
    }
}