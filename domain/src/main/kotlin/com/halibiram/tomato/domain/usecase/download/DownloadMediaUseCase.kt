package com.halibiram.tomato.domain.usecase.download

import com.halibiram.tomato.domain.repository.DownloadRepository
import javax.inject.Inject

class DownloadMediaUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    suspend operator fun invoke(mediaId: String, mediaType: String, title: String, downloadUrl: String) {
        // Logic to check if already downloaded, etc. might go here or in repository
        downloadRepository.addDownload(mediaId, mediaType, title, downloadUrl)
    }
}
