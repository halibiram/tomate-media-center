package com.halibiram.tomato.domain.usecase.download

import com.halibiram.tomato.domain.repository.DownloadRepository
// import com.halibiram.tomato.domain.repository.DownloadItem // Assuming DownloadItem is detailed enough for starting a download
// import com.halibiram.tomato.domain.service.DownloadService // Or interact directly with WorkManager via repository/service
import javax.inject.Inject

class DownloadMediaUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
    // private val downloadService: DownloadService // To enqueue in WorkManager
) {
    /**
     * Initiates the download of a media item.
     * @param mediaId The ID of the media (movie or episode).
     * @param mediaType Type of media ("movie" or "episode").
     * @param mediaTitle Title of the media.
     * @param downloadUrl The direct URL to download from.
     * @param posterPath Optional poster path for UI.
     * @return Result indicating success or failure of enqueueing.
     */
    suspend operator fun invoke(
        mediaId: String,
        mediaType: String,
        mediaTitle: String,
        downloadUrl: String, // This needs to be resolved before calling this use case
        posterPath: String?
    ): Result<Unit> {
        // Placeholder logic:
        // 1. Check if already downloaded or downloading (via DownloadRepository).
        // 2. If not, create a DownloadItem domain object.
        // 3. Add it to the repository (which might save to DB and mark as PENDING).
        // 4. Enqueue a DownloadWorker using WorkManager (possibly via a DownloadService or the repository itself).
        //    Pass necessary data like mediaId, downloadUrl, title to the worker.

        // val existingDownload = downloadRepository.getDownloadById(mediaId)
        // if (existingDownload != null &&
        //     (existingDownload.downloadStatus == DownloadStatus.COMPLETED.name ||
        //      existingDownload.downloadStatus == DownloadStatus.DOWNLOADING.name)) {
        //     return Result.failure(Exception("Media is already downloaded or downloading."))
        // }

        // val initialDownloadItem = com.halibiram.tomato.domain.repository.DownloadItem(
        //     mediaId = mediaId,
        //     title = mediaTitle,
        //     posterPath = posterPath,
        //     downloadSizeBytes = 0, // Will be updated by worker
        //     downloadedSizeBytes = 0,
        //     progressPercentage = 0,
        //     downloadStatus = com.halibiram.tomato.domain.repository.DownloadStatus.PENDING.name,
        //     addedDate = java.util.Date(),
        //     mediaType = mediaType,
        //     filePath = null // Will be updated upon completion
        // )
        // downloadRepository.addDownload(initialDownloadItem)

        // downloadService.startDownload(
        //     mediaId = mediaId,
        //     mediaTitle = mediaTitle,
        //     downloadUrl = downloadUrl,
        //     posterPath = posterPath
        // )
        return Result.success(Unit) // Placeholder
    }
}
