package dev.nettools.android.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.repository.ConnectionProfileRepositoryImpl;
import dev.nettools.android.domain.repository.ConnectionProfileRepository;
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
public final class DatabaseModule_ProvideConnectionProfileRepositoryFactory implements Factory<ConnectionProfileRepository> {
  private final Provider<ConnectionProfileRepositoryImpl> implProvider;

  public DatabaseModule_ProvideConnectionProfileRepositoryFactory(
      Provider<ConnectionProfileRepositoryImpl> implProvider) {
    this.implProvider = implProvider;
  }

  @Override
  public ConnectionProfileRepository get() {
    return provideConnectionProfileRepository(implProvider.get());
  }

  public static DatabaseModule_ProvideConnectionProfileRepositoryFactory create(
      Provider<ConnectionProfileRepositoryImpl> implProvider) {
    return new DatabaseModule_ProvideConnectionProfileRepositoryFactory(implProvider);
  }

  public static ConnectionProfileRepository provideConnectionProfileRepository(
      ConnectionProfileRepositoryImpl impl) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideConnectionProfileRepository(impl));
  }
}
