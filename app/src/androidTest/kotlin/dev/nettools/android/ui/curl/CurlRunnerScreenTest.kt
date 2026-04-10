package dev.nettools.android.ui.curl

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class CurlRunnerScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsValidationErrorForMalformedCommand() {
        composeRule.setContent {
            CurlRunnerContent(
                state = CurlRunnerUiState(
                    commandText = "curl --bogus https://example.com",
                    validationMessages = listOf("Unknown option: --bogus"),
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onNavigateBack = {},
                onCommandChange = {},
                onOpenResults = {},
                onOpenWorkspace = {},
                onOpenLogs = {},
                onOpenSettings = {},
                onRun = {},
            )
        }

        composeRule.onNodeWithText("Unknown option: --bogus").assertIsDisplayed()
    }

    @Test
    fun runsValidCommandThroughUi() {
        val commandText = mutableStateOf("")
        val submittedCommand = mutableStateOf<String?>(null)

        composeRule.setContent {
            CurlRunnerContent(
                state = CurlRunnerUiState(commandText = commandText.value),
                snackbarHostState = remember { SnackbarHostState() },
                onNavigateBack = {},
                onCommandChange = { commandText.value = it },
                onOpenResults = {},
                onOpenWorkspace = {},
                onOpenLogs = {},
                onOpenSettings = {},
                onRun = { submittedCommand.value = commandText.value },
            )
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("curl https://example.com")
        composeRule.onNodeWithText("Run").performClick()

        composeRule.runOnIdle {
            check(submittedCommand.value == "curl https://example.com")
        }
    }
}
