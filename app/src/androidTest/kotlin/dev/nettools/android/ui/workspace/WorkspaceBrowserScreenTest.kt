package dev.nettools.android.ui.workspace

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import dev.nettools.android.domain.model.WorkspaceEntry
import org.junit.Rule
import org.junit.Test

class WorkspaceBrowserScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun supportsWorkspaceCrudFlows() {
        val createDialogOpen = mutableStateOf(false)
        val renameTarget = mutableStateOf<WorkspaceEntry?>(null)
        val moveTarget = mutableStateOf<WorkspaceEntry?>(null)
        val deleteTarget = mutableStateOf<WorkspaceEntry?>(null)
        val createdName = mutableStateOf<String?>(null)
        val renamedEntry = mutableStateOf<Pair<String, String>?>(null)
        val movedEntry = mutableStateOf<Pair<String, String>?>(null)
        val deletedPath = mutableStateOf<String?>(null)
        val entry = WorkspaceEntry(
            path = "/notes.txt",
            name = "notes.txt",
            isDirectory = false,
            sizeBytes = 12,
            modifiedAt = 1_700_000_000_000,
        )

        composeRule.setContent {
            WorkspaceBrowserContent(
                state = WorkspaceBrowserUiState(
                    currentPath = "/",
                    workspaceRootPath = "/workspace",
                    entries = listOf(entry),
                    isLoading = false,
                ),
                snackbarHostState = remember { SnackbarHostState() },
                createDialogOpen = createDialogOpen.value,
                renameTarget = renameTarget.value,
                moveTarget = moveTarget.value,
                deleteTarget = deleteTarget.value,
                onNavigateBack = {},
                onOpenCreateDialog = { createDialogOpen.value = true },
                onDismissCreateDialog = { createDialogOpen.value = false },
                onOpenRenameDialog = { renameTarget.value = it },
                onDismissRenameDialog = { renameTarget.value = null },
                onOpenMoveDialog = { moveTarget.value = it },
                onDismissMoveDialog = { moveTarget.value = null },
                onOpenDeleteDialog = { deleteTarget.value = it },
                onDismissDeleteDialog = { deleteTarget.value = null },
                onImportRequest = {},
                onExportRequest = {},
                onRefresh = {},
                onNavigateUp = {},
                onOpenDirectory = {},
                onCreateDirectory = {
                    createDialogOpen.value = false
                    createdName.value = it
                },
                onRename = { path, newName ->
                    renameTarget.value = null
                    renamedEntry.value = path to newName
                },
                onMove = { path, destination ->
                    moveTarget.value = null
                    movedEntry.value = path to destination
                },
                onDelete = {
                    deleteTarget.value = null
                    deletedPath.value = it
                },
            )
        }

        composeRule.onNodeWithText("notes.txt").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Create directory").performClick()
        composeRule.onNodeWithTag("workspace-name-input").performTextReplacement("logs")
        composeRule.onNodeWithTag("workspace-dialog-confirm").performClick()

        composeRule.onAllNodesWithContentDescription("Workspace entry actions")[0].performClick()
        composeRule.onAllNodesWithText("Rename")[0].performClick()
        composeRule.onNodeWithTag("workspace-name-input").performTextReplacement("archive.txt")
        composeRule.onNodeWithTag("workspace-dialog-confirm").performClick()

        composeRule.onAllNodesWithContentDescription("Workspace entry actions")[0].performClick()
        composeRule.onAllNodesWithText("Move")[0].performClick()
        composeRule.onNodeWithTag("workspace-name-input").performTextReplacement("/archive")
        composeRule.onNodeWithTag("workspace-dialog-confirm").performClick()

        composeRule.onAllNodesWithContentDescription("Workspace entry actions")[0].performClick()
        composeRule.onAllNodesWithText("Delete")[0].performClick()
        composeRule.onNodeWithTag("workspace-delete-confirm").performClick()

        composeRule.runOnIdle {
            check(createdName.value == "logs")
            check(renamedEntry.value == ("/notes.txt" to "archive.txt"))
            check(movedEntry.value == ("/notes.txt" to "/archive"))
            check(deletedPath.value == "/notes.txt")
        }
    }
}
