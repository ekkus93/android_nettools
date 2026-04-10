package dev.nettools.android.ui.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.domain.model.WorkspaceEntry
import dev.nettools.android.domain.repository.WorkspaceRepository
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceBrowserUiState())
    val uiState: StateFlow<WorkspaceBrowserUiState> = _uiState.asStateFlow()

    init {
        load(path = "/")
    }

    /** Loads the requested workspace directory. */
    fun load(path: String = _uiState.value.currentPath) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                workspaceRepository.list(path)
            }.onSuccess { entries ->
                _uiState.update {
                    it.copy(
                        currentPath = workspaceRepository.normalizePath(path),
                        workspaceRootPath = workspaceRepository.getWorkspaceRootPath(),
                        entries = entries,
                        isLoading = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Workspace operation failed.",
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
            runCatching { workspaceRepository.createDirectory(targetPath) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Unable to create directory.") }
                }
            load()
        }
    }

    /** Renames the given workspace entry. */
    fun rename(path: String, newName: String) {
        viewModelScope.launch {
            runCatching { workspaceRepository.rename(path, newName.trim()) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Unable to rename entry.") }
                }
            load()
        }
    }

    /** Moves the given workspace entry to the destination directory path. */
    fun move(path: String, destinationDirectoryPath: String) {
        viewModelScope.launch {
            runCatching { workspaceRepository.move(path, workspaceRepository.normalizePath(destinationDirectoryPath)) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Unable to move entry.") }
                }
            load()
        }
    }

    /** Deletes the given workspace entry. */
    fun delete(path: String) {
        viewModelScope.launch {
            runCatching { workspaceRepository.delete(path) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Unable to delete entry.") }
                }
            load()
        }
    }

    /** Clears the current transient error message. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

private fun buildChildPath(parent: String, childName: String): String =
    if (parent == "/") "/$childName" else "$parent/$childName"
