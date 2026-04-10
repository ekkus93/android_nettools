package dev.nettools.android.ui.curl

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class CurlResultsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsStdoutAndStderrSeparately() {
        composeRule.setContent {
            CurlResultsContent(
                runId = "run-1",
                state = CurlResultsUiState(
                    runId = "run-1",
                    commandText = "curl https://example.com",
                    stdoutText = "stdout body",
                    stderrText = "stderr body",
                ),
                snackbarHostState = remember { SnackbarHostState() },
                showMetadata = false,
                onNavigateBack = {},
                onToggleMetadata = {},
                onCancel = null,
                onSaveOutput = {},
                onCopyStdout = {},
                onCopyStderr = {},
            )
        }

        composeRule.onNodeWithText("stdout").assertIsDisplayed()
        composeRule.onNodeWithText("stdout body").assertIsDisplayed()
        composeRule.onNodeWithText("stderr").assertIsDisplayed()
        composeRule.onNodeWithText("stderr body").assertIsDisplayed()
    }
}
