package dev.nettools.android.ui.connections;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.CardDefaults;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.ExposedDropdownMenuDefaults;
import androidx.compose.material3.MenuAnchorType;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.input.PasswordVisualTransformation;
import androidx.navigation.NavController;
import dev.nettools.android.domain.model.AuthType;
import dev.nettools.android.domain.model.ConnectionProfile;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000F\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\u001a\u001a\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a,\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a\u00cc\u0001\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u000e2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u00102\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u00102\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u00102\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u00102\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u00010\u00102\u0012\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u00102\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00010\u00102\u0012\u0010\u001a\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u00102\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u00a8\u0006\u001d"}, d2 = {"SavedConnectionsScreen", "", "navController", "Landroidx/navigation/NavController;", "viewModel", "Ldev/nettools/android/ui/connections/SavedConnectionsViewModel;", "ProfileRow", "profile", "Ldev/nettools/android/domain/model/ConnectionProfile;", "onEdit", "Lkotlin/Function0;", "onDelete", "ProfileEditDialog", "state", "Ldev/nettools/android/ui/connections/ProfileEditState;", "onNameChange", "Lkotlin/Function1;", "", "onHostChange", "onPortChange", "onUsernameChange", "onAuthTypeChange", "Ldev/nettools/android/domain/model/AuthType;", "onKeyPathChange", "onSavePasswordChange", "", "onPasswordChange", "onSave", "onDismiss", "app_release"})
public final class SavedConnectionsScreenKt {
    
    /**
     * Saved Connections screen — CRUD for [ConnectionProfile] items.
     *
     * @param navController Navigation controller for back navigation.
     * @param viewModel The [SavedConnectionsViewModel] managing profile state.
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void SavedConnectionsScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.ui.connections.SavedConnectionsViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ProfileRow(dev.nettools.android.domain.model.ConnectionProfile profile, kotlin.jvm.functions.Function0<kotlin.Unit> onEdit, kotlin.jvm.functions.Function0<kotlin.Unit> onDelete) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void ProfileEditDialog(dev.nettools.android.ui.connections.ProfileEditState state, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNameChange, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onHostChange, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onPortChange, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onUsernameChange, kotlin.jvm.functions.Function1<? super dev.nettools.android.domain.model.AuthType, kotlin.Unit> onAuthTypeChange, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onKeyPathChange, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onSavePasswordChange, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onPasswordChange, kotlin.jvm.functions.Function0<kotlin.Unit> onSave, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}