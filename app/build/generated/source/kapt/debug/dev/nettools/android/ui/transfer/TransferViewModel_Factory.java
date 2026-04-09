package dev.nettools.android.ui.transfer;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.data.security.CredentialStore;
import dev.nettools.android.data.security.KnownHostsManager;
import dev.nettools.android.domain.repository.ConnectionProfileRepository;
import dev.nettools.android.domain.repository.TransferHistoryRepository;
import dev.nettools.android.service.TransferProgressHolder;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class TransferViewModel_Factory implements Factory<TransferViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<ConnectionProfileRepository> profileRepositoryProvider;

  private final Provider<TransferHistoryRepository> historyRepositoryProvider;

  private final Provider<CredentialStore> credentialStoreProvider;

  private final Provider<KnownHostsManager> knownHostsManagerProvider;

  private final Provider<TransferProgressHolder> progressHolderProvider;

  public TransferViewModel_Factory(Provider<Context> contextProvider,
      Provider<ConnectionProfileRepository> profileRepositoryProvider,
      Provider<TransferHistoryRepository> historyRepositoryProvider,
      Provider<CredentialStore> credentialStoreProvider,
      Provider<KnownHostsManager> knownHostsManagerProvider,
      Provider<TransferProgressHolder> progressHolderProvider) {
    this.contextProvider = contextProvider;
    this.profileRepositoryProvider = profileRepositoryProvider;
    this.historyRepositoryProvider = historyRepositoryProvider;
    this.credentialStoreProvider = credentialStoreProvider;
    this.knownHostsManagerProvider = knownHostsManagerProvider;
    this.progressHolderProvider = progressHolderProvider;
  }

  @Override
  public TransferViewModel get() {
    return newInstance(contextProvider.get(), profileRepositoryProvider.get(), historyRepositoryProvider.get(), credentialStoreProvider.get(), knownHostsManagerProvider.get(), progressHolderProvider.get());
  }

  public static TransferViewModel_Factory create(Provider<Context> contextProvider,
      Provider<ConnectionProfileRepository> profileRepositoryProvider,
      Provider<TransferHistoryRepository> historyRepositoryProvider,
      Provider<CredentialStore> credentialStoreProvider,
      Provider<KnownHostsManager> knownHostsManagerProvider,
      Provider<TransferProgressHolder> progressHolderProvider) {
    return new TransferViewModel_Factory(contextProvider, profileRepositoryProvider, historyRepositoryProvider, credentialStoreProvider, knownHostsManagerProvider, progressHolderProvider);
  }

  public static TransferViewModel newInstance(Context context,
      ConnectionProfileRepository profileRepository, TransferHistoryRepository historyRepository,
      CredentialStore credentialStore, KnownHostsManager knownHostsManager,
      TransferProgressHolder progressHolder) {
    return new TransferViewModel(context, profileRepository, historyRepository, credentialStore, knownHostsManager, progressHolder);
  }
}
