package com.halibiram.tomato.domain.usecase.download

import com.halibiram.tomato.domain.model.Download // Domain model
import com.halibiram.tomato.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDownloadsUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    /**
     * Retrieves a flow of all download items.
     * @return A Flow emitting a list of download items.
     */
    operator fun invoke(): Flow<List<Download>> {
        return downloadRepository.getDownloads()
    }

    /**
     * Retrieves a flow of a specific download item by its task ID.
     * @param downloadId The unique ID of the download task.
     * @return A Flow emitting the download item, or null if not found.
     */
    fun getDownloadById(downloadId: String): Flow<Download?> {
        return downloadRepository.getDownloadFlow(downloadId)
    }

    /**
     * Retrieves a flow of active downloads (PENDING or DOWNLOADING).
     * @return A Flow emitting a list of active download items.
     */
    fun getActiveDownloads(): Flow<List<Download>> {
        return downloadRepository.getActiveDownloadsFlow()
    }
}
