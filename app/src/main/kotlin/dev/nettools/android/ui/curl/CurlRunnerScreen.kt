package dev.nettools.android.ui.curl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.nettools.android.domain.model.CurlRunStatus
import dev.nettools.android.ui.navigation.Routes

/**
 * Main screen for entering and starting curl commands.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurlRunnerScreen(
    navController: NavController,
    viewModel: CurlRunnerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navigateToResults.collect { runId ->
            navController.navigate(Routes.curlResults(runId))
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Curl") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Paste a full curl command or just the arguments. Multiline commands with shell-style continuations are supported.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = state.commandText,
                onValueChange = viewModel::onCommandChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                label = { Text("Command") },
                placeholder = { Text("curl https://example.com") },
                supportingText = {
                    if (state.validationMessages.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            state.validationMessages.forEach { message ->
                                Text(message, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                },
                isError = state.validationMessages.isNotEmpty(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            )

            val activeRunId = state.activeRunId
            if (activeRunId != null) {
                ActiveCurlRunCard(
                    runId = activeRunId,
                    commandText = state.activeCommandText,
                    status = state.activeStatus,
                    onOpenResults = { navController.navigate(Routes.curlResults(activeRunId)) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { navController.navigate(Routes.CURL_WORKSPACE) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Workspace")
                }
                OutlinedButton(
                    onClick = { navController.navigate(Routes.CURL_LOGS) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Logs")
                }
                OutlinedButton(
                    onClick = { navController.navigate(Routes.CURL_SETTINGS) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Settings")
                }
            }

            Button(
                onClick = viewModel::runCommand,
                enabled = !state.hasActiveRun,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(if (state.hasActiveRun) "Run unavailable" else "Run")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ActiveCurlRunCard(
    runId: String,
    commandText: String,
    status: CurlRunStatus?,
    onOpenResults: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Active run", style = MaterialTheme.typography.titleMedium)
            Text(
                text = status.toRunnerStatusLabel(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = commandText.ifBlank { "Current command" },
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            )
            Text(
                text = "Run ID: $runId",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onOpenResults) {
                Text("Open results")
            }
        }
    }
}

private fun CurlRunStatus?.toRunnerStatusLabel(): String = when (this) {
    CurlRunStatus.QUEUED -> "Queued"
    CurlRunStatus.VALIDATING -> "Validating"
    CurlRunStatus.IN_PROGRESS -> "Running"
    CurlRunStatus.COMPLETED -> "Completed"
    CurlRunStatus.FAILED -> "Failed"
    CurlRunStatus.CANCELLED -> "Cancelled"
    null -> "Unknown"
}
