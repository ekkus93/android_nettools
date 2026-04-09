package dev.nettools.android.ui.sftp;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.SnackbarHostState;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.navigation.NavController;
import dev.nettools.android.domain.model.RemoteFileEntry;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000:\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a$\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u0007\u001a*\u0010\b\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\rH\u0003\u001aP\u0010\u000e\u001a\u00020\u00012\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u0011H\u0003\u00a8\u0006\u0015"}, d2 = {"SftpBrowserScreen", "", "navController", "Landroidx/navigation/NavController;", "pickerMode", "", "viewModel", "Ldev/nettools/android/ui/sftp/SftpBrowserViewModel;", "BreadcrumbRow", "breadcrumbs", "", "", "onNavigate", "Lkotlin/Function1;", "FileEntryRow", "entry", "Ldev/nettools/android/domain/model/RemoteFileEntry;", "Lkotlin/Function0;", "onSelect", "onRename", "onDelete", "app_debug"})
public final class SftpBrowserScreenKt {
    
    /**
     * SFTP Browser screen — navigates the remote directory tree.
     * When launched as a path picker (i.e. from the Transfer screen),
     * selecting a file or directory returns its path to the previous back stack entry.
     *
     * @param navController Navigation controller for routing.
     * @param pickerMode When true, tapping an entry returns its path rather than navigating into it.
     * @param viewModel The [SftpBrowserViewModel] managing remote state.
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void SftpBrowserScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, boolean pickerMode, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.ui.sftp.SftpBrowserViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void BreadcrumbRow(java.util.List<java.lang.String> breadcrumbs, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigate) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void FileEntryRow(dev.nettools.android.domain.model.RemoteFileEntry entry, boolean pickerMode, kotlin.jvm.functions.Function0<kotlin.Unit> onNavigate, kotlin.jvm.functions.Function0<kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onRename, kotlin.jvm.functions.Function0<kotlin.Unit> onDelete) {
    }
}