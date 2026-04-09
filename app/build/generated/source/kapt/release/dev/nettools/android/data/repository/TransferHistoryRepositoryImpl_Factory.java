package dev.nettools.android.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.db.TransferHistoryDao;
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
public final class TransferHistoryRepositoryImpl_Factory implements Factory<TransferHistoryRepositoryImpl> {
  private final Provider<TransferHistoryDao> daoProvider;

  public TransferHistoryRepositoryImpl_Factory(Provider<TransferHistoryDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public TransferHistoryRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static TransferHistoryRepositoryImpl_Factory create(
      Provider<TransferHistoryDao> daoProvider) {
    return new TransferHistoryRepositoryImpl_Factory(daoProvider);
  }

  public static TransferHistoryRepositoryImpl newInstance(TransferHistoryDao dao) {
    return new TransferHistoryRepositoryImpl(dao);
  }
}
