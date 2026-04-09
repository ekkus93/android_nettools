package dev.nettools.android.data.ssh;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class SshConnectionManager_Factory implements Factory<SshConnectionManager> {
  @Override
  public SshConnectionManager get() {
    return newInstance();
  }

  public static SshConnectionManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SshConnectionManager newInstance() {
    return new SshConnectionManager();
  }

  private static final class InstanceHolder {
    static final SshConnectionManager_Factory INSTANCE = new SshConnectionManager_Factory();
  }
}
