package dev.nettools.android.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.ssh.ScpClient;
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
public final class SshModule_ProvideScpClientFactory implements Factory<ScpClient> {
  @Override
  public ScpClient get() {
    return provideScpClient();
  }

  public static SshModule_ProvideScpClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ScpClient provideScpClient() {
    return Preconditions.checkNotNullFromProvides(SshModule.INSTANCE.provideScpClient());
  }

  private static final class InstanceHolder {
    static final SshModule_ProvideScpClientFactory INSTANCE = new SshModule_ProvideScpClientFactory();
  }
}
