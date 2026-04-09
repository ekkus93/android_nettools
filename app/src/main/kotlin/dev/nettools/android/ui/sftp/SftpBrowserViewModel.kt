package dev.nettools.android.ui.sftp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.data.security.KnownHostsManager
import dev.nettools.android.data.ssh.SftpClient
import dev.nettools.android.data.ssh.SshConnectionManager
import dev.nettools.android.domain.model.AuthType
import dev.nettools.android.domain.model.RemoteFileEntry
import dev.nettools.android.domain.model.TransferError
import dev.nettools.android.service.TransferProgressHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.schmizz.sshj.SSHClient
import javax.inject.Inject

/** Sort order for remote directory listings. */
enum class SortOrder { NAME, SIZE, DATE }

/** UI state for the SFTP Browser screen. */
data class SftpBrowserUiState(
    val currentPath: String = "~",
    val entries: List<RemoteFileEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isConnected: Boolean = false,
    val breadcrumbs: List<String> = listOf("~"),
    val sortOrder: SortOrder = SortOrder.NAME,
    /** Non-null when the browser is in "pick" mode and an item has been selected. */
    val selectedPath: String? = null,
    /** Dialog state for rename operation. */
    val renameTarget: RemoteFileEntry? = null,
    val renameNewName: String = "",
    /** Dialog state for delete confirmation. */
    val deleteTarget: RemoteFileEntry? = null,
    /** Whether the new-directory dialog is visible. */
    val showNewDirDialog: Boolean = false,
    val newDirName: String = "",
)

/**
 * ViewModel for the SFTP Browser screen.
 * Opens an SSH session on demand and navigates the remote file system.
 *
 * @property sshConnectionManager Factory for SSH sessions.
 * @property sftpClient SFTP operations.
 * @property knownHostsManager Host-key trust management.
 */
