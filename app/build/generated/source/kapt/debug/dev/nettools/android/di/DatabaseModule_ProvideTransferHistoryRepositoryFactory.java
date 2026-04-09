package dev.nettools.android.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.repository.TransferHistoryRepositoryImpl;
import dev.nettools.android.domain.repository.TransferHistoryRepository;
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
public final class DatabaseModule_ProvideTransferHistoryRepositoryFactory implements Factory<TransferHistoryRepository> {
  private final Provider<TransferHistoryRepositoryImpl> implProvider;

  public DatabaseModule_ProvideTransferHistoryRepositoryFactory(
      Provider<TransferHistoryRepositoryImpl> implProvider) {
    this.implProvider = implProvider;
  }

  @Override
  public TransferHistoryRepository get() {
    return provideTransferHistoryRepository(implProvider.get());
  }

  public static DatabaseModule_ProvideTransferHistoryRepositoryFactory create(
      Provider<TransferHistoryRepositoryImpl> implProvider) {
    return new DatabaseModule_ProvideTransferHistoryRepositoryFactory(implProvider);
  }

  public static TransferHistoryRepository provideTransferHistoryRepository(
      TransferHistoryRepositoryImpl impl) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTransferHistoryRepository(impl));
  }
}
