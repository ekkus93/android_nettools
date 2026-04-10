package dev.nettools.android.ui.curl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nettools.android.data.curl.CurlRuntimeMetadataProvider
import dev.nettools.android.data.curl.CurlRuntimeMetadataResult
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
    val bundledRuntimeError: String? = null,
)

/**
 * ViewModel for curl settings UI.
 */
@HiltViewModel
class CurlSettingsViewModel @Inject constructor(
    private val settingsRepository: CurlSettingsRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val runtimeMetadataProvider: CurlRuntimeMetadataProvider,
    private val clearCurlLogs: ClearCurlLogsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurlSettingsUiState())
    val uiState: StateFlow<CurlSettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { current ->
            when (val metadataResult = runtimeMetadataProvider.getRuntimeMetadata()) {
                is CurlRuntimeMetadataResult.Available -> current.copy(
                    bundledCurlVersion = metadataResult.metadata.bundledCurlVersion,
                    bundledProtocols = metadataResult.metadata.supportedProtocols,
                    bundledFeatures = metadataResult.metadata.supportedFeatures,
                    http2Supported = metadataResult.metadata.http2Supported,
                    bundledRuntimeError = null,
                )

                is CurlRuntimeMetadataResult.Unavailable -> current.copy(
                    bundledRuntimeError = metadataResult.message,
                )
            }
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
