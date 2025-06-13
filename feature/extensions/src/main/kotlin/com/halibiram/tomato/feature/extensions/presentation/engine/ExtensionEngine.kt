package com.halibiram.tomato.feature.extensions.presentation.engine

// ExtensionEngine to manage and run extensions
class ExtensionEngine(
    private val loader: ExtensionLoader,
    private val sandbox: ExtensionSandbox
) {
    fun listExtensions() { /* ... */ }
    fun installExtension(sourceUrl: String) { /* ... */ }
    fun uninstallExtension(extensionId: String) { /* ... */ }
    fun runExtension(extensionId: String, action: String, params: Map<String, Any>) { /* ... */ }
}
