package dev.nettools.android.ui.progress;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.nettools.android.service.TransferProgressHolder;
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
public final class ProgressViewModel_Factory implements Factory<ProgressViewModel> {
  private final Provider<TransferProgressHolder> holderProvider;

  public ProgressViewModel_Factory(Provider<TransferProgressHolder> holderProvider) {
    this.holderProvider = holderProvider;
  }

  @Override
  public ProgressViewModel get() {
    return newInstance(holderProvider.get());
  }

  public static ProgressViewModel_Factory create(Provider<TransferProgressHolder> holderProvider) {
    return new ProgressViewModel_Factory(holderProvider);
  }

  public static ProgressViewModel newInstance(TransferProgressHolder holder) {
    return new ProgressViewModel(holder);
  }
}
