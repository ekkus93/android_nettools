package dev.nettools.android.ui.sftp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.nettools.android.domain.model.RemoteFileEntry
import dev.nettools.android.service.RemotePickerMode
import dev.nettools.android.util.toFormattedSize

/**
 * SFTP Browser screen — navigates the remote directory tree.
 * When launched as a path picker (i.e. from the Transfer screen),
 * selecting a file or directory returns its path to the previous back stack entry.
 *
 * @param navController Navigation controller for routing.
 * @param viewModel The [SftpBrowserViewModel] managing remote state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SftpBrowserScreen(
    navController: NavController,
    viewModel: SftpBrowserViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Propagate selected path back to the Transfer screen
    LaunchedEffect(state.selectedPath) {
        state.selectedPath?.let { path ->
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("remote_path", path)
            navController.popBackStack()
        }
    }

    // Show errors as snackbar
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.currentPath,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                actions = {
                    var sortMenuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.Filled.Home, contentDescription = "Home directory")
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { viewModel.requestNewDir() }) {
                        Icon(Icons.Filled.CreateNewFolder, contentDescription = "New directory")
                    }
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(Icons.Filled.SortByAlpha, contentDescription = "Sort order")
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                        ) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (order) {
                                                SortOrder.NAME -> "Sort by Name"
                                                SortOrder.SIZE -> "Sort by Size"
                                                SortOrder.DATE -> "Sort by Date"
                                            }
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOrder(order)
                                        sortMenuExpanded = false
                                    },
                                    leadingIcon = if (state.sortOrder == order) {
                                        { Icon(Icons.Filled.Check, contentDescription = null) }
                                    } else null,
                                )
                            }
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Breadcrumb trail — always visible
            BreadcrumbRow(
                breadcrumbs = state.breadcrumbs,
                onNavigate = { viewModel.navigate(it) },
            )
            HorizontalDivider()

            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    !state.isConnected && state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Text(
                                    "Connecting…",
                                    modifier = Modifier.padding(top = 12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                    state.entries.isEmpty() && !state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Empty directory", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.entries, key = { it.path }) { entry ->
                                FileEntryRow(
                                    entry = entry,
                                    pickerMode = state.pickerMode,
                                    onNavigate = { viewModel.navigate(entry.path) },
                                    onSelect = { viewModel.selectEntry(entry) },
                                    onRename = { viewModel.requestRename(entry) },
                                    onDelete = { viewModel.requestDelete(entry) },
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename dialog
    state.renameTarget?.let {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRename() },
            title = { Text("Rename") },
            text = {
                OutlinedTextField(
                    value = state.renameNewName,
                    onValueChange = viewModel::onRenameNameChange,
                    label = { Text("New name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmRename() }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRename() }) { Text("Cancel") }
            },
        )
    }

    // Delete dialog
    state.deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Delete?") },
            text = { Text("Permanently delete \"${target.name}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDelete() }) { Text("Cancel") }
            },
        )
    }

    // New directory dialog
    if (state.showNewDirDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissNewDir() },
            title = { Text("New directory") },
            text = {
                OutlinedTextField(
                    value = state.newDirName,
                    onValueChange = viewModel::onNewDirNameChange,
                    label = { Text("Directory name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmNewDir() }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissNewDir() }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun BreadcrumbRow(
    breadcrumbs: List<String>,
    onNavigate: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(breadcrumbs) { crumb ->
            Text(
                text = crumb.substringAfterLast('/').ifBlank { crumb },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigate(crumb) },
            )
            if (crumb != breadcrumbs.last()) {
                Text(
                    text = " /",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FileEntryRow(
    entry: RemoteFileEntry,
    pickerMode: RemotePickerMode,
    onNavigate: () -> Unit,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            if (!entry.isDirectory) {
                Text(
                    text = "${entry.sizeBytes.toFormattedSize()} · ${entry.permissions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = if (entry.isDirectory) Icons.Filled.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = if (entry.isDirectory) "Directory" else "File",
                tint = if (entry.isDirectory) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        },
        trailingContent = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More options for ${entry.name}",
                        modifier = Modifier.size(20.dp),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Filled.DriveFileRenameOutline, null) },
                        onClick = { onRename(); menuExpanded = false },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = { onDelete(); menuExpanded = false },
                    )
                }
            }
        },
        modifier = Modifier.clickable {
            when {
                pickerMode == RemotePickerMode.PICK_DIRECTORY && entry.isDirectory -> onSelect()
                pickerMode == RemotePickerMode.PICK_FILE && entry.isDirectory -> onNavigate()
                pickerMode == RemotePickerMode.PICK_FILE -> onSelect()
                entry.isDirectory -> onNavigate()
                else -> onSelect()
            }
        },
    )
}
