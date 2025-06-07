package com.halibiram.tomato.domain.repository

import com.halibiram.tomato.feature.extensions.api.TomatoExtension // Assuming API is in feature, might move to domain/core
import kotlinx.coroutines.flow.Flow

// Placeholder data model for UI/Domain layer representation of an extension
data class ExtensionInfo(
    val id: String,
    val name: String,
    val version: String,
    val apiVersion: Int,
    val description: String?,
    val iconUrl: String?,
    var isEnabled: Boolean, // Host app manages this state
    val source: String // e.g., "Installed APK", "External File"
)

interface ExtensionRepository {
    fun getInstalledExtensionsFlow(): Flow<List<ExtensionInfo>>
    suspend fun getInstalledExtensions(): List<ExtensionInfo>
    suspend fun getExtensionById(id: String): ExtensionInfo?
    suspend fun getRawExtensionById(id: String): TomatoExtension? // To get the actual extension instance

    suspend fun enableExtension(id: String)
    suspend fun disableExtension(id: String)

    // Installation/Uninstallation might be more complex, involving file operations or Package Manager
    suspend fun installExtension(filePath: String): Result<Unit> // Result might indicate success/failure/reason
    suspend fun uninstallExtension(id: String): Result<Unit>

    // May also include methods to fetch extension sources or browse a repository of extensions
}
