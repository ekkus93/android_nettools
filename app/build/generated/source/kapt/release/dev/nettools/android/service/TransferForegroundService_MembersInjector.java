package dev.nettools.android.service;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class TransferForegroundService_MembersInjector implements MembersInjector<TransferForegroundService> {
  private final Provider<NotificationHelper> notificationHelperProvider;

  public TransferForegroundService_MembersInjector(
      Provider<NotificationHelper> notificationHelperProvider) {
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public static MembersInjector<TransferForegroundService> create(
      Provider<NotificationHelper> notificationHelperProvider) {
    return new TransferForegroundService_MembersInjector(notificationHelperProvider);
  }

  @Override
  public void injectMembers(TransferForegroundService instance) {
    injectNotificationHelper(instance, notificationHelperProvider.get());
  }

  @InjectedFieldSignature("dev.nettools.android.service.TransferForegroundService.notificationHelper")
  public static void injectNotificationHelper(TransferForegroundService instance,
      NotificationHelper notificationHelper) {
    instance.notificationHelper = notificationHelper;
  }
}
