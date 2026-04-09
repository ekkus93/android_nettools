package dev.nettools.android.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class KnownHostRepositoryImpl_Factory implements Factory<KnownHostRepositoryImpl> {
  private final Provider<KnownHostDao> daoProvider;

  public KnownHostRepositoryImpl_Factory(Provider<KnownHostDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public KnownHostRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static KnownHostRepositoryImpl_Factory create(Provider<KnownHostDao> daoProvider) {
    return new KnownHostRepositoryImpl_Factory(daoProvider);
  }

  public static KnownHostRepositoryImpl newInstance(KnownHostDao dao) {
    return new KnownHostRepositoryImpl(dao);
  }
}
