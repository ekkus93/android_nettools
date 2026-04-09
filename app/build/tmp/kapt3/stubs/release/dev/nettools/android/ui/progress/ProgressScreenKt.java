package dev.nettools.android.ui.progress;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.CardDefaults;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.navigation.NavController;
import dev.nettools.android.data.ssh.TransferProgress;
import dev.nettools.android.domain.model.TransferJob;
import dev.nettools.android.domain.model.TransferStatus;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a\"\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u0007\u001a\"\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000eH\u0003\u001a\u0010\u0010\u000f\u001a\u00020\u00012\u0006\u0010\u0010\u001a\u00020\u0011H\u0003\u00a8\u0006\u0012"}, d2 = {"ProgressScreen", "", "jobId", "", "navController", "Landroidx/navigation/NavController;", "viewModel", "Ldev/nettools/android/ui/progress/ProgressViewModel;", "TransferCard", "job", "Ldev/nettools/android/domain/model/TransferJob;", "progress", "Ldev/nettools/android/data/ssh/TransferProgress;", "isPrimary", "", "StatusText", "status", "Ldev/nettools/android/domain/model/TransferStatus;", "app_release"})
public final class ProgressScreenKt {
    
    /**
     * Transfer Progress screen — shows live progress for all active transfers.
     *
     * @param jobId The job ID to focus on (used to highlight the primary transfer).
     * @param navController Navigation controller for back navigation.
     * @param viewModel The [ProgressViewModel] providing live state.
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ProgressScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String jobId, @org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.ui.progress.ProgressViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void TransferCard(dev.nettools.android.domain.model.TransferJob job, dev.nettools.android.data.ssh.TransferProgress progress, boolean isPrimary) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void StatusText(dev.nettools.android.domain.model.TransferStatus status) {
    }
}