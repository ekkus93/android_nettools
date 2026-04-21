package dev.nettools.android.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CompareArrows
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.nettools.android.R
import dev.nettools.android.ui.navigation.Routes

/**
 * Home screen composable — the main entry point of the application UI.
 * Provides navigation to SCP/SFTP transfer, saved connections, and history screens.
 * Shows a banner when active transfers are in progress.
 *
 * @param navController Navigation controller for routing.
 * @param viewModel The [HomeViewModel] providing active transfer state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val activeCount by viewModel.activeTransferCount.collectAsState()
    val firstJobId by viewModel.firstActiveJobId.collectAsState()

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
            // Active transfer banner
            if (activeCount > 0) {
                Card(
                    onClick = {
                        firstJobId?.let { navController.navigate(Routes.progress(it)) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "$activeCount transfer(s) in progress",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            // Hero image and tagline
            Image(
                painter = painterResource(id = R.drawable.ic_nettools_hero),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Text(
                text = "SSH file transfers, simplified",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            NavCard(
                title = "SCP Transfer",
                subtitle = "Upload or download files over SSH",
                icon = { Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, Modifier.size(48.dp)) },
                onClick = { navController.navigate(Routes.TRANSFER) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleStyle = MaterialTheme.typography.titleLarge,
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
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            icon()
            Text(title, style = titleStyle)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
