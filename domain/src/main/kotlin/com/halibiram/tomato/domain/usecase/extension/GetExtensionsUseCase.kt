package com.halibiram.tomato.domain.usecase.extension

import com.halibiram.tomato.domain.model.Extension
import com.halibiram.tomato.domain.repository.ExtensionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExtensionsUseCase @Inject constructor(
    private val extensionRepository: ExtensionRepository
) {
    operator fun invoke(): Flow<List<Extension>> {
        return extensionRepository.getInstalledExtensions()
    }
}
