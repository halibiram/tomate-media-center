package com.halibiram.tomato.domain.usecase.extension

import com.halibiram.tomato.domain.repository.ExtensionRepository
import javax.inject.Inject

class InstallExtensionUseCase @Inject constructor(
    private val extensionRepository: ExtensionRepository
) {
    /**
     * Installs an extension from a given file path.
     * @param filePath The path to the extension file (e.g., APK or ZIP).
     * @return Result indicating success or failure.
     */
    suspend operator fun invoke(filePath: String): Result<Unit> {
        if (filePath.isBlank()) {
            return Result.failure(IllegalArgumentException("File path cannot be empty."))
        }
        // Further validation of filePath (e.g., file existence, type) can be added here or in repository.
        return extensionRepository.installExtension(filePath)
    }
}
