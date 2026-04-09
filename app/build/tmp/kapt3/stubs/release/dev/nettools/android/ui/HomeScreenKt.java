package dev.nettools.android.ui;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.CardDefaults;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.navigation.NavController;
import dev.nettools.android.ui.navigation.Routes;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000$\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0007\u001a9\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\u0011\u0010\b\u001a\r\u0012\u0004\u0012\u00020\u00010\t\u00a2\u0006\u0002\b\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\tH\u0003\u00a8\u0006\f"}, d2 = {"HomeScreen", "", "navController", "Landroidx/navigation/NavController;", "NavCard", "title", "", "subtitle", "icon", "Lkotlin/Function0;", "Landroidx/compose/runtime/Composable;", "onClick", "app_release"})
public final class HomeScreenKt {
    
    /**
     * Home screen composable — the main entry point of the application UI.
     * Provides navigation to SCP/SFTP transfer, saved connections, and history screens.
     *
     * @param navController Navigation controller for routing.
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void HomeScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void NavCard(java.lang.String title, java.lang.String subtitle, androidx.compose.runtime.internal.ComposableFunction0<kotlin.Unit> icon, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}