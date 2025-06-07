package com.halibiram.tomato.domain.usecase.download

import com.halibiram.tomato.domain.repository.DownloadItem
import com.halibiram.tomato.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDownloadsUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    /**
     * Retrieves a flow of download items based on a status filter.
     * @param statusFilter The status to filter by (e.g., "ALL", "DOWNLOADING", "COMPLETED").
     * @return A Flow emitting a list of download items.
     */
    operator fun invoke(statusFilter: String = "ALL"): Flow<List<DownloadItem>> {
        return downloadRepository.getDownloadsByStatusFlow(statusFilter)
    }
}
