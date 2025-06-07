package com.halibiram.tomato.domain.usecase.extension

import com.halibiram.tomato.domain.repository.ExtensionRepository
import com.halibiram.tomato.feature.extensions.api.TomatoExtension // Assuming API path
import javax.inject.Inject

class LoadExtensionUseCase @Inject constructor(
    private val extensionRepository: ExtensionRepository
) {
    /**
     * Retrieves a loaded (raw) instance of a specific extension.
     * This is useful for when the system needs to directly interact with the extension's methods.
     * It implies the extension is enabled and its code is loaded into memory.
     *
     * @param extensionId The ID of the extension to load.
     * @return The [TomatoExtension] instance, or null if not found, not enabled, or loading failed.
     */
    suspend operator fun invoke(extensionId: String): TomatoExtension? {
        // Placeholder:
        // 1. Check if extension is known and enabled (via ExtensionRepository/ExtensionInfo).
        // 2. If yes, ask ExtensionRepository (which asks ExtensionEngine) for the loaded instance.
        // The repository/engine would handle the actual class loading if not already done.
        return extensionRepository.getRawExtensionById(extensionId)
    }
}
