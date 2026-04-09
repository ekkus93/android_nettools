package dev.nettools.android.ui.connections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Edit / Create dialog
    editState?.let { state ->
        ProfileEditDialog(
            state = state,
            onNameChange = viewModel::onNameChange,
            onHostChange = viewModel::onHostChange,
            onPortChange = viewModel::onPortChange,
            onUsernameChange = viewModel::onUsernameChange,
            onAuthTypeChange = viewModel::onAuthTypeChange,
            onKeyPathChange = viewModel::onKeyPathChange,
            onSavePasswordChange = viewModel::onSavePasswordChange,
            onPasswordChange = viewModel::onPasswordChange,
            onSave = { viewModel.saveProfile() },
            onDismiss = { viewModel.dismissEditor() },
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditDialog(
    state: ProfileEditState,
    onNameChange: (String) -> Unit,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onAuthTypeChange: (AuthType) -> Unit,
    onKeyPathChange: (String) -> Unit,
    onSavePasswordChange: (Boolean) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    var authDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.isNew) "New Connection" else "Edit Connection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.host,
                    onValueChange = onHostChange,
                    label = { Text("Host") },
                    isError = state.hostError != null,
                    supportingText = state.hostError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.port,
                    onValueChange = onPortChange,
                    label = { Text("Port") },
                    isError = state.portError != null,
                    supportingText = state.portError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.username,
                    onValueChange = onUsernameChange,
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
                            onClick = { onAuthTypeChange(AuthType.PASSWORD); authDropdownExpanded = false },
                        )
                        DropdownMenuItem(
                            text = { Text("Private Key") },
                            onClick = { onAuthTypeChange(AuthType.PRIVATE_KEY); authDropdownExpanded = false },
                        )
                    }
                }

                when (state.authType) {
                    AuthType.PASSWORD -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = state.savePassword, onCheckedChange = onSavePasswordChange)
                            Text("Save password")
                        }
                        if (state.savePassword) {
                            OutlinedTextField(
                                value = state.password,
                                onValueChange = onPasswordChange,
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    AuthType.PRIVATE_KEY -> {
                        OutlinedTextField(
                            value = state.keyPath,
                            onValueChange = onKeyPathChange,
                            label = { Text("Key file path") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
