package dev.nettools.android.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.db.AppDatabase;
import dev.nettools.android.data.db.KnownHostDao;
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
public final class DatabaseModule_ProvideKnownHostDaoFactory implements Factory<KnownHostDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideKnownHostDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public KnownHostDao get() {
    return provideKnownHostDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideKnownHostDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideKnownHostDaoFactory(dbProvider);
  }

  public static KnownHostDao provideKnownHostDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideKnownHostDao(db));
  }
}
