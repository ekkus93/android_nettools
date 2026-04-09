package dev.nettools.android.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.security.KnownHostsManager;
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
public final class SshModule_ProvideKnownHostsManagerFactory implements Factory<KnownHostsManager> {
  private final Provider<KnownHostRepository> repositoryProvider;

  public SshModule_ProvideKnownHostsManagerFactory(
      Provider<KnownHostRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public KnownHostsManager get() {
    return provideKnownHostsManager(repositoryProvider.get());
  }

  public static SshModule_ProvideKnownHostsManagerFactory create(
      Provider<KnownHostRepository> repositoryProvider) {
    return new SshModule_ProvideKnownHostsManagerFactory(repositoryProvider);
  }

  public static KnownHostsManager provideKnownHostsManager(KnownHostRepository repository) {
    return Preconditions.checkNotNullFromProvides(SshModule.INSTANCE.provideKnownHostsManager(repository));
  }
}
