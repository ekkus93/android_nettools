package dev.nettools.android.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.db.AppDatabase;
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
public final class DatabaseModule_ProvideTransferHistoryDaoFactory implements Factory<TransferHistoryDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideTransferHistoryDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TransferHistoryDao get() {
    return provideTransferHistoryDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideTransferHistoryDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideTransferHistoryDaoFactory(dbProvider);
  }

  public static TransferHistoryDao provideTransferHistoryDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTransferHistoryDao(db));
  }
}
