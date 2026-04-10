package dev.nettools.android.ui.curl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.nettools.android.domain.model.CurlRunStatus

/**
 * Screen showing the live or retained output for a curl run.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurlResultsScreen(
    runId: String,
    navController: NavController,
    viewModel: CurlResultsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showMetadata by remember { mutableStateOf(false) }

    LaunchedEffect(state.saveMessage) {
        state.saveMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSaveMessage()
        }
    }

    CurlResultsContent(
        runId = runId,
        state = state,
        snackbarHostState = snackbarHostState,
        showMetadata = showMetadata,
        onNavigateBack = navController::popBackStack,
        onToggleMetadata = { showMetadata = !showMetadata },
        onCancel = if (state.canCancel) viewModel::cancelRun else null,
        onSaveOutput = viewModel::saveOutput,
        onCopyStdout = { copyToClipboard(context, "curl stdout", state.stdoutText) },
        onCopyStderr = { copyToClipboard(context, "curl stderr", state.stderrText) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CurlResultsContent(
    runId: String,
    state: CurlResultsUiState,
    snackbarHostState: SnackbarHostState,
    showMetadata: Boolean,
    onNavigateBack: () -> Unit,
    onToggleMetadata: () -> Unit,
    onCancel: (() -> Unit)?,
    onSaveOutput: () -> Unit,
    onCopyStdout: () -> Unit,
    onCopyStderr: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Curl results") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        if (state.isMissing) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Run not found", style = MaterialTheme.typography.titleMedium)
                Text(
                    "No retained data was found for $runId.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatusCard(
                commandText = state.commandText,
                status = state.status,
                exitCode = state.exitCode,
                onToggleMetadata = onToggleMetadata,
                onCancel = onCancel,
                onSaveOutput = onSaveOutput,
            )

            if (showMetadata) {
                MetadataCard(
                    state = state,
                    showEffectiveCommand = !state.effectiveCommandText.isNullOrBlank() &&
                        state.effectiveCommandText != state.commandText,
                )
            }

            state.cleanupWarning?.let { warning ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = warning,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            OutputCard(
                title = "stdout",
                text = state.stdoutText,
                truncated = state.stdoutTruncated,
                onCopy = onCopyStdout,
            )
            OutputCard(
                title = "stderr",
                text = state.stderrText,
                truncated = state.stderrTruncated,
                onCopy = onCopyStderr,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatusCard(
    commandText: String,
    status: CurlRunStatus?,
    exitCode: Int?,
    onToggleMetadata: () -> Unit,
    onCancel: (() -> Unit)?,
    onSaveOutput: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = status.toDisplayLabel(),
                style = MaterialTheme.typography.titleMedium,
                color = status.toDisplayColor(),
            )
            Text(
                text = commandText,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onToggleMetadata) {
                    Text(if (exitCode == null) "Show details" else "Show exit details")
                }
                OutlinedButton(onClick = onSaveOutput) {
                    Text("Save output")
                }
                if (onCancel != null) {
                    Button(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataCard(
    state: CurlResultsUiState,
    showEffectiveCommand: Boolean,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Run details", style = MaterialTheme.typography.titleSmall)
            DetailLine("Status", state.status.toDisplayLabel())
            DetailLine("Exit code", state.exitCode?.toString() ?: "Unavailable")
            DetailLine("Duration", state.durationMillis.toDurationLabel())
            DetailLine("Cleanup", state.cleanupStatus.toDisplayLabel())
            if (showEffectiveCommand) {
                Text(
                    text = "Executed command",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SelectionContainer {
                    Text(
                        text = state.effectiveCommandText.orEmpty(),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    )
                }
            }
        }
    }
}

@Composable
private fun OutputCard(
    title: String,
    text: String,
    truncated: Boolean,
    onCopy: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                OutlinedButton(onClick = onCopy, enabled = text.isNotBlank()) {
                    Text("Copy")
                }
            }
            if (truncated) {
                Text(
                    text = "Output was truncated at the retention cap.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            SelectionContainer {
                Text(
                    text = text.ifBlank { "No $title captured." },
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            modifier = Modifier.weight(0.4f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun dev.nettools.android.domain.model.CurlCleanupStatus?.toDisplayLabel(): String = when (this) {
    dev.nettools.android.domain.model.CurlCleanupStatus.SUCCEEDED -> "Succeeded"
    dev.nettools.android.domain.model.CurlCleanupStatus.FAILED -> "Failed"
    dev.nettools.android.domain.model.CurlCleanupStatus.SKIPPED -> "Skipped"
    null -> "Not recorded"
}

@Composable
private fun CurlRunStatus?.toDisplayColor() = when (this) {
    CurlRunStatus.COMPLETED -> MaterialTheme.colorScheme.primary
    CurlRunStatus.FAILED -> MaterialTheme.colorScheme.error
    CurlRunStatus.CANCELLED -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.onSurface
}

private fun CurlRunStatus?.toDisplayLabel(): String = when (this) {
    CurlRunStatus.QUEUED -> "Queued"
    CurlRunStatus.VALIDATING -> "Validating"
    CurlRunStatus.IN_PROGRESS -> "Running"
    CurlRunStatus.COMPLETED -> "Completed"
    CurlRunStatus.FAILED -> "Failed"
    CurlRunStatus.CANCELLED -> "Cancelled"
    null -> "Unknown"
}

private fun Long?.toDurationLabel(): String {
    if (this == null) return "Unavailable"
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