@HiltViewModel
class SftpBrowserViewModel @Inject constructor(
    private val sshConnectionManager: SshConnectionManager,
    private val sftpClient: SftpClient,
    private val knownHostsManager: KnownHostsManager,
    private val progressHolder: TransferProgressHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SftpBrowserUiState())

    /** Current SFTP browser state. */
    val uiState: StateFlow<SftpBrowserUiState> = _uiState.asStateFlow()

    private var sshClient: SSHClient? = null

    // Connection params set before opening the browser
    private var host: String = ""
    private var port: Int = 22
    private var username: String = ""
    private var authType: AuthType = AuthType.PASSWORD
    private var password: String? = null
    private var keyPath: String? = null

    init {
        // Auto-connect if credentials were pre-loaded into TransferProgressHolder
        // (set by TransferViewModel.prepareSftpBrowse() before navigation).
        val params = progressHolder.pendingSftpConnectionParams
        if (params != null) {
            progressHolder.pendingSftpConnectionParams = null
            connect(
                host = params.host,
                port = params.port,
                username = params.username,
                authType = params.authType,
                password = params.password,
                keyPath = params.keyPath,
            )
        }
    }

    /**
     * Initialises the browser with connection parameters and opens the SSH session.
     * Should be called once, immediately after the ViewModel is created.
     *
     * @param host Remote hostname.
     * @param port SSH port.
     * @param username Remote username.
     * @param authType Authentication method.
     * @param password Plaintext password (for [AuthType.PASSWORD]).
     * @param keyPath Path to private key (for [AuthType.PRIVATE_KEY]).
     * @param initialPath Starting directory (default: home directory).
     */
    fun connect(
        host: String,
        port: Int,
        username: String,
        authType: AuthType,
        password: String? = null,
        keyPath: String? = null,
        initialPath: String = "~",
    ) {
        this.host = host
        this.port = port
        this.username = username
        this.authType = authType
        this.password = password
        this.keyPath = keyPath
        viewModelScope.launch(Dispatchers.IO) {
            openSshAndNavigate(initialPath)
        }
    }

    /**
     * Navigates into [path], adding it to the breadcrumb trail.
     *
     * @param path Absolute path on the remote host.
     */
    fun navigate(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { sftpClient.listDirectory(requireClient(), path) }
                .onSuccess { entries ->
                    val crumbs = buildBreadcrumbs(path)
                    val order = _uiState.value.sortOrder
                    _uiState.update {
                        it.copy(
                            currentPath = path,
                            entries = entries.sortedWith(sortComparator(order)),
                            breadcrumbs = crumbs,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { handleError(it) }
        }
    }

    /** Navigates to the parent of the current directory. */
    fun navigateUp() {
        val current = _uiState.value.currentPath
        val parent = current.substringBeforeLast('/', "~").ifBlank { "~" }
        navigate(parent)
    }

    /** Navigates to the remote home directory. */
    fun navigateHome() = navigate("~")

    /** Refreshes the current directory listing. */
    fun refresh() = navigate(_uiState.value.currentPath)

    /**
     * Selects [entry] as the result when the browser is in picker mode.
     * The selection is surfaced via [SftpBrowserUiState.selectedPath].
     */
    fun selectEntry(entry: RemoteFileEntry) {
        _uiState.update { it.copy(selectedPath = entry.path) }
    }

    // ── Rename ────────────────────────────────────────────────────────────────

    /** Opens the rename dialog for [entry]. */
    fun requestRename(entry: RemoteFileEntry) =
        _uiState.update { it.copy(renameTarget = entry, renameNewName = entry.name) }

    /** Updates the rename text field value. */
    fun onRenameNameChange(v: String) = _uiState.update { it.copy(renameNewName = v) }

    /** Dismisses the rename dialog without making changes. */
    fun dismissRename() = _uiState.update { it.copy(renameTarget = null, renameNewName = "") }

    /** Renames [renameTarget] to [SftpBrowserUiState.renameNewName] and refreshes. */
    fun confirmRename() {
        val state = _uiState.value
        val target = state.renameTarget ?: return
        if (state.renameNewName.isBlank()) return
        val newPath = target.path.substringBeforeLast('/') + "/" + state.renameNewName
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { sftpClient.rename(requireClient(), target.path, newPath) }
                .onSuccess { _uiState.update { it.copy(renameTarget = null, renameNewName = "") }; refresh() }
                .onFailure { handleError(it) }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /** Opens the delete-confirmation dialog for [entry]. */
    fun requestDelete(entry: RemoteFileEntry) = _uiState.update { it.copy(deleteTarget = entry) }

    /** Dismisses the delete confirmation dialog. */
    fun dismissDelete() = _uiState.update { it.copy(deleteTarget = null) }

    /** Deletes [deleteTarget] and refreshes the current listing. */
    fun confirmDelete() {
        val target = _uiState.value.deleteTarget ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { sftpClient.delete(requireClient(), target.path) }
                .onSuccess { _uiState.update { it.copy(deleteTarget = null) }; refresh() }
                .onFailure { handleError(it) }
        }
    }

    // ── New directory ─────────────────────────────────────────────────────────

    /** Opens the new-directory dialog. */
    fun requestNewDir() = _uiState.update { it.copy(showNewDirDialog = true, newDirName = "") }

    /** Updates the new-directory name field. */
    fun onNewDirNameChange(v: String) = _uiState.update { it.copy(newDirName = v) }

    /** Dismisses the new-directory dialog. */
    fun dismissNewDir() = _uiState.update { it.copy(showNewDirDialog = false, newDirName = "") }

    /** Creates the new directory and refreshes. */
    fun confirmNewDir() {
        val state = _uiState.value
        if (state.newDirName.isBlank()) return
        val newPath = "${state.currentPath}/${state.newDirName}"
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { sftpClient.mkdir(requireClient(), newPath) }
                .onSuccess { _uiState.update { it.copy(showNewDirDialog = false, newDirName = "") }; refresh() }
                .onFailure { handleError(it) }
        }
    }

    /** Dismisses the current error message. */
    fun onErrorDismissed() = _uiState.update { it.copy(errorMessage = null) }

    /** Changes the sort order for directory listings and re-sorts the current entries. */
    fun setSortOrder(order: SortOrder) {
        _uiState.update { state ->
            state.copy(
                sortOrder = order,
                entries = state.entries.sortedWith(sortComparator(order)),
            )
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCleared() {
        try { sshClient?.close() } catch (_: Exception) {}
        super.onCleared()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun openSshAndNavigate(initialPath: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        runCatching {
            sshClient = sshConnectionManager.connect(
                host = host,
                port = port,
                username = username,
                authType = authType,
                password = password,
                keyPath = keyPath,
                knownHostsManager = knownHostsManager,
            )
        }.onSuccess {
            _uiState.update { it.copy(isConnected = true) }
            navigate(initialPath)
        }.onFailure { handleError(it) }
    }

    private fun requireClient(): SSHClient =
        sshClient ?: error("SSH client is not connected")

    private fun buildBreadcrumbs(path: String): List<String> {
        if (path == "~" || path == "/") return listOf(path)
        return path.split("/").filter { it.isNotBlank() }.let { parts ->
            buildList {
                add("~")
                var accumulated = ""
                parts.drop(1).forEach { part ->
                    accumulated = "$accumulated/$part"
                    add(accumulated)
                }
            }
        }
    }

    private fun handleError(t: Throwable) {
        val message = when (t) {
            is TransferError.AuthFailure -> "Authentication failed: ${t.message}"
            is TransferError.HostUnreachable -> "Host unreachable: ${t.message}"
            is TransferError.PermissionDenied -> "Permission denied: ${t.message}"
            else -> t.message ?: "An unexpected error occurred"
        }
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    companion object {
        /** Returns a comparator that sorts entries by [order], directories always first. */
        internal fun sortComparator(order: SortOrder): Comparator<RemoteFileEntry> =
            when (order) {
                SortOrder.NAME -> compareBy({ !it.isDirectory }, { it.name.lowercase() })
                SortOrder.SIZE -> compareBy({ !it.isDirectory }, { it.sizeBytes })
                SortOrder.DATE -> compareByDescending<RemoteFileEntry> { it.isDirectory }
                    .thenByDescending { it.modifiedAt }
            }
    }
}
