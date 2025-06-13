package com.halibiram.tomato.data.repository

import com.halibiram.tomato.core.database.dao.ExtensionDao
import com.halibiram.tomato.core.database.entity.ExtensionEntity
// import com.halibiram.tomato.feature.extensions.api.TomatoExtension // Raw extension instance, used by domain repo
// import com.halibiram.tomato.feature.extensions.engine.ExtensionEngine // For loading/managing raw extensions
import com.halibiram.tomato.domain.model.Extension // Domain model
import com.halibiram.tomato.domain.repository.ExtensionRepository // Domain interface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionRepositoryImpl @Inject constructor(
    private val extensionDao: ExtensionDao
    // private val extensionEngine: ExtensionEngine // Will be needed for getRawExtensionById and actual install/uninstall logic
) : ExtensionRepository {

    override fun getExtensions(): Flow<List<Extension>> { // Renamed from getInstalledExtensionsFlow
        return extensionDao.getAllExtensions().map { entities ->
            entities.map { mapEntityToDomain(it) }
        }
    }

    override fun getEnabledExtensions(): Flow<List<Extension>> {
        return extensionDao.getEnabledExtensions().map { entities ->
            entities.map { mapEntityToDomain(it) }
        }
    }

    override suspend fun getExtension(id: String): Extension? { // Renamed from getExtensionById
        return extensionDao.getExtensionById(id)?.let { mapEntityToDomain(it) }
    }

    override fun getExtensionFlow(id: String): Flow<Extension?> {
        return extensionDao.getExtensionByIdFlow(id).map { entity ->
            entity?.let { mapEntityToDomain(it) }
        }
    }

    // getRawExtensionById would require ExtensionEngine to load and return the actual instance.
    // For now, it's not implemented here as Engine is not fully integrated.
    // override suspend fun getRawExtensionById(id: String): TomatoExtension? {
    //     val extensionInfo = getExtension(id)
    //     return if (extensionInfo != null && extensionInfo.isEnabled) {
    //         // extensionEngine.getLoadedExtensionInstance(id) // Conceptual
    //         null // Placeholder
    //     } else null
    // }

    override suspend fun installExtension(extension: Extension, sourceUri: String?) {
        // This simplified version assumes 'extension' domain model is pre-populated with manifest data.
        // A real install would parse the sourceUri to get this data.
        val entity = mapDomainToEntity(extension).copy(
            sourceUri = sourceUri, // Store where it came from
            installedAt = System.currentTimeMillis() // Set install time
        )
        extensionDao.insertExtension(entity)
        // Actual file operations or APK parsing would be handled by ExtensionEngine or a dedicated installer service.
    }

    override suspend fun uninstallExtension(id: String): Result<Unit> {
        // TODO: Add logic to delete files associated with the extension (e.g., its APK, data)
        // This might be handled by an ExtensionEngine or a dedicated uninstaller service.
        // For now, just remove from DB.
        val extensionEntity = extensionDao.getExtensionById(id)
        if (extensionEntity == null) {
            return Result.failure(Exception("Extension with ID $id not found."))
        }
        extensionDao.deleteExtensionById(id)
        // extensionEntity.sourceUri?.let { uri -> /* code to delete file from uri */ }
        // extensionEntity.iconPath?.let { path -> /* code to delete file from path */ }
        return Result.success(Unit)
    }

    override suspend fun enableExtension(id: String, enable: Boolean) {
        extensionDao.setEnabled(id, enable)
        // If enabling, ExtensionEngine might need to load/initialize the extension.
        // If disabling, ExtensionEngine might need to unload/de-initialize.
    }

    override suspend fun updateExtensionLoadingError(id: String, error: String?) {
        extensionDao.updateLoadingError(id, error)
    }

    // --- Mappers ---
    private fun mapEntityToDomain(entity: ExtensionEntity): Extension {
        return Extension(
            id = entity.id,
            name = entity.name,
            version = entity.version,
            author = entity.author,
            description = entity.description,
            sourceUrl = entity.sourceUri, // sourceUrl in domain model maps to sourceUri in entity
            iconUrl = entity.iconPath,
            isEnabled = entity.isEnabled,
            apiVersion = entity.apiVersion,
            className = entity.className,
            packageName = entity.packageName,
            source = entity.sourceDescription ?: "Unknown Source",
            loadingError = entity.loadingError // Map loadingError
        )
    }

    private fun mapDomainToEntity(domain: Extension): ExtensionEntity {
        return ExtensionEntity(
            id = domain.id,
            name = domain.name,
            version = domain.version,
            author = domain.author ?: "Unknown Author",
            description = domain.description,
            sourceUri = domain.sourceUrl,
            iconPath = domain.iconUrl,
            isEnabled = domain.isEnabled,
            installedAt = System.currentTimeMillis(), // This should ideally be set only on first install
            apiVersion = domain.apiVersion,
            className = domain.className,
            packageName = domain.packageName,
            sourceDescription = domain.source,
            loadingError = domain.loadingError // Map loadingError
        )
    }
}
