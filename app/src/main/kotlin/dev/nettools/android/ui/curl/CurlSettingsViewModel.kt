package dev.nettools.android.ui.curl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.data.curl.NativeCurlBridge
import dev.nettools.android.domain.model.CurlSettings
import dev.nettools.android.domain.repository.CurlSettingsRepository
import dev.nettools.android.domain.repository.WorkspaceRepository
import dev.nettools.android.domain.usecase.curl.ClearCurlLogsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for curl settings.
 */
data class CurlSettingsUiState(
    val settings: CurlSettings = CurlSettings(),
    val effectiveWorkspaceRoot: String = "",
    val bundledCurlVersion: String = "",
    val bundledProtocols: List<String> = emptyList(),
    val bundledFeatures: List<String> = emptyList(),
    val http2Supported: Boolean = false,
)

/**
 * ViewModel for curl settings UI.
 */
@HiltViewModel
class CurlSettingsViewModel @Inject constructor(
    private val settingsRepository: CurlSettingsRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val nativeCurlBridge: NativeCurlBridge,
    private val clearCurlLogs: ClearCurlLogsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurlSettingsUiState())
    val uiState: StateFlow<CurlSettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                bundledCurlVersion = nativeCurlBridge.getBundledCurlVersion(),
                bundledProtocols = nativeCurlBridge.getSupportedProtocols(),
                bundledFeatures = nativeCurlBridge.getSupportedFeatures(),
                http2Supported = nativeCurlBridge.isHttp2Supported(),
            )
        }
        viewModelScope.launch {
            settingsRepository.observeSettings().collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        settings = settings,
                        effectiveWorkspaceRoot = workspaceRepository.getWorkspaceRootPath(),
                    )
                }
            }
        }
    }

    /** Enables or disables persistent curl logging. */
    fun setLoggingEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setLoggingEnabled(enabled) }
    }

    /** Enables or disables saved command history. */
    fun setSaveHistoryEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSaveHistoryEnabled(enabled) }
    }

    /** Clears all persisted curl logs and retained output. */
    fun clearLogs() {
        viewModelScope.launch { clearCurlLogs() }
    }
}
