package dev.nettools.android;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dev.nettools.android.data.db.AppDatabase;
import dev.nettools.android.data.db.ConnectionProfileDao;
import dev.nettools.android.data.db.KnownHostDao;
import dev.nettools.android.data.db.TransferHistoryDao;
import dev.nettools.android.data.repository.ConnectionProfileRepositoryImpl;
import dev.nettools.android.data.repository.KnownHostRepositoryImpl;
import dev.nettools.android.data.repository.TransferHistoryRepositoryImpl;
import dev.nettools.android.data.security.CredentialStore;
import dev.nettools.android.data.security.KnownHostsManager;
import dev.nettools.android.data.ssh.SftpClient;
import dev.nettools.android.data.ssh.SshConnectionManager;
import dev.nettools.android.di.DatabaseModule_ProvideAppDatabaseFactory;
import dev.nettools.android.di.DatabaseModule_ProvideConnectionProfileDaoFactory;
import dev.nettools.android.di.DatabaseModule_ProvideConnectionProfileRepositoryFactory;
import dev.nettools.android.di.DatabaseModule_ProvideKnownHostDaoFactory;
import dev.nettools.android.di.DatabaseModule_ProvideKnownHostRepositoryFactory;
import dev.nettools.android.di.DatabaseModule_ProvideTransferHistoryDaoFactory;
import dev.nettools.android.di.DatabaseModule_ProvideTransferHistoryRepositoryFactory;
import dev.nettools.android.di.SshModule_ProvideKnownHostsManagerFactory;
import dev.nettools.android.di.SshModule_ProvideSftpClientFactory;
import dev.nettools.android.di.SshModule_ProvideSshConnectionManagerFactory;
import dev.nettools.android.domain.repository.ConnectionProfileRepository;
import dev.nettools.android.domain.repository.KnownHostRepository;
import dev.nettools.android.domain.repository.TransferHistoryRepository;
import dev.nettools.android.service.NotificationHelper;
import dev.nettools.android.service.TransferForegroundService;
import dev.nettools.android.service.TransferForegroundService_MembersInjector;
import dev.nettools.android.service.TransferProgressHolder;
import dev.nettools.android.ui.connections.SavedConnectionsViewModel;
import dev.nettools.android.ui.connections.SavedConnectionsViewModel_HiltModules;
import dev.nettools.android.ui.connections.SavedConnectionsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import dev.nettools.android.ui.connections.SavedConnectionsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dev.nettools.android.ui.history.HistoryViewModel;
import dev.nettools.android.ui.history.HistoryViewModel_HiltModules;
import dev.nettools.android.ui.history.HistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import dev.nettools.android.ui.history.HistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dev.nettools.android.ui.progress.ProgressViewModel;
import dev.nettools.android.ui.progress.ProgressViewModel_HiltModules;
import dev.nettools.android.ui.progress.ProgressViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import dev.nettools.android.ui.progress.ProgressViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dev.nettools.android.ui.sftp.SftpBrowserViewModel;
import dev.nettools.android.ui.sftp.SftpBrowserViewModel_HiltModules;
import dev.nettools.android.ui.sftp.SftpBrowserViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import dev.nettools.android.ui.sftp.SftpBrowserViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dev.nettools.android.ui.transfer.TransferViewModel;
import dev.nettools.android.ui.transfer.TransferViewModel_HiltModules;
import dev.nettools.android.ui.transfer.TransferViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import dev.nettools.android.ui.transfer.TransferViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerNetToolsApp_HiltComponents_SingletonC {
  private DaggerNetToolsApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public NetToolsApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements NetToolsApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public NetToolsApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements NetToolsApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public NetToolsApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements NetToolsApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public NetToolsApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements NetToolsApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public NetToolsApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements NetToolsApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public NetToolsApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements NetToolsApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public NetToolsApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements NetToolsApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public NetToolsApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends NetToolsApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends NetToolsApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends NetToolsApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends NetToolsApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(5).put(HistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HistoryViewModel_HiltModules.KeyModule.provide()).put(ProgressViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ProgressViewModel_HiltModules.KeyModule.provide()).put(SavedConnectionsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SavedConnectionsViewModel_HiltModules.KeyModule.provide()).put(SftpBrowserViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SftpBrowserViewModel_HiltModules.KeyModule.provide()).put(TransferViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TransferViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }
  }

  private static final class ViewModelCImpl extends NetToolsApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<HistoryViewModel> historyViewModelProvider;

    Provider<ProgressViewModel> progressViewModelProvider;

    Provider<SavedConnectionsViewModel> savedConnectionsViewModelProvider;

    Provider<SftpBrowserViewModel> sftpBrowserViewModelProvider;

    Provider<TransferViewModel> transferViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.historyViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.progressViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.savedConnectionsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.sftpBrowserViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.transferViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(5).put(HistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (historyViewModelProvider))).put(ProgressViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (progressViewModelProvider))).put(SavedConnectionsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (savedConnectionsViewModelProvider))).put(SftpBrowserViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (sftpBrowserViewModelProvider))).put(TransferViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (transferViewModelProvider))).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dev.nettools.android.ui.history.HistoryViewModel
          return (T) new HistoryViewModel(singletonCImpl.provideTransferHistoryRepositoryProvider.get());

          case 1: // dev.nettools.android.ui.progress.ProgressViewModel
          return (T) new ProgressViewModel(singletonCImpl.transferProgressHolderProvider.get());

          case 2: // dev.nettools.android.ui.connections.SavedConnectionsViewModel
          return (T) new SavedConnectionsViewModel(singletonCImpl.provideConnectionProfileRepositoryProvider.get(), singletonCImpl.credentialStoreProvider.get());

          case 3: // dev.nettools.android.ui.sftp.SftpBrowserViewModel
          return (T) new SftpBrowserViewModel(singletonCImpl.provideSshConnectionManagerProvider.get(), singletonCImpl.provideSftpClientProvider.get(), singletonCImpl.provideKnownHostsManagerProvider.get());

          case 4: // dev.nettools.android.ui.transfer.TransferViewModel
          return (T) new TransferViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideConnectionProfileRepositoryProvider.get(), singletonCImpl.provideTransferHistoryRepositoryProvider.get(), singletonCImpl.credentialStoreProvider.get(), singletonCImpl.provideKnownHostsManagerProvider.get(), singletonCImpl.transferProgressHolderProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends NetToolsApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends NetToolsApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    NotificationHelper notificationHelper() {
      return new NotificationHelper(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));
    }

    @Override
    public void injectTransferForegroundService(
        TransferForegroundService transferForegroundService) {
      injectTransferForegroundService2(transferForegroundService);
    }

    private TransferForegroundService injectTransferForegroundService2(
        TransferForegroundService instance) {
      TransferForegroundService_MembersInjector.injectNotificationHelper(instance, notificationHelper());
      return instance;
    }
  }

  private static final class SingletonCImpl extends NetToolsApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<AppDatabase> provideAppDatabaseProvider;

    Provider<TransferHistoryRepository> provideTransferHistoryRepositoryProvider;

    Provider<TransferProgressHolder> transferProgressHolderProvider;

    Provider<ConnectionProfileRepository> provideConnectionProfileRepositoryProvider;

    Provider<CredentialStore> credentialStoreProvider;

    Provider<SshConnectionManager> provideSshConnectionManagerProvider;

    Provider<SftpClient> provideSftpClientProvider;

    Provider<KnownHostRepository> provideKnownHostRepositoryProvider;

    Provider<KnownHostsManager> provideKnownHostsManagerProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    TransferHistoryDao transferHistoryDao() {
      return DatabaseModule_ProvideTransferHistoryDaoFactory.provideTransferHistoryDao(provideAppDatabaseProvider.get());
    }

    TransferHistoryRepositoryImpl transferHistoryRepositoryImpl() {
      return new TransferHistoryRepositoryImpl(transferHistoryDao());
    }

    ConnectionProfileDao connectionProfileDao() {
      return DatabaseModule_ProvideConnectionProfileDaoFactory.provideConnectionProfileDao(provideAppDatabaseProvider.get());
    }

    ConnectionProfileRepositoryImpl connectionProfileRepositoryImpl() {
      return new ConnectionProfileRepositoryImpl(connectionProfileDao());
    }

    KnownHostDao knownHostDao() {
      return DatabaseModule_ProvideKnownHostDaoFactory.provideKnownHostDao(provideAppDatabaseProvider.get());
    }

    KnownHostRepositoryImpl knownHostRepositoryImpl() {
      return new KnownHostRepositoryImpl(knownHostDao());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 1));
      this.provideTransferHistoryRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<TransferHistoryRepository>(singletonCImpl, 0));
      this.transferProgressHolderProvider = DoubleCheck.provider(new SwitchingProvider<TransferProgressHolder>(singletonCImpl, 2));
      this.provideConnectionProfileRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ConnectionProfileRepository>(singletonCImpl, 3));
      this.credentialStoreProvider = DoubleCheck.provider(new SwitchingProvider<CredentialStore>(singletonCImpl, 4));
      this.provideSshConnectionManagerProvider = DoubleCheck.provider(new SwitchingProvider<SshConnectionManager>(singletonCImpl, 5));
      this.provideSftpClientProvider = DoubleCheck.provider(new SwitchingProvider<SftpClient>(singletonCImpl, 6));
      this.provideKnownHostRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<KnownHostRepository>(singletonCImpl, 8));
      this.provideKnownHostsManagerProvider = DoubleCheck.provider(new SwitchingProvider<KnownHostsManager>(singletonCImpl, 7));
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @Override
    public void injectNetToolsApp(NetToolsApp netToolsApp) {
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dev.nettools.android.domain.repository.TransferHistoryRepository
          return (T) DatabaseModule_ProvideTransferHistoryRepositoryFactory.provideTransferHistoryRepository(singletonCImpl.transferHistoryRepositoryImpl());

          case 1: // dev.nettools.android.data.db.AppDatabase
          return (T) DatabaseModule_ProvideAppDatabaseFactory.provideAppDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // dev.nettools.android.service.TransferProgressHolder
          return (T) new TransferProgressHolder();

          case 3: // dev.nettools.android.domain.repository.ConnectionProfileRepository
          return (T) DatabaseModule_ProvideConnectionProfileRepositoryFactory.provideConnectionProfileRepository(singletonCImpl.connectionProfileRepositoryImpl());

          case 4: // dev.nettools.android.data.security.CredentialStore
          return (T) new CredentialStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // dev.nettools.android.data.ssh.SshConnectionManager
          return (T) SshModule_ProvideSshConnectionManagerFactory.provideSshConnectionManager();

          case 6: // dev.nettools.android.data.ssh.SftpClient
          return (T) SshModule_ProvideSftpClientFactory.provideSftpClient();

          case 7: // dev.nettools.android.data.security.KnownHostsManager
          return (T) SshModule_ProvideKnownHostsManagerFactory.provideKnownHostsManager(singletonCImpl.provideKnownHostRepositoryProvider.get());

          case 8: // dev.nettools.android.domain.repository.KnownHostRepository
          return (T) DatabaseModule_ProvideKnownHostRepositoryFactory.provideKnownHostRepository(singletonCImpl.knownHostRepositoryImpl());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
