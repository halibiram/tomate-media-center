package com.halibiram.tomato.domain.usecase.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.model.DownloadMediaType
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.repository.DownloadRepository
import com.halibiram.tomato.feature.downloads.worker.DownloadWorker // Correct path to worker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID // For generating a unique ID for the download task
import javax.inject.Inject

class DownloadMediaUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadRepository: DownloadRepository
) {
    companion object {
        // Helper for constraints, can be moved to a common place
        internal fun buildConstraints(): Constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                // .setRequiresStorageNotLow(true) // Example
                .build()
    }

    /**
     * Initiates the download of a media item.
     * @param mediaId The ID of the media (movie or episode).
     * @param mediaType The type of media.
     * @param mediaUrl The direct URL to download from.
     * @param title Title of the media.
     * @param posterPath Optional poster path for UI.
     * @return The ID of the created download task, or null if already downloading/downloaded.
     */
    suspend operator fun invoke(
        mediaId: String, // ID of the content (e.g., movie ID from TMDB)
        mediaType: DownloadMediaType,
        mediaUrl: String,
        title: String,
        posterPath: String?
    ): String? {
        // Check if this specific mediaId is already being downloaded or completed
        // This check might be more complex (e.g., check by mediaId AND mediaType)
        val existingDownloads = downloadRepository.getDownloads().first() // Get current list
        val alreadyExists = existingDownloads.any {
            it.mediaId == mediaId &&
            (it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PENDING || it.status == DownloadStatus.COMPLETED)
        }
        if (alreadyExists) {
            // Optionally, if it's completed and user wants to re-download, handle that.
            // Or if it failed, allow retry. For now, just prevent duplicate active/completed.
            // Log.i("DownloadMediaUseCase", "Download for mediaId $mediaId already exists and is active or completed.")
            return null // Indicate that download was not enqueued because it exists
        }


        val downloadTaskId = UUID.randomUUID().toString() // Generate a unique ID for this download task

        val downloadDomainModel = Download(
            id = downloadTaskId,
            mediaId = mediaId,
            mediaType = mediaType,
            title = title,
            downloadUrl = mediaUrl, // Store original URL for potential retry logic
            status = DownloadStatus.PENDING,
            progress = 0,
            filePath = null,
            totalSizeBytes = 0L, // Will be updated by worker
            downloadedSizeBytes = 0L,
            addedDate = System.currentTimeMillis(),
            posterPath = posterPath
        )
        downloadRepository.addDownload(downloadDomainModel)

        val workData = workDataOf(
            DownloadWorker.KEY_DOWNLOAD_ID to downloadTaskId,
            DownloadWorker.KEY_MEDIA_URL to mediaUrl,
            DownloadWorker.KEY_MEDIA_TITLE to title
            // Optionally pass posterPath, mediaType etc. if worker needs them for notifications directly
        )

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(buildConstraints()) // Use helper for constraints
            .setInputData(workData)
            .setConstraints(constraints)
            .addTag(downloadTaskId) // Tag work with its ID for easier management/observation
            .addTag("media_download_work") // General tag
            .build()

        // Use downloadTaskId as unique work name to prevent multiple workers for the same task
        WorkManager.getInstance(context).enqueueUniqueWork(
            downloadTaskId, // Unique work name
            ExistingWorkPolicy.REPLACE, // Or KEEP, if a PENDING task should not be replaced
            workRequest
        )

        return downloadTaskId
    }
}
