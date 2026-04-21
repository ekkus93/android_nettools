package dev.nettools.android.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.nettools.android.domain.model.HistoryStatus
import dev.nettools.android.domain.model.TransferDirection
import dev.nettools.android.domain.model.TransferHistoryEntry
import dev.nettools.android.ui.navigation.Routes
import dev.nettools.android.util.toFormattedSize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Transfer History screen — lists all past transfers with status badges.
 * Supports filtering by file name, host, or remote directory via a search field,
 * and by status via filter chips. Tapping an entry shows a full-detail dialog.
 *
 * @param navController Navigation controller for back navigation and "Transfer again".
 * @param viewModel The [HistoryViewModel] supplying history data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val history by viewModel.history.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val selectedEntry by viewModel.selectedEntry.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
                actions = {
                    if (history.isNotEmpty() || searchQuery.isNotBlank()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear all history",
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by file, host, or path") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
            )

            // Status filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { viewModel.onStatusFilterChange(null) },
                        label = { Text("All") },
                    )
                }
                items(HistoryStatus.entries.toList()) { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { viewModel.onStatusFilterChange(status) },
                        label = {
                            Text(status.name.lowercase().replaceFirstChar { it.uppercase() })
                        },
                    )
                }
            }

            if (history.isEmpty()) {
                EmptyHistoryPlaceholder(
                    query = searchQuery,
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(history, key = { it.id }) { entry ->
                        HistoryEntryRow(
                            entry = entry,
                            onClick = { viewModel.onEntrySelected(entry) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    selectedEntry?.let { entry ->
        HistoryDetailDialog(
            entry = entry,
            onDismiss = viewModel::onDetailDismissed,
            onTransferAgain = { e ->
                navController.navigate(
                    Routes.transferPrefill(
                        host = e.host,
                        remoteDir = e.remoteDir,
                        fileName = e.fileName,
                        direction = e.direction,
                    )
                )
                viewModel.onDetailDismissed()
            },
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear history?") },
            text = { Text("All transfer history will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAll(); showClearDialog = false }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun HistoryDetailDialog(
    entry: TransferHistoryEntry,
    onDismiss: () -> Unit,
    onTransferAgain: (TransferHistoryEntry) -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = entry.fileName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow("Status", entry.status.name.replaceFirstChar { it.uppercase() })
                DetailRow("Direction", entry.direction.name.replaceFirstChar { it.uppercase() })
                DetailRow("Host", entry.host)
                DetailRow("Remote path", "${entry.remoteDir}/${entry.fileName}")
                DetailRow("Size", entry.fileSizeBytes.toFormattedSize())
                DetailRow("Date", dateFormat.format(Date(entry.timestamp)))
                if (entry.status == HistoryStatus.FAILED && !entry.errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = entry.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onTransferAgain(entry) }) {
                    Text("Transfer again")
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HistoryEntryRow(
    entry: TransferHistoryEntry,
    onClick: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) }
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (entry.direction == TransferDirection.UPLOAD)
                        Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = entry.direction.name,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = entry.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${entry.host} · ${entry.remoteDir}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = dateFormat.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = entry.fileSizeBytes.toFormattedSize(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatusBadge(entry.status)
            }
        },
    )
}

@Composable
private fun StatusBadge(status: HistoryStatus) {
    val (label, color) = when (status) {
        HistoryStatus.SUCCESS -> "Success" to MaterialTheme.colorScheme.primary
        HistoryStatus.FAILED -> "Failed" to MaterialTheme.colorScheme.error
        HistoryStatus.CANCELLED -> "Cancelled" to MaterialTheme.colorScheme.onSurfaceVariant
        HistoryStatus.RESUMED -> "Resumed" to MaterialTheme.colorScheme.tertiary
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
    )
}

@Composable
private fun EmptyHistoryPlaceholder(
    query: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (query.isBlank()) {
                Text("No transfers yet", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Completed transfers will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text("No matches for \"$query\"", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Try a different search term",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
