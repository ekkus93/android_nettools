package dev.nettools.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.nettools.android.ui.navigation.Routes

/**
 * Home screen composable — the main entry point of the application UI.
 * Provides navigation to SCP/SFTP transfer, saved connections, and history screens.
 *
 * @param navController Navigation controller for routing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("NetTools") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            NavCard(
                title = "Curl",
                subtitle = "Run raw curl commands with live stdout and stderr",
                icon = { Icon(Icons.Filled.Cloud, contentDescription = null, Modifier.size(36.dp)) },
                onClick = { navController.navigate(Routes.CURL) },
            )
            NavCard(
                title = "SCP Transfer",
                subtitle = "Upload or download files over SSH",
                icon = { Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, Modifier.size(36.dp)) },
                onClick = { navController.navigate(Routes.TRANSFER) },
            )
            NavCard(
                title = "Saved Connections",
                subtitle = "Manage SSH connection profiles",
                icon = { Icon(Icons.Filled.Dns, contentDescription = null, Modifier.size(36.dp)) },
                onClick = { navController.navigate(Routes.SAVED_CONNECTIONS) },
            )
            NavCard(
                title = "Transfer History",
                subtitle = "View past file transfers",
                icon = { Icon(Icons.Filled.History, contentDescription = null, Modifier.size(36.dp)) },
                onClick = { navController.navigate(Routes.HISTORY) },
            )
        }
    }
}

@Composable
private fun NavCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            icon()
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
