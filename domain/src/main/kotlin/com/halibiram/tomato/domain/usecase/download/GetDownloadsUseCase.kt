package com.halibiram.tomato.domain.usecase.download

import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDownloadsUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    operator fun invoke(): Flow<List<Download>> {
        return downloadRepository.getDownloads()
    }
}
