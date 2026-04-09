package dev.nettools.android.ui.history;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.navigation.NavController;
import dev.nettools.android.domain.model.HistoryStatus;
import dev.nettools.android.domain.model.TransferDirection;
import dev.nettools.android.domain.model.TransferHistoryEntry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000,\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a\u001a\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0010\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\bH\u0003\u001a\u0010\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000bH\u0003\u001a\u0012\u0010\f\u001a\u00020\u00012\b\b\u0002\u0010\r\u001a\u00020\u000eH\u0003\u00a8\u0006\u000f"}, d2 = {"HistoryScreen", "", "navController", "Landroidx/navigation/NavController;", "viewModel", "Ldev/nettools/android/ui/history/HistoryViewModel;", "HistoryEntryRow", "entry", "Ldev/nettools/android/domain/model/TransferHistoryEntry;", "StatusBadge", "status", "Ldev/nettools/android/domain/model/HistoryStatus;", "EmptyHistoryPlaceholder", "modifier", "Landroidx/compose/ui/Modifier;", "app_release"})
public final class HistoryScreenKt {
    
    /**
     * Transfer History screen — lists all past transfers with status badges.
     *
     * @param navController Navigation controller for back navigation.
     * @param viewModel The [HistoryViewModel] supplying history data.
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void HistoryScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.ui.history.HistoryViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void HistoryEntryRow(dev.nettools.android.domain.model.TransferHistoryEntry entry) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void StatusBadge(dev.nettools.android.domain.model.HistoryStatus status) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void EmptyHistoryPlaceholder(androidx.compose.ui.Modifier modifier) {
    }
}