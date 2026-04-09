package dev.nettools.android.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class ConnectionProfileRepositoryImpl_Factory implements Factory<ConnectionProfileRepositoryImpl> {
  private final Provider<ConnectionProfileDao> daoProvider;

  public ConnectionProfileRepositoryImpl_Factory(Provider<ConnectionProfileDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public ConnectionProfileRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static ConnectionProfileRepositoryImpl_Factory create(
      Provider<ConnectionProfileDao> daoProvider) {
    return new ConnectionProfileRepositoryImpl_Factory(daoProvider);
  }

  public static ConnectionProfileRepositoryImpl newInstance(ConnectionProfileDao dao) {
    return new ConnectionProfileRepositoryImpl(dao);
  }
}
