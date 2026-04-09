package dev.nettools.android.ui.progress;

import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dev.nettools.android.data.ssh.TransferProgress;
import dev.nettools.android.domain.model.TransferJob;
import dev.nettools.android.service.TransferProgressHolder;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

/**
 * ViewModel for the Transfer Progress screen.
 * Observes live progress from [TransferProgressHolder] without requiring
 * direct service binding.
 *
 * @property holder Singleton that holds in-flight transfer state.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\u0006\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001d\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\f\u00a8\u0006\u0011"}, d2 = {"Ldev/nettools/android/ui/progress/ProgressViewModel;", "Landroidx/lifecycle/ViewModel;", "holder", "Ldev/nettools/android/service/TransferProgressHolder;", "<init>", "(Ldev/nettools/android/service/TransferProgressHolder;)V", "progress", "Lkotlinx/coroutines/flow/StateFlow;", "", "", "Ldev/nettools/android/data/ssh/TransferProgress;", "getProgress", "()Lkotlinx/coroutines/flow/StateFlow;", "activeJobs", "", "Ldev/nettools/android/domain/model/TransferJob;", "getActiveJobs", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ProgressViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.service.TransferProgressHolder holder = null;
    
    /**
     * Per-job progress keyed by job ID.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.Map<java.lang.String, dev.nettools.android.data.ssh.TransferProgress>> progress = null;
    
    /**
     * List of currently active or queued jobs.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<dev.nettools.android.domain.model.TransferJob>> activeJobs = null;
    
    @javax.inject.Inject()
    public ProgressViewModel(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.service.TransferProgressHolder holder) {
        super();
    }
    
    /**
     * Per-job progress keyed by job ID.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.Map<java.lang.String, dev.nettools.android.data.ssh.TransferProgress>> getProgress() {
        return null;
    }
    
    /**
     * List of currently active or queued jobs.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<dev.nettools.android.domain.model.TransferJob>> getActiveJobs() {
        return null;
    }
}