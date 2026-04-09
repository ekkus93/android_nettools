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
public final class ScpClient_Factory implements Factory<ScpClient> {
  @Override
  public ScpClient get() {
    return newInstance();
  }

  public static ScpClient_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ScpClient newInstance() {
    return new ScpClient();
  }

  private static final class InstanceHolder {
    static final ScpClient_Factory INSTANCE = new ScpClient_Factory();
  }
}
