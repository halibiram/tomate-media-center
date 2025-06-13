package com.halibiram.tomato.domain.usecase.extension

import com.halibiram.tomato.domain.model.Extension // Domain model
import com.halibiram.tomato.domain.repository.ExtensionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExtensionsUseCase @Inject constructor(
    private val extensionRepository: ExtensionRepository
) {
    /**
     * Retrieves a flow of all installed extensions' information.
     * @return A Flow emitting a list of [Extension] objects.
     */
    operator fun invoke(): Flow<List<Extension>> { // Changed from ExtensionInfo
        return extensionRepository.getExtensions()
    }

    /**
     * Retrieves a flow of currently enabled extensions' information.
     * @return A Flow emitting a list of enabled [Extension] objects.
     */
    fun getEnabledExtensions(): Flow<List<Extension>> {
        return extensionRepository.getEnabledExtensions()
    }

     /**
     * Retrieves information for a single extension by its ID.
     * @param id The ID of the extension.
     * @return [Extension] or null if not found.
     */
    suspend fun getExtensionDetails(id: String): Extension? { // Changed from ExtensionInfo
        return extensionRepository.getExtension(id)
    }

    /**
     * Retrieves a flow of information for a single extension by its ID.
     * @param id The ID of the extension.
     * @return A Flow emitting [Extension] or null if not found.
     */
    fun observeExtensionDetails(id: String): Flow<Extension?> {
        return extensionRepository.getExtensionFlow(id)
    }
}
