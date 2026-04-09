package dev.nettools.android.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.repository.KnownHostRepositoryImpl;
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
public final class DatabaseModule_ProvideKnownHostRepositoryFactory implements Factory<KnownHostRepository> {
  private final Provider<KnownHostRepositoryImpl> implProvider;

  public DatabaseModule_ProvideKnownHostRepositoryFactory(
      Provider<KnownHostRepositoryImpl> implProvider) {
    this.implProvider = implProvider;
  }

  @Override
  public KnownHostRepository get() {
    return provideKnownHostRepository(implProvider.get());
  }

  public static DatabaseModule_ProvideKnownHostRepositoryFactory create(
      Provider<KnownHostRepositoryImpl> implProvider) {
    return new DatabaseModule_ProvideKnownHostRepositoryFactory(implProvider);
  }

  public static KnownHostRepository provideKnownHostRepository(KnownHostRepositoryImpl impl) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideKnownHostRepository(impl));
  }
}
