package dev.nettools.android.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.ssh.SftpClient;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SshModule_ProvideSftpClientFactory implements Factory<SftpClient> {
  @Override
  public SftpClient get() {
    return provideSftpClient();
  }

  public static SshModule_ProvideSftpClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SftpClient provideSftpClient() {
    return Preconditions.checkNotNullFromProvides(SshModule.INSTANCE.provideSftpClient());
  }

  private static final class InstanceHolder {
    static final SshModule_ProvideSftpClientFactory INSTANCE = new SshModule_ProvideSftpClientFactory();
  }
}
