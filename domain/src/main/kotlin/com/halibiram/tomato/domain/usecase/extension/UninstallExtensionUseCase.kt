package com.halibiram.tomato.domain.usecase.extension

import com.halibiram.tomato.domain.repository.ExtensionRepository
import javax.inject.Inject

class UninstallExtensionUseCase @Inject constructor(
    private val extensionRepository: ExtensionRepository
) {
    /**
     * Uninstalls an extension.
     * @param extensionId The ID of the extension to uninstall.
     * @return Result indicating success or failure.
     */
    suspend operator fun invoke(extensionId: String): Result<Unit> {
        if (extensionId.isBlank()) {
            return Result.failure(IllegalArgumentException("Extension ID cannot be empty."))
        }
        // Further checks, like if the extension is a system one and cannot be uninstalled,
        // would typically be handled in the repository or engine.
        return extensionRepository.uninstallExtension(extensionId)
    }
}
