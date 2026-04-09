package dev.nettools.android.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import dev.nettools.android.util.toFormattedSize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Transfer History screen — lists all past transfers with status badges.
 *
 * @param navController Navigation controller for back navigation.
 * @param viewModel The [HistoryViewModel] supplying history data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val history by viewModel.history.collectAsState()
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
                    if (history.isNotEmpty()) {
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
        if (history.isEmpty()) {
            EmptyHistoryPlaceholder(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(history, key = { it.id }) { entry ->
                    HistoryEntryRow(entry = entry)
                    HorizontalDivider()
                }
            }
        }
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
private fun HistoryEntryRow(entry: TransferHistoryEntry) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) }
    ListItem(
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
private fun EmptyHistoryPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No transfers yet", style = MaterialTheme.typography.titleMedium)
            Text(
                "Completed transfers will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
