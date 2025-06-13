package com.halibiram.tomato.domain.usecase.extension

import com.halibiram.tomato.domain.repository.ExtensionRepository
import javax.inject.Inject

class EnableExtensionUseCase @Inject constructor(
    private val extensionRepository: ExtensionRepository
) {
    /**
     * Enables or disables an installed extension.
     *
     * @param id The unique ID of the extension.
     * @param enable True to enable the extension, false to disable it.
     * @return Result indicating success or failure of the operation.
     */
    suspend operator fun invoke(id: String, enable: Boolean): Result<Unit> {
        if (id.isBlank()) {
            return Result.failure(IllegalArgumentException("Extension ID cannot be empty."))
        }
        return try {
            extensionRepository.enableExtension(id, enable)
            Result.success(Unit)
        } catch (e: Exception) {
            // Catch exceptions from repository layer (e.g., DB error)
            Result.failure(e)
        }
    }
}
