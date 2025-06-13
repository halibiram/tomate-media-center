package com.halibiram.tomato.domain.usecase.download

import com.halibiram.tomato.domain.repository.DownloadRepository
import javax.inject.Inject

class PauseDownloadUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    suspend operator fun invoke(mediaId: String) {
        downloadRepository.pauseDownload(mediaId)
    }
}
