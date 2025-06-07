package com.halibiram.tomato.domain.usecase.download

import android.content.Context
import androidx.work.WorkManager
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CancelDownloadUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadRepository: DownloadRepository
) {
    /**
     * Cancels an ongoing or pending download.
     * This involves cancelling the WorkManager job and updating the DB status.
     * The worker itself should detect cancellation and clean up.
     *
     * @param downloadId The unique ID of the download task to cancel.
     * @return Result indicating success or failure.
     */
    suspend operator fun invoke(downloadId: String): Result<Unit> {
        return try {
            val downloadItem = downloadRepository.getDownload(downloadId)
            if (downloadItem != null &&
                (downloadItem.status == DownloadStatus.DOWNLOADING ||
                 downloadItem.status == DownloadStatus.PENDING ||
                 downloadItem.status == DownloadStatus.PAUSED)) { // Can cancel paused downloads too

                WorkManager.getInstance(context).cancelUniqueWork(downloadId)

                // Update status in repository to CANCELLED.
                // The worker, upon observing isStopped, might also try to update its status,
                // so this ensures the final state is CANCELLED.
                // If the worker cleans up and deletes the record, this might not be necessary,
                // but setting to CANCELLED is a good definitive action.
                downloadRepository.updateDownloadStatus(downloadId, DownloadStatus.CANCELLED)
                // Optionally, if cancelled items should be removed from DB immediately after cancellation signal:
                // downloadRepository.removeDownload(downloadId) // This would also delete file if path exists

                Result.success(Unit)
            } else {
                Result.failure(Exception("Download not found or not in a cancellable state."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
