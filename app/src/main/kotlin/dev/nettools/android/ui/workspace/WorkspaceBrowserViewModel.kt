package dev.nettools.android.ui.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.domain.model.WorkspaceEntry
import dev.nettools.android.domain.repository.WorkspaceRepository
import dev.nettools.android.domain.usecase.curl.ExportWorkspaceDocumentUseCase
import dev.nettools.android.domain.usecase.curl.ImportWorkspaceDocumentUseCase
import dev.nettools.android.util.CurlUserMessageFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the curl workspace browser.
 */
data class WorkspaceBrowserUiState(
    val currentPath: String = "/",
    val workspaceRootPath: String = "",
    val entries: List<WorkspaceEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

/**
 * ViewModel for the curl workspace browser screen.
 */
@HiltViewModel
class WorkspaceBrowserViewModel @Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val importWorkspaceDocument: ImportWorkspaceDocumentUseCase,
    private val exportWorkspaceDocument: ExportWorkspaceDocumentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceBrowserUiState())
    val uiState: StateFlow<WorkspaceBrowserUiState> = _uiState.asStateFlow()

    init {
        load(path = "/")
    }

    /** Loads the requested workspace directory. */
    fun load(
        path: String = _uiState.value.currentPath,
        preservedErrorMessage: String? = null,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = preservedErrorMessage) }
            runCatching {
                workspaceRepository.list(path)
            }.onSuccess { entries ->
                _uiState.update {
                    it.copy(
                        currentPath = workspaceRepository.normalizePath(path),
                        workspaceRootPath = workspaceRepository.getWorkspaceRootPath(),
                        entries = entries,
                        isLoading = false,
                        errorMessage = preservedErrorMessage,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = CurlUserMessageFormatter.workspaceFailure("load this workspace directory", error),
                    )
                }
            }
        }
    }

    /** Navigates into the selected directory. */
    fun openDirectory(path: String) {
        load(path)
    }

    /** Navigates to the parent directory when not already at root. */
    fun navigateUp() {
        if (_uiState.value.currentPath == "/") return
        val parent = _uiState.value.currentPath.substringBeforeLast('/', missingDelimiterValue = "")
        load(if (parent.isBlank()) "/" else parent)
    }

    /** Creates a new directory below the current path. */
    fun createDirectory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Directory name cannot be blank.") }
            return
        }
        val targetPath = buildChildPath(_uiState.value.currentPath, trimmed)
        viewModelScope.launch {
            var operationError: String? = null
            runCatching { workspaceRepository.createDirectory(targetPath) }
                .onFailure { error ->
                    operationError = CurlUserMessageFormatter.workspaceFailure("create the directory", error)
                }
            load(preservedErrorMessage = operationError)
        }
    }

    /** Renames the given workspace entry. */
    fun rename(path: String, newName: String) {
        viewModelScope.launch {
            var operationError: String? = null
            runCatching { workspaceRepository.rename(path, newName.trim()) }
                .onFailure { error ->
                    operationError = CurlUserMessageFormatter.workspaceFailure("rename the workspace item", error)
                }
            load(preservedErrorMessage = operationError)
        }
    }

    /** Moves the given workspace entry to the destination directory path. */
    fun move(path: String, destinationDirectoryPath: String) {
        viewModelScope.launch {
            var operationError: String? = null
            runCatching { workspaceRepository.move(path, workspaceRepository.normalizePath(destinationDirectoryPath)) }
                .onFailure { error ->
                    operationError = CurlUserMessageFormatter.workspaceFailure("move the workspace item", error)
                }
            load(preservedErrorMessage = operationError)
        }
    }

    /** Deletes the given workspace entry. */
    fun delete(path: String) {
        viewModelScope.launch {
            var operationError: String? = null
            runCatching { workspaceRepository.delete(path) }
                .onFailure { error ->
                    operationError = CurlUserMessageFormatter.workspaceFailure("delete the workspace item", error)
                }
            load(preservedErrorMessage = operationError)
        }
    }

    /** Clears the current transient error message. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Imports the selected [uris] into the current workspace directory. */
    fun importFiles(documentUris: List<String>) {
        viewModelScope.launch {
            var operationError: String? = null
            documentUris.forEach { documentUri ->
                runCatching {
                    importWorkspaceDocument(
                        targetDirectoryPath = _uiState.value.currentPath,
                        documentUri = documentUri,
                    )
                }.onFailure { error ->
                    operationError = CurlUserMessageFormatter.workspaceFailure("import the file", error)
                }
            }
            load(preservedErrorMessage = operationError)
        }
    }

    /** Exports the workspace file at [path] into the selected [uri]. */
    fun exportFile(path: String, destinationUri: String) {
        viewModelScope.launch {
            runCatching {
                exportWorkspaceDocument(path = path, destinationUri = destinationUri)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = CurlUserMessageFormatter.workspaceFailure("export the file", error)) }
            }
        }
    }
}

private fun buildChildPath(parent: String, childName: String): String =
    if (parent == "/") "/$childName" else "$parent/$childName"
