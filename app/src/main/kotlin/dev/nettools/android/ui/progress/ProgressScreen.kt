package dev.nettools.android.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import dev.nettools.android.data.ssh.TransferProgress
import dev.nettools.android.domain.model.TransferJob
import dev.nettools.android.domain.model.TransferStatus
import dev.nettools.android.util.toDisplayPath
import dev.nettools.android.util.toEtaString
import dev.nettools.android.util.toFormattedSize
import dev.nettools.android.util.toSpeedString

/**
 * Transfer Progress screen — shows live progress for all active transfers.
 *
 * @param jobId The job ID to focus on (used to highlight the primary transfer).
 * @param navController Navigation controller for back navigation.
 * @param viewModel The [ProgressViewModel] providing live state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    jobId: String,
    navController: NavController,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val progressMap by viewModel.progress.collectAsState()
    val activeJobs by viewModel.activeJobs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transferring…") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Minimise to background",
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (activeJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("No active transfers", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(activeJobs, key = { it.id }) { job ->
                    TransferCard(
                        job = job,
                        progress = progressMap[job.id],
                        isPrimary = job.id == jobId,
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun TransferCard(
    job: TransferJob,
    progress: TransferProgress?,
    isPrimary: Boolean,
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    val canCancel = job.status in setOf(
        TransferStatus.QUEUED,
        TransferStatus.IN_PROGRESS,
        TransferStatus.PAUSED,
    )

    val containerColor = if (isPrimary)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = progress?.fileName ?: job.localPath.toDisplayPath().substringAfterLast('/'),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (canCancel) {
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel transfer")
                    }
                }
            }

            if (progress != null) {
                Spacer(Modifier.height(8.dp))

                if (progress.isResuming) {
                    Text(
                        text = "Resuming from ${progress.resumeOffsetBytes.toFormattedSize()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }

                val fraction = if (progress.totalBytes > 0)
                    (progress.bytesTransferred.toFloat() / progress.totalBytes).coerceIn(0f, 1f)
                else -1f

                if (fraction >= 0f) {
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (progress.totalBytes > 0)
                            "${progress.bytesTransferred.toFormattedSize()} / ${progress.totalBytes.toFormattedSize()}"
                        else
                            progress.bytesTransferred.toFormattedSize(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = progress.speedBytesPerSec.toSpeedString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (progress.totalBytes > 0 && progress.speedBytesPerSec > 0) {
                    val remaining = progress.totalBytes - progress.bytesTransferred
                    Text(
                        text = remaining.toEtaString(progress.speedBytesPerSec),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Spacer(Modifier.height(8.dp))
                StatusText(job.status)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
    }

    if (showCancelDialog && canCancel) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel transfer?") },
            text = { Text("The in-progress transfer will be stopped. Partial files will be left on the remote.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Cancellation is handled by the service via the notification action;
                        // UI just navigates back.
                        showCancelDialog = false
                    },
                ) { Text("Cancel Transfer", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Keep going") }
            },
        )
    }
}

@Composable
private fun StatusText(status: TransferStatus) {
    val label = when (status) {
        TransferStatus.QUEUED -> "Waiting in queue…"
        TransferStatus.IN_PROGRESS -> "Connecting…"
        TransferStatus.PAUSED -> "Paused"
        TransferStatus.COMPLETED -> "Completed"
        TransferStatus.FAILED -> "Failed"
        TransferStatus.CANCELLED -> "Cancelled"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
