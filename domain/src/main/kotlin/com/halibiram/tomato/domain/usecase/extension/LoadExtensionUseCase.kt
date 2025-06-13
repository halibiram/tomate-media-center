package com.halibiram.tomato.domain.usecase.extension

// This use case might be more complex, depending on what "loading" an extension means.
// If it means preparing it for execution, or fetching its details, the repository interface would need to reflect that.
// For now, let's assume it's about ensuring an extension is ready or fetching its details.

import com.halibiram.tomato.domain.model.Extension
import com.halibiram.tomato.domain.repository.ExtensionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class LoadExtensionUseCase @Inject constructor(
    private val extensionRepository: ExtensionRepository
) {
    // Example: Get details of a specific (already installed) extension
    operator fun invoke(extensionId: String): Flow<Extension?> {
        return extensionRepository.getInstalledExtensions().mapNotNull { extensions ->
            extensions.find { it.id == extensionId }
        }
    }
}
