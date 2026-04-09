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
public final class SftpClient_Factory implements Factory<SftpClient> {
  @Override
  public SftpClient get() {
    return newInstance();
  }

  public static SftpClient_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SftpClient newInstance() {
    return new SftpClient();
  }

  private static final class InstanceHolder {
    static final SftpClient_Factory INSTANCE = new SftpClient_Factory();
  }
}
