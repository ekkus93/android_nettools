package dev.nettools.android.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.ssh.SshConnectionManager;
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
public final class SshModule_ProvideSshConnectionManagerFactory implements Factory<SshConnectionManager> {
  @Override
  public SshConnectionManager get() {
    return provideSshConnectionManager();
  }

  public static SshModule_ProvideSshConnectionManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SshConnectionManager provideSshConnectionManager() {
    return Preconditions.checkNotNullFromProvides(SshModule.INSTANCE.provideSshConnectionManager());
  }

  private static final class InstanceHolder {
    static final SshModule_ProvideSshConnectionManagerFactory INSTANCE = new SshModule_ProvideSshConnectionManagerFactory();
  }
}
