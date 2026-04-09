package dev.nettools.android.ui.history;

import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dev.nettools.android.domain.model.TransferHistoryEntry;
import dev.nettools.android.domain.repository.TransferHistoryRepository;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

/**
 * ViewModel for the Transfer History screen.
 * Exposes history entries as a [StateFlow] and provides a clear-all action.
 *
 * @property repository The source of persisted transfer history.
 */
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0006\u0010\f\u001a\u00020\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u000e"}, d2 = {"Ldev/nettools/android/ui/history/HistoryViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Ldev/nettools/android/domain/repository/TransferHistoryRepository;", "<init>", "(Ldev/nettools/android/domain/repository/TransferHistoryRepository;)V", "history", "Lkotlinx/coroutines/flow/StateFlow;", "", "Ldev/nettools/android/domain/model/TransferHistoryEntry;", "getHistory", "()Lkotlinx/coroutines/flow/StateFlow;", "clearAll", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HistoryViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final dev.nettools.android.domain.repository.TransferHistoryRepository repository = null;
    
    /**
     * All recorded transfers, newest first.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<dev.nettools.android.domain.model.TransferHistoryEntry>> history = null;
    
    @javax.inject.Inject()
    public HistoryViewModel(@org.jetbrains.annotations.NotNull()
    dev.nettools.android.domain.repository.TransferHistoryRepository repository) {
        super();
    }
    
    /**
     * All recorded transfers, newest first.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<dev.nettools.android.domain.model.TransferHistoryEntry>> getHistory() {
        return null;
    }
    
    /**
     * Deletes all history entries from the database.
     */
    public final void clearAll() {
    }
}