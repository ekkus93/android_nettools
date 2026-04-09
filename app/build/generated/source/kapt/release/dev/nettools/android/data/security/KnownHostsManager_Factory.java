package dev.nettools.android.data.security;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.domain.repository.KnownHostRepository;
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
public final class KnownHostsManager_Factory implements Factory<KnownHostsManager> {
  private final Provider<KnownHostRepository> repositoryProvider;

  public KnownHostsManager_Factory(Provider<KnownHostRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public KnownHostsManager get() {
    return newInstance(repositoryProvider.get());
  }

  public static KnownHostsManager_Factory create(Provider<KnownHostRepository> repositoryProvider) {
    return new KnownHostsManager_Factory(repositoryProvider);
  }

  public static KnownHostsManager newInstance(KnownHostRepository repository) {
    return new KnownHostsManager(repository);
  }
}
