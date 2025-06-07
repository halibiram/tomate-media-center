package com.halibiram.tomato.domain.usecase.download

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.repository.DownloadRepository
import com.halibiram.tomato.feature.downloads.worker.DownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ResumeDownloadUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadRepository: DownloadRepository
) {
    /**
     * Resumes a paused or attempts to retry a failed download.
     * This typically involves re-enqueueing the DownloadWorker.
     * @param download The Download domain model object to resume/retry.
     * @return The ID of the download task, or null if it cannot be resumed/retried.
     */
    suspend operator fun invoke(download: Download): String? {
        if (download.status != DownloadStatus.PAUSED && download.status != DownloadStatus.FAILED) {
            // Log.w("ResumeDownloadUseCase", "Download item ${download.id} is not in a resumable state (${download.status}).")
            return null
        }

        // Update status to PENDING before re-enqueueing (optional, worker might do this)
        downloadRepository.updateDownloadStatus(download.id, DownloadStatus.PENDING)

        val workData = workDataOf(
            DownloadWorker.KEY_DOWNLOAD_ID to download.id,
            DownloadWorker.KEY_MEDIA_URL to download.downloadUrl, // Assuming original URL is stored
            DownloadWorker.KEY_MEDIA_TITLE to download.title
            // Other necessary data like posterPath if worker uses it for notifications
        )

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workData)
            // Constraints can be re-applied or adjusted
            .setConstraints(DownloadMediaUseCase.buildConstraints()) // Assuming a helper for constraints
            .addTag(download.id)
            .build()

        // Re-enqueue with REPLACE policy to ensure the new worker runs.
        WorkManager.getInstance(context).enqueueUniqueWork(
            download.id, // Use the existing download ID as unique work name
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        return download.id
    }
}

// Helper for constraints, can be moved to a common place or kept in DownloadMediaUseCase
internal fun DownloadMediaUseCase.Companion.buildConstraints(): androidx.work.Constraints =
    androidx.work.Constraints.Builder()
        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
        .build()

// Companion object in DownloadMediaUseCase to host buildConstraints
// Add this to DownloadMediaUseCase.kt if not already present:
// class DownloadMediaUseCase @Inject constructor(...) {
//     companion object {
//         internal fun buildConstraints(): Constraints = ...
//     }
//     ...
// }
