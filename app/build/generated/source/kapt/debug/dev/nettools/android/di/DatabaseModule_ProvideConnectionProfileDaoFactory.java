package dev.nettools.android.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.db.AppDatabase;
import dev.nettools.android.data.db.ConnectionProfileDao;
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
public final class DatabaseModule_ProvideConnectionProfileDaoFactory implements Factory<ConnectionProfileDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideConnectionProfileDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ConnectionProfileDao get() {
    return provideConnectionProfileDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideConnectionProfileDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideConnectionProfileDaoFactory(dbProvider);
  }

  public static ConnectionProfileDao provideConnectionProfileDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideConnectionProfileDao(db));
  }
}
