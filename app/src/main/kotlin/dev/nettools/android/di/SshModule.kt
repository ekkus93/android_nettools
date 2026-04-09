package dev.nettools.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.nettools.android.data.security.CredentialStore
import dev.nettools.android.data.security.KnownHostsManager
import dev.nettools.android.data.ssh.ScpClient
import dev.nettools.android.data.ssh.SftpClient
import dev.nettools.android.data.ssh.SshConnectionManager
import dev.nettools.android.domain.repository.KnownHostRepository
import javax.inject.Singleton

/**
 * Hilt DI module that provides SSH/SCP/SFTP infrastructure and security components.
 */
@Module
@InstallIn(SingletonComponent::class)
object SshModule {

    /**
     * Provides the singleton [SshConnectionManager].
     */
    @Provides
    @Singleton
    fun provideSshConnectionManager(): SshConnectionManager = SshConnectionManager()

    /**
     * Provides the singleton [ScpClient].
     */
    @Provides
    @Singleton
    fun provideScpClient(): ScpClient = ScpClient()

    /**
     * Provides the singleton [SftpClient].
     */
    @Provides
    @Singleton
    fun provideSftpClient(): SftpClient = SftpClient()

    /**
     * Provides the singleton [KnownHostsManager].
     *
     * @param repository The [KnownHostRepository] used to persist fingerprints.
     */
    @Provides
    @Singleton
    fun provideKnownHostsManager(
        repository: KnownHostRepository
    ): KnownHostsManager = KnownHostsManager(repository)
}
