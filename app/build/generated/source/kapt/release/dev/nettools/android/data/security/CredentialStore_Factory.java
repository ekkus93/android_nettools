package dev.nettools.android.data.security;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class CredentialStore_Factory implements Factory<CredentialStore> {
  private final Provider<Context> contextProvider;

  public CredentialStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CredentialStore get() {
    return newInstance(contextProvider.get());
  }

  public static CredentialStore_Factory create(Provider<Context> contextProvider) {
    return new CredentialStore_Factory(contextProvider);
  }

  public static CredentialStore newInstance(Context context) {
    return new CredentialStore(context);
  }
}
