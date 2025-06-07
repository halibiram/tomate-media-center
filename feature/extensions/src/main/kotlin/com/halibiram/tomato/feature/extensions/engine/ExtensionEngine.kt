package com.halibiram.tomato.feature.extensions.engine

import android.content.Context
import com.halibiram.tomato.feature.extensions.api.ContentProviderExtension
import com.halibiram.tomato.feature.extensions.api.TomatoExtension
// import kotlinx.coroutines.flow.MutableStateFlow
// import kotlinx.coroutines.flow.StateFlow

/**
 * The core of the extension system.
 * Responsibilities:
 * - Discovery: Finding available extensions (e.g., by scanning installed packages or a designated folder).
 * - Lifecycle Management: Loading, initializing (onEnable), and unloading (onDisable) extensions.
 * - Registry: Keeping track of active extensions and their capabilities.
 * - Execution: Facilitating safe execution of extension methods via the [ExtensionSandbox].
 * - Event Bus (Optional): Allowing extensions to publish/subscribe to events if needed.
 */
class ExtensionEngine(
    private val context: Context,
    private val extensionLoader: ExtensionLoader,
    private val extensionSandbox: ExtensionSandbox
    // private val extensionMetadataDao: ExtensionMetadataDao // For storing info about extensions
) {

    // In-memory registry of active extensions. A persistent store might be needed for installed status, user preferences etc.
    private val _activeExtensions = mutableMapOf<String, TomatoExtension>()
    // private val _extensionsFlow = MutableStateFlow<List<TomatoExtensionInfo>>(emptyList())
    // val extensionsFlow: StateFlow<List<TomatoExtensionInfo>> = _extensionsFlow // UI can observe this

    fun initialize() {
        // Discover and load extensions during startup or on demand
        loadAndRegisterExtensions()
    }

    private fun loadAndRegisterExtensions() {
        val loadedExtensions = extensionLoader.loadInstalledExtensions() // This is simplified
        for (ext in loadedExtensions) {
            // Before registering, initialize it within the sandbox (conceptual)
            // extensionSandbox.initializeExtensionInSandbox(ext)
            // Call onEnable safely
            // extensionSandbox.executeSafely(ext) { onEnable(context) }

            // For this placeholder, we directly add, assuming loader gives ready instances
            _activeExtensions[ext.id] = ext
            // ext.onEnable(context) // In a real system, this would be sandboxed.
        }
        // Update _extensionsFlow with metadata of loaded extensions
        // updateExtensionsFlow()
    }

    fun getExtensionById(id: String): TomatoExtension? {
        return _activeExtensions[id]
    }

    fun getAllExtensions(): List<TomatoExtension> {
        return _activeExtensions.values.toList()
    }

    /**
     * Gets all extensions that implement a specific API, e.g., ContentProviderExtension.
     */
    inline fun <reified T : TomatoExtension> getContentProviders(): List<T> {
        return _activeExtensions.values.filterIsInstance<T>()
    }


    // Example of how an extension method might be called through the engine:
    suspend fun <R> callContentProviderFunction(
        extensionId: String,
        action: suspend ContentProviderExtension.() -> R
    ): Result<R> {
        val extension = getExtensionById(extensionId) as? ContentProviderExtension
        return if (extension != null) {
            extensionSandbox.executeSafely(extension, action)
        } else {
            Result.failure(IllegalArgumentException("ContentProviderExtension with ID $extensionId not found or not active."))
        }
    }

    fun enableExtension(extensionId: String) {
        // Logic to enable an extension:
        // 1. Load it if not already loaded (e.g., from a disabled state).
        // 2. Call its onEnable method (sandboxed).
        // 3. Update its status in metadata store and _extensionsFlow.
    }

    fun disableExtension(extensionId: String) {
        // Logic to disable an extension:
        // 1. Call its onDisable method (sandboxed).
        // 2. Unload its classes/resources if possible (complex).
        // 3. Update its status.
        _activeExtensions[extensionId]?.let {
             // extensionSandbox.executeSafely(it) { onDisable() } // Conceptual
             // it.onDisable() // Direct call for placeholder
            _activeExtensions.remove(extensionId)
            // updateExtensionsFlow()
        }
    }

    fun uninstallExtension(extensionId: String) {
        // Logic for uninstalling (e.g., removing its APK or files, then disabling).
        // This is typically handled by the Android Package Manager for APK-based extensions.
        // The engine would then react to the package removal broadcast.
        disableExtension(extensionId)
        // Remove from metadata store.
    }

    // private fun updateExtensionsFlow() {
    //     _extensionsFlow.value = _activeExtensions.values.map {
    //         TomatoExtensionInfo(it.id, it.name, it.version, true /*isActive*/, /* other metadata */)
    //     }
    // }

    // data class TomatoExtensionInfo(
    //     val id: String,
    //     val name: String,
    //     val version: String,
    //     val isActive: Boolean,
    //     val iconUrl: String? = null // etc.
    // )
}
