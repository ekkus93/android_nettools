package dev.nettools.android.ui.transfer;

import android.net.Uri;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.ExposedDropdownMenuDefaults;
import androidx.compose.material3.MenuAnchorType;
import androidx.compose.material3.SegmentedButtonDefaults;
import androidx.compose.material3.SnackbarHostState;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.input.KeyboardType;
import androidx.compose.ui.text.input.PasswordVisualTransformation;
import androidx.navigation.NavController;
import dev.nettools.android.domain.model.AuthType;
import dev.nettools.android.domain.model.TransferDirection;
import dev.nettools.android.ui.navigation.Routes;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000\u0014\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\u001a\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u00a8\u0006\u0006"}, d2 = {"TransferScreen", "", "navController", "Landroidx/navigation/NavController;", "viewModel", "Ldev/nettools/android/ui/transfer/TransferViewModel;", "app_release"})
public final class TransferScreenKt {
    
    /**
     * SCP Transfer screen.
     * Allows the user to configure an SSH connection and start a file transfer.
     *
     * @param navController Navigation controller for screen routing.
     * @param viewModel The [TransferViewModel] managing form and transfer state.
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void TransferScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    dev.nettools.android.ui.transfer.TransferViewModel viewModel) {
    }
}