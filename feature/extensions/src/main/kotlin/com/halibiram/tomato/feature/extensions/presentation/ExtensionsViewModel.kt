package com.halibiram.tomato.feature.extensions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.core.common.result.Result // Import your Result class
import com.halibiram.tomato.domain.model.Extension
import com.halibiram.tomato.domain.usecase.extension.EnableExtensionUseCase
import com.halibiram.tomato.domain.usecase.extension.GetExtensionsUseCase
import com.halibiram.tomato.domain.usecase.extension.InstallExtensionUseCase
import com.halibiram.tomato.domain.usecase.extension.UninstallExtensionUseCase
// Removed direct ExtensionManifest import as it's handled by use case
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExtensionsUiState(
    val isLoading: Boolean = true,
    val extensions: List<Extension> = emptyList(),
    val error: String? = null,
    val infoMessage: String? = null
)

@HiltViewModel
class ExtensionsViewModel @Inject constructor(
    private val getExtensionsUseCase: GetExtensionsUseCase,
    private val installExtensionUseCase: InstallExtensionUseCase, // Updated use case
    private val uninstallExtensionUseCase: UninstallExtensionUseCase,
    private val enableExtensionUseCase: EnableExtensionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExtensionsUiState())
    val uiState: StateFlow<ExtensionsUiState> = _uiState.asStateFlow()

    init {
        loadExtensions()
    }

    fun loadExtensions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, infoMessage = null) }
            getExtensionsUseCase.invoke()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load extensions: ${e.message}") }
                }
                .collectLatest { extensions ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            extensions = extensions.sortedBy { ext -> ext.name.lowercase() }
                        )
                    }
                }
        }
    }

    /**
     * Installs an extension from the given source URI.
     * Manifest is now loaded by the InstallExtensionUseCase.
     */
    fun installExtension(sourceUri: String) {
        viewModelScope.launch {
            // Extract a user-friendly name for messages before result, if possible
            val fileName = sourceUri.substringAfterLast('/')
            _uiState.update { it.copy(isLoading = true, error = null, infoMessage = "Installing $fileName...") }

            val result = installExtensionUseCase(sourceUri = sourceUri, sourceDescription = "From file: $fileName") // Pass URI

            result.fold(
                onSuccess = { installedExtension ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            infoMessage = "Extension installed: ${installedExtension.name} v${installedExtension.version}"
                        )
                    }
                    // The list should refresh automatically due to the Flow collection in init().
                    // If not, uncomment: loadExtensions()
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to install $fileName: ${exception.message}"
                        )
                    }
                }
            )
        }
    }

    fun uninstallExtension(id: String) {
        viewModelScope.launch {
            val extensionName = _uiState.value.extensions.find { it.id == id }?.name ?: id
            _uiState.update { it.copy(isLoading = true, error = null, infoMessage = "Uninstalling $extensionName...") }
            val result = uninstallExtensionUseCase(id)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, infoMessage = "$extensionName uninstalled.") }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to uninstall $extensionName: ${exception.message}") }
                }
            )
        }
    }

    fun toggleExtensionEnabled(id: String, currentIsEnabled: Boolean) {
        viewModelScope.launch {
            val extensionName = _uiState.value.extensions.find { it.id == id }?.name ?: id
            val actionText = if (currentIsEnabled) "Disabling" else "Enabling"
            _uiState.update { it.copy(isLoading = true, error = null, infoMessage = "$actionText $extensionName...") }

            val result = enableExtensionUseCase(id, !currentIsEnabled)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, infoMessage = "$extensionName ${if (!currentIsEnabled) "enabled" else "disabled"}.") }
                },
                onFailure = { exception ->
                     _uiState.update { it.copy(isLoading = false, error = "Failed to $actionText $extensionName: ${exception.message}") }
                }
            )
        }
    }

    fun clearUserMessages() {
        _uiState.update { it.copy(error = null, infoMessage = null) }
    }
}
