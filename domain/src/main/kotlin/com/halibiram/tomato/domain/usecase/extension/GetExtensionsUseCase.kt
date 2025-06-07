package com.halibiram.tomato.domain.usecase.extension

import com.halibiram.tomato.domain.repository.ExtensionInfo
import com.halibiram.tomato.domain.repository.ExtensionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExtensionsUseCase @Inject constructor(
    private val extensionRepository: ExtensionRepository
) {
    /**
     * Retrieves a flow of installed extensions' information.
     * This is typically used by the UI to display a list of extensions and their status.
     * @return A Flow emitting a list of [ExtensionInfo] objects.
     */
    operator fun invoke(): Flow<List<ExtensionInfo>> {
        return extensionRepository.getInstalledExtensionsFlow()
    }

    /**
     * Retrieves a non-flow list of installed extensions' information.
     * Useful for one-time fetches.
     */
    suspend fun getCurrentExtensions(): List<ExtensionInfo> {
        return extensionRepository.getInstalledExtensions()
    }

     /**
     * Retrieves information for a single extension.
     * @param id The ID of the extension.
     * @return [ExtensionInfo] or null if not found.
     */
    suspend fun getExtensionDetails(id: String): ExtensionInfo? {
        return extensionRepository.getExtensionById(id)
    }
}
