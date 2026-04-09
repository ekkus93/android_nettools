package dev.nettools.android.ui.sftp;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.security.KnownHostsManager;
import dev.nettools.android.data.ssh.SftpClient;
import dev.nettools.android.data.ssh.SshConnectionManager;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class SftpBrowserViewModel_Factory implements Factory<SftpBrowserViewModel> {
  private final Provider<SshConnectionManager> sshConnectionManagerProvider;

  private final Provider<SftpClient> sftpClientProvider;

  private final Provider<KnownHostsManager> knownHostsManagerProvider;

  public SftpBrowserViewModel_Factory(Provider<SshConnectionManager> sshConnectionManagerProvider,
      Provider<SftpClient> sftpClientProvider,
      Provider<KnownHostsManager> knownHostsManagerProvider) {
    this.sshConnectionManagerProvider = sshConnectionManagerProvider;
    this.sftpClientProvider = sftpClientProvider;
    this.knownHostsManagerProvider = knownHostsManagerProvider;
  }

  @Override
  public SftpBrowserViewModel get() {
    return newInstance(sshConnectionManagerProvider.get(), sftpClientProvider.get(), knownHostsManagerProvider.get());
  }

  public static SftpBrowserViewModel_Factory create(
      Provider<SshConnectionManager> sshConnectionManagerProvider,
      Provider<SftpClient> sftpClientProvider,
      Provider<KnownHostsManager> knownHostsManagerProvider) {
    return new SftpBrowserViewModel_Factory(sshConnectionManagerProvider, sftpClientProvider, knownHostsManagerProvider);
  }

  public static SftpBrowserViewModel newInstance(SshConnectionManager sshConnectionManager,
      SftpClient sftpClient, KnownHostsManager knownHostsManager) {
    return new SftpBrowserViewModel(sshConnectionManager, sftpClient, knownHostsManager);
  }
}
