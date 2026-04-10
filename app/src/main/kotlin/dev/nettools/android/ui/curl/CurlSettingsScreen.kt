package dev.nettools.android.ui.curl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.nettools.android.util.toFormattedSize

/**
 * Screen for curl feature settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurlSettingsScreen(
    navController: NavController,
    viewModel: CurlSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Curl settings") },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsToggleCard(
                title = "Persistent logs",
                body = "Keep stdout and stderr across runs. Off by default to reduce sensitive data retention.",
                checked = state.settings.loggingEnabled,
                onCheckedChange = viewModel::setLoggingEnabled,
            )
            SettingsToggleCard(
                title = "Saved command history",
                body = "Retain the typed curl command in stored run history. Off by default.",
                checked = state.settings.saveHistoryEnabled,
                onCheckedChange = viewModel::setSaveHistoryEnabled,
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Workspace", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = state.effectiveWorkspaceRoot.ifBlank { "Loading workspace root..." },
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "Output retention: ${state.settings.stdoutBytesCap.toLong().toFormattedSize()} stdout, ${state.settings.stderrBytesCap.toLong().toFormattedSize()} stderr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Unix-style local paths entered in curl commands are resolved inside this workspace root.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OutlinedButton(
                onClick = viewModel::clearLogs,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Clear stored curl logs")
            }
        }
    }
}

@Composable
private fun SettingsToggleCard(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}
