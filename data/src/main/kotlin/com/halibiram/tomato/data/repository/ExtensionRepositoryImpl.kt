package com.halibiram.tomato.data.repository

import com.halibiram.tomato.feature.extensions.api.TomatoExtension
import com.halibiram.tomato.feature.extensions.engine.ExtensionEngine // Placeholder
import com.halibiram.tomato.domain.repository.ExtensionInfo
import com.halibiram.tomato.domain.repository.ExtensionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionRepositoryImpl @Inject constructor(
    private val extensionEngine: ExtensionEngine? // Nullable for placeholder
    // private val extensionMetadataDao: ExtensionMetadataDao // For storing enabled/disabled state, order etc.
) : ExtensionRepository {

    override fun getInstalledExtensionsFlow(): Flow<List<ExtensionInfo>> = flow {
        // Placeholder:
        // 1. Get all raw extensions from ExtensionEngine.
        // 2. Get metadata (like isEnabled, description) from extensionMetadataDao.
        // 3. Combine and map to List<ExtensionInfo>.
        // For now, use the direct (non-flow) version or mock.
        emit(getInstalledExtensions())
    }

    override suspend fun getInstalledExtensions(): List<ExtensionInfo> {
        return extensionEngine?.getAllExtensions()?.map { rawExt ->
            mapToDomain(rawExt, true /* isEnabled placeholder */, "Placeholder description")
        } ?: emptyList()
    }

    override suspend fun getExtensionById(id: String): ExtensionInfo? {
        return extensionEngine?.getExtensionById(id)?.let { rawExt ->
            mapToDomain(rawExt, true, "Placeholder description")
        }
    }

    override suspend fun getRawExtensionById(id: String): TomatoExtension? {
        return extensionEngine?.getExtensionById(id)
    }

    override suspend fun enableExtension(id: String) {
        // extensionEngine?.enableExtension(id)
        // extensionMetadataDao?.updateEnabledState(id, true)
    }

    override suspend fun disableExtension(id: String) {
        // extensionEngine?.disableExtension(id)
        // extensionMetadataDao?.updateEnabledState(id, false)
    }

    override suspend fun installExtension(filePath: String): Result<Unit> {
        // This is complex. Might involve:
        // 1. Copying file to a secure location.
        // 2. Validating the extension package.
        // 3. Telling ExtensionEngine to load it.
        // 4. Storing metadata.
        // extensionEngine?.loadExtensionFromFile(filePath) // Simplified
        return Result.success(Unit) // Placeholder
    }

    override suspend fun uninstallExtension(id: String): Result<Unit> {
        // extensionEngine?.uninstallExtension(id)
        // extensionMetadataDao?.deleteExtension(id)
        // Delete files if applicable
        return Result.success(Unit) // Placeholder
    }

    // --- Mapper ---
    private fun mapToDomain(extension: TomatoExtension, isEnabled: Boolean, description: String?): ExtensionInfo {
        return ExtensionInfo(
            id = extension.id,
            name = extension.name,
            version = extension.version,
            apiVersion = extension.apiVersion,
            description = description ?: "No description provided.", // Could fetch from metadata
            iconUrl = null, // Would come from metadata
            isEnabled = isEnabled, // Would come from metadata (user preference)
            source = "Installed" // Placeholder, could be more specific (APK, file path, etc.)
        )
    }
}
