package dev.nettools.android.service;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class TransferProgressHolder_Factory implements Factory<TransferProgressHolder> {
  @Override
  public TransferProgressHolder get() {
    return newInstance();
  }

  public static TransferProgressHolder_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TransferProgressHolder newInstance() {
    return new TransferProgressHolder();
  }

  private static final class InstanceHolder {
    static final TransferProgressHolder_Factory INSTANCE = new TransferProgressHolder_Factory();
  }
}
