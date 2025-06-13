package com.halibiram.tomato.domain.usecase.extension

import com.halibiram.tomato.domain.repository.ExtensionRepository
import javax.inject.Inject

class InstallExtensionUseCase @Inject constructor(
    private val extensionRepository: ExtensionRepository
) {
    suspend operator fun invoke(sourceUrl: String): Result<Unit> {
        return extensionRepository.installExtension(sourceUrl)
    }
}
