package dev.nettools.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.nettools.android.ui.HomeScreen
import dev.nettools.android.ui.connections.SavedConnectionsScreen
import dev.nettools.android.ui.history.HistoryScreen
import dev.nettools.android.ui.navigation.Routes
import dev.nettools.android.ui.progress.ProgressScreen
import dev.nettools.android.ui.sftp.SftpBrowserScreen
import dev.nettools.android.ui.theme.NetToolsTheme
import dev.nettools.android.ui.transfer.TransferScreen

/**
 * Main entry point activity for Android NetTools.
 * Sets up Jetpack Compose UI with Navigation Compose [NavHost].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetToolsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Routes.HOME,
                    ) {
                        composable(Routes.HOME) {
                            HomeScreen(navController = navController)
                        }
                        composable(Routes.TRANSFER) {
                            TransferScreen(navController = navController)
                        }
                        composable(Routes.SFTP_BROWSER) {
                            SftpBrowserScreen(
                                navController = navController,
                                pickerMode = true,
                            )
                        }
                        composable(Routes.SAVED_CONNECTIONS) {
                            SavedConnectionsScreen(navController = navController)
                        }
                        composable(Routes.HISTORY) {
                            HistoryScreen(navController = navController)
                        }
                        composable(Routes.PROGRESS) { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                            ProgressScreen(jobId = jobId, navController = navController)
                        }
                    }
                }
            }
        }
    }
}
