package dev.nettools.android.ui.connections

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.ConnectionProfile

/**
 * Saved Connections screen — CRUD for [ConnectionProfile] items.
 *
 * @param navController Navigation controller for back navigation.
 * @param viewModel The [SavedConnectionsViewModel] managing profile state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedConnectionsScreen(
    navController: NavController,
    viewModel: SavedConnectionsViewModel = hiltViewModel(),
) {
    val profiles by viewModel.profiles.collectAsState()
    val editState by viewModel.editState.collectAsState()
    val deleteConfirmId by viewModel.deleteConfirmId.collectAsState()
    val context = LocalContext.current

    val keyFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
            viewModel.onKeyPathChange(it.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Connections") },
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
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openEditor(null) }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add connection")
            }
        },
    ) { padding ->
        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No saved connections", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tap + to add one",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ProfileRow(
                        profile = profile,
                        onEdit = { viewModel.openEditor(profile) },
                        onDelete = { viewModel.requestDelete(profile.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    // Edit / Create bottom sheet
    editState?.let { state ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var authDropdownExpanded by remember { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissEditor() },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (state.isNew) "New Connection" else "Edit Connection",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )

                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Name") },
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.host,
                    onValueChange = viewModel::onHostChange,
                    label = { Text("Host") },
                    isError = state.hostError != null,
                    supportingText = state.hostError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.port,
                    onValueChange = viewModel::onPortChange,
                    label = { Text("Port") },
                    isError = state.portError != null,
                    supportingText = state.portError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = { Text("Username") },
                    isError = state.usernameError != null,
                    supportingText = state.usernameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Auth type dropdown
                ExposedDropdownMenuBox(
                    expanded = authDropdownExpanded,
                    onExpandedChange = { authDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = if (state.authType == AuthType.PASSWORD) "Password" else "Private Key",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Auth Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(authDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = authDropdownExpanded,
                        onDismissRequest = { authDropdownExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Password") },
                            onClick = { viewModel.onAuthTypeChange(AuthType.PASSWORD); authDropdownExpanded = false },
                        )
                        DropdownMenuItem(
                            text = { Text("Private Key") },
                            onClick = { viewModel.onAuthTypeChange(AuthType.PRIVATE_KEY); authDropdownExpanded = false },
                        )
                    }
                }

                when (state.authType) {
                    AuthType.PASSWORD -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = state.savePassword, onCheckedChange = viewModel::onSavePasswordChange)
                            Text("Save password")
                        }
                        if (state.savePassword) {
                            OutlinedTextField(
                                value = state.password,
                                onValueChange = viewModel::onPasswordChange,
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    AuthType.PRIVATE_KEY -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            OutlinedTextField(
                                value = state.keyPath,
                                onValueChange = viewModel::onKeyPathChange,
                                label = { Text("Key file path") },
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { keyFileLauncher.launch(arrayOf("*/*")) }) {
                                Icon(Icons.Filled.FolderOpen, contentDescription = "Browse for key file")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { viewModel.dismissEditor() },
                        modifier = Modifier.weight(1f),
                    ) { Text("Cancel") }
                    Button(
                        onClick = { viewModel.saveProfile() },
                        modifier = Modifier.weight(1f),
                    ) { Text("Save") }
                }
            }
        }
    }

    // Delete confirmation
    deleteConfirmId?.let { id ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Delete connection?") },
            text = { Text("This will permanently delete the profile and any saved password.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(id) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDelete() }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ProfileRow(
    profile: ConnectionProfile,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(profile.name) },
        supportingContent = {
            Text(
                text = "${profile.username}@${profile.host}:${profile.port}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Icon(
                imageVector = if (profile.authType == AuthType.PRIVATE_KEY)
                    Icons.Filled.Key else Icons.Filled.Password,
                contentDescription = profile.authType.name,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit ${profile.name}")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete ${profile.name}",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
    )
}
