package dev.nettools.android.ui.workspace

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nettools.android.domain.model.WorkspaceEntry
import dev.nettools.android.domain.repository.WorkspaceRepository
import dev.nettools.android.domain.usecase.curl.ExportWorkspaceFileUseCase
import dev.nettools.android.domain.usecase.curl.ImportWorkspaceFileUseCase
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
    @param:ApplicationContext private val context: Context,
    private val workspaceRepository: WorkspaceRepository,
    private val importWorkspaceFile: ImportWorkspaceFileUseCase,
    private val exportWorkspaceFile: ExportWorkspaceFileUseCase,
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
            runCatching { workspaceRepository.createDirectory(targetPath) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = CurlUserMessageFormatter.workspaceFailure("create the directory", error)) }
                }
            load()
        }
    }

    /** Renames the given workspace entry. */
    fun rename(path: String, newName: String) {
        viewModelScope.launch {
            runCatching { workspaceRepository.rename(path, newName.trim()) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = CurlUserMessageFormatter.workspaceFailure("rename the workspace item", error)) }
                }
            load()
        }
    }

    /** Moves the given workspace entry to the destination directory path. */
    fun move(path: String, destinationDirectoryPath: String) {
        viewModelScope.launch {
            runCatching { workspaceRepository.move(path, workspaceRepository.normalizePath(destinationDirectoryPath)) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = CurlUserMessageFormatter.workspaceFailure("move the workspace item", error)) }
                }
            load()
        }
    }

    /** Deletes the given workspace entry. */
    fun delete(path: String) {
        viewModelScope.launch {
            runCatching { workspaceRepository.delete(path) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = CurlUserMessageFormatter.workspaceFailure("delete the workspace item", error)) }
                }
            load()
        }
    }

    /** Clears the current transient error message. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Imports the selected [uris] into the current workspace directory. */
    fun importFiles(uris: List<Uri>) {
        viewModelScope.launch {
            uris.forEach { uri ->
                runCatching {
                    val fileName = queryDisplayName(uri)
                    val input = context.contentResolver.openInputStream(uri)
                        ?: error("Unable to open the selected file. Permission may have been revoked.")
                    importWorkspaceFile(
                        targetDirectoryPath = _uiState.value.currentPath,
                        fileName = fileName,
                        inputStream = input,
                    )
                }.onFailure { error ->
                    _uiState.update { it.copy(errorMessage = CurlUserMessageFormatter.workspaceFailure("import the file", error)) }
                }
            }
            load()
        }
    }

    /** Exports the workspace file at [path] into the selected [uri]. */
    fun exportFile(path: String, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val output = context.contentResolver.openOutputStream(uri)
                    ?: error("Unable to write the selected destination. Permission may have been revoked.")
                exportWorkspaceFile(path = path, outputStream = output)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = CurlUserMessageFormatter.workspaceFailure("export the file", error)) }
            }
        }
    }

    private fun queryDisplayName(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        return cursor.getString(index)
                    }
                }
            }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "imported-file"
    }
}

private fun buildChildPath(parent: String, childName: String): String =
    if (parent == "/") "/$childName" else "$parent/$childName"
