package com.halibiram.tomato.domain.model

/**
 * Represents the metadata and state of an extension as understood by the domain/application layer.
 * This is distinct from the `TomatoExtension` API interface which defines the extension's behavior.
 */
data class Extension(
    val id: String, // Unique identifier, often package name for APK-based extensions
    val name: String,
    val packageName: String, // Specific to Android if it's an APK extension
    val version: String,
    val sourceUrl: String?, // URL from where it was downloaded or its repository (if applicable)
    val description: String?,
    var isEnabled: Boolean, // Managed by the host application
    val iconUrl: String? = null, // URL or path to an icon for the extension
    val apiVersion: Int, // API version the extension targets
    val author: String? = null, // Optional: author of the extension
    val source: String // e.g., "Installed APK", "External File", "Built-in"
)
// This replaces the ExtensionInfo data class previously defined in ExtensionRepository.kt
// It adds more fields like packageName, iconUrl, apiVersion, author, and source for better representation.
