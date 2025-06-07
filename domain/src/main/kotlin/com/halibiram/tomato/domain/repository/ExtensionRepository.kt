package com.halibiram.tomato.domain.repository

import com.halibiram.tomato.domain.model.Extension
import kotlinx.coroutines.flow.Flow

interface ExtensionRepository {
    fun getInstalledExtensions(): Flow<List<Extension>>
    suspend fun installExtension(sourceUrl: String): Result<Unit> // Result for success/failure feedback
    suspend fun uninstallExtension(extensionId: String): Result<Unit>
    suspend fun enableExtension(extensionId: String)
    suspend fun disableExtension(extensionId: String)
    // May need methods to get extension details, or execute actions via an extension
    // fun <T> executeExtensionAction(extensionId: String, action: String, params: Map<String, Any>): Flow<T>
}
