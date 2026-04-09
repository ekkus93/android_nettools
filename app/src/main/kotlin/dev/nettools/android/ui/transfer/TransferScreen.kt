package dev.nettools.android.ui.transfer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.ui.navigation.Routes

/**
 * SCP Transfer screen.
 * Allows the user to configure an SSH connection and start a file transfer.
 *
 * @param navController Navigation controller for screen routing.
 * @param viewModel The [TransferViewModel] managing form and transfer state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    navController: NavController,
    viewModel: TransferViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate to progress screen when transfer is dispatched
    LaunchedEffect(Unit) {
        viewModel.navigateToProgress.collect { jobId ->
            navController.navigate(Routes.progress(jobId))
        }
    }

    // Show error snackbar
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }

    // Observe remote path result from SFTP browser
    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.savedStateHandle
            ?.getStateFlow<String?>("remote_path", null)
            ?.collect { path ->
                path?.let {
                    viewModel.onRemotePathChange(it)
                    navBackStackEntry.savedStateHandle.remove<String>("remote_path")
                }
            }
    }

    // File picker for local upload
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onLocalPathChange(it.toString()) }
    }

    // Directory picker for local download destination
    val dirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onLocalPathChange(it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SCP Transfer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Saved profile picker ──────────────────────────────────────────
            if (state.savedProfiles.isNotEmpty()) {
                var profileDropdown by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = profileDropdown,
                    onExpandedChange = { profileDropdown = it },
                ) {
                    OutlinedTextField(
                        value = state.savedProfiles
                            .find { it.id == state.selectedProfileId }?.name ?: "Select saved profile…",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Saved Profile") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(profileDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = profileDropdown,
                        onDismissRequest = { profileDropdown = false },
                    ) {
                        state.savedProfiles.forEach { profile ->
                            DropdownMenuItem(
                                text = { Text(profile.name) },
                                onClick = {
                                    viewModel.onProfileSelected(profile.id)
                                    profileDropdown = false
                                },
                            )
                        }
                    }
                }
            }

            // ── Connection fields ─────────────────────────────────────────────
            OutlinedTextField(
                value = state.host,
                onValueChange = viewModel::onHostChange,
                label = { Text("Host") },
                placeholder = { Text("192.168.1.100") },
                isError = state.hostError != null,
                supportingText = state.hostError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.port,
                    onValueChange = viewModel::onPortChange,
                    label = { Text("Port") },
                    isError = state.portError != null,
                    supportingText = state.portError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = { Text("Username") },
                    isError = state.usernameError != null,
                    supportingText = state.usernameError?.let { { Text(it) } },
                    modifier = Modifier.weight(2f),
                )
            }

            // ── Auth method ───────────────────────────────────────────────────
            var authDropdown by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = authDropdown,
                onExpandedChange = { authDropdown = it },
            ) {
                OutlinedTextField(
                    value = if (state.authType == AuthType.PASSWORD) "Password" else "Private Key",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Auth Method") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(authDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = authDropdown,
                    onDismissRequest = { authDropdown = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Password") },
                        onClick = { viewModel.onAuthTypeChange(AuthType.PASSWORD); authDropdown = false },
                    )
                    DropdownMenuItem(
                        text = { Text("Private Key") },
                        onClick = { viewModel.onAuthTypeChange(AuthType.PRIVATE_KEY); authDropdown = false },
                    )
                }
            }

            when (state.authType) {
                AuthType.PASSWORD -> {
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                AuthType.PRIVATE_KEY -> {
                    OutlinedTextField(
                        value = state.keyPath,
                        onValueChange = viewModel::onKeyPathChange,
                        label = { Text("Key file path") },
                        placeholder = { Text("/storage/emulated/0/id_rsa") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ── Transfer direction ────────────────────────────────────────────
            val directions = listOf(TransferDirection.UPLOAD, TransferDirection.DOWNLOAD)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                directions.forEachIndexed { index, direction ->
                    SegmentedButton(
                        selected = state.direction == direction,
                        onClick = { viewModel.onDirectionChange(direction) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = directions.size),
                        label = { Text(direction.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            // ── Local path ────────────────────────────────────────────────────
            OutlinedTextField(
                value = state.localPath,
                onValueChange = viewModel::onLocalPathChange,
                label = { Text(if (state.direction == TransferDirection.UPLOAD) "Local file" else "Download to") },
                isError = state.localPathError != null,
                supportingText = state.localPathError?.let { { Text(it) } },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (state.direction == TransferDirection.UPLOAD) {
                                filePicker.launch(arrayOf("*/*"))
                            } else {
                                dirPicker.launch(null)
                            }
                        },
                    ) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = "Pick local path")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Remote path ───────────────────────────────────────────────────
            OutlinedTextField(
                value = state.remotePath,
                onValueChange = viewModel::onRemotePathChange,
                label = { Text("Remote path") },
                placeholder = { Text("/home/user/files") },
                isError = state.remotePathError != null,
                supportingText = state.remotePathError?.let { { Text(it) } },
                trailingIcon = {
                    TextButton(
                        onClick = { viewModel.prepareSftpBrowse(); navController.navigate(Routes.SFTP_BROWSER) },
                        enabled = state.host.isNotBlank() && state.username.isNotBlank(),
                    ) {
                        Text("Browse…")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Save profile ──────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.saveProfile, onCheckedChange = viewModel::onSaveProfileChange)
                Text("Save as connection profile")
            }
            if (state.saveProfile) {
                OutlinedTextField(
                    value = state.profileName,
                    onValueChange = viewModel::onProfileNameChange,
                    label = { Text("Profile name") },
                    placeholder = { Text(state.host.ifBlank { "My Server" }) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── Transfer button ───────────────────────────────────────────────
            Button(
                onClick = { viewModel.startTransfer() },
                enabled = !state.isConnecting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (state.isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(if (state.isConnecting) "Connecting…" else "Transfer")
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Host key TOFU dialog ──────────────────────────────────────────────────
    state.pendingHostKey?.let { pending ->
        AlertDialog(
            onDismissRequest = { viewModel.onHostKeyRejected() },
            title = {
                Text(if (pending.isChanged) "⚠️ Host Key Changed!" else "Unknown Host Key")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (pending.isChanged) {
                        Text(
                            "The host key for ${pending.host} has changed since your last connection. " +
                                "This could indicate a man-in-the-middle attack.",
                        )
                        Text("Old: ${pending.oldFingerprint}")
                    } else {
                        Text("You are connecting to ${pending.host} for the first time.")
                    }
                    Text("Fingerprint (SHA-256):")
                    Text(
                        text = pending.fingerprint,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onHostKeyAccepted() }) {
                    Text(if (pending.isChanged) "Trust anyway" else "Trust")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onHostKeyRejected() }) { Text("Reject") }
            },
        )
    }
}
