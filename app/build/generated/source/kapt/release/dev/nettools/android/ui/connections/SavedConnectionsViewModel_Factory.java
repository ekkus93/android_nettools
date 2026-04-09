package dev.nettools.android.ui.connections;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.security.CredentialStore;
import dev.nettools.android.domain.repository.ConnectionProfileRepository;
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
public final class SavedConnectionsViewModel_Factory implements Factory<SavedConnectionsViewModel> {
  private final Provider<ConnectionProfileRepository> profileRepositoryProvider;

  private final Provider<CredentialStore> credentialStoreProvider;

  public SavedConnectionsViewModel_Factory(
      Provider<ConnectionProfileRepository> profileRepositoryProvider,
      Provider<CredentialStore> credentialStoreProvider) {
    this.profileRepositoryProvider = profileRepositoryProvider;
    this.credentialStoreProvider = credentialStoreProvider;
  }

  @Override
  public SavedConnectionsViewModel get() {
    return newInstance(profileRepositoryProvider.get(), credentialStoreProvider.get());
  }

  public static SavedConnectionsViewModel_Factory create(
      Provider<ConnectionProfileRepository> profileRepositoryProvider,
      Provider<CredentialStore> credentialStoreProvider) {
    return new SavedConnectionsViewModel_Factory(profileRepositoryProvider, credentialStoreProvider);
  }

  public static SavedConnectionsViewModel newInstance(ConnectionProfileRepository profileRepository,
      CredentialStore credentialStore) {
    return new SavedConnectionsViewModel(profileRepository, credentialStore);
  }
}
