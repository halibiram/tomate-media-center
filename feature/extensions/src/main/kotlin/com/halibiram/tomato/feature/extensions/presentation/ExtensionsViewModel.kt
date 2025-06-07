package com.halibiram.tomato.feature.extensions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.feature.extensions.api.TomatoExtension // For the data class
import com.halibiram.tomato.feature.extensions.engine.ExtensionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// UI representation of an extension
data class UiExtension(
    val id: String,
    val name: String,
    val version: String,
    val apiVersion: Int,
    val description: String? = null, // Could be from metadata
    val iconUrl: String? = null, // Could be from metadata
    val isEnabled: Boolean, // Managed by the host app
    val source: String // e.g., "Installed APK", "External File"
)

data class ExtensionsUiState(
    val extensions: List<UiExtension> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

// @HiltViewModel
class ExtensionsViewModel /*@Inject constructor(
    // private val extensionEngine: ExtensionEngine,
    // private val extensionsRepository: ExtensionsRepository // Or directly use engine
)*/ : ViewModel() {

    private val _uiState = MutableStateFlow(ExtensionsUiState())
    val uiState: StateFlow<ExtensionsUiState> = _uiState

    // In a real app, ExtensionEngine would be injected.
    // For this placeholder, we might simulate it or assume it's passed.
    private var mockEngine: ExtensionEngine? = null // Placeholder for a real engine

    init {
        // If using a real engine, it would be injected.
        // For preview/placeholder:
        // mockEngine = ExtensionEngine(ApplicationProvider.getApplicationContext(), ExtensionLoader(ApplicationProvider.getApplicationContext()), ExtensionSandbox(...))
        loadExtensions()
    }

    fun loadExtensions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // val loadedExtensions = extensionEngine.getAllExtensions() // From real engine
                // val uiExtensions = loadedExtensions.map { mapToUiExtension(it, true) } // Assuming all are enabled initially

                // Simulate fetching extensions
                kotlinx.coroutines.delay(500)
                val simulatedExtensions = listOf(
                    UiExtension("com.example.movieprovider", "Example Movie Provider", "1.0.2", 1, "Provides movies from Example Source", null, true, "Installed"),
                    UiExtension("com.example.seriesprovider", "Example Series Hub", "0.9.0", 1, "Access series content easily", null, true, "Installed"),
                    UiExtension("com.example.disabledext", "Old Extension", "0.5.0", 1, "This one is disabled by user", null, false, "Installed")
                )
                _uiState.value = _uiState.value.copy(isLoading = false, extensions = simulatedExtensions)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load extensions: ${e.message}")
            }
        }
    }

    private fun mapToUiExtension(ext: TomatoExtension, isEnabled: Boolean): UiExtension {
        return UiExtension(
            id = ext.id,
            name = ext.name,
            version = ext.version,
            apiVersion = ext.apiVersion,
            // description and iconUrl would come from metadata associated with the extension
            description = "A sample ${ext.name} extension.",
            isEnabled = isEnabled, // This status would be stored by the host app
            source = "Installed" // Placeholder
        )
    }

    fun toggleExtensionEnabled(extensionId: String, currentIsEnabled: Boolean) {
        viewModelScope.launch {
            try {
                if (currentIsEnabled) {
                    // extensionEngine.disableExtension(extensionId)
                } else {
                    // extensionEngine.enableExtension(extensionId)
                }
                // Refresh the list after toggling
                loadExtensions() // Or update the specific item in the list for better UX
            } catch (e: Exception) {
                // Handle error during enable/disable, maybe show a toast
                _uiState.value = _uiState.value.copy(error = "Failed to toggle extension ${extensionId}: ${e.message}")
            }
        }
    }

    fun uninstallExtension(extensionId: String) {
        viewModelScope.launch {
            try {
                // extensionEngine.uninstallExtension(extensionId)
                // This might involve prompting the user if it's an APK uninstall
                loadExtensions() // Refresh list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to uninstall extension ${extensionId}: ${e.message}")
            }
        }
    }

    fun getExtensionDetails(extensionId: String) {
        // Navigate to a details screen or show a dialog
        // This would fetch more detailed info if available
    }
}
