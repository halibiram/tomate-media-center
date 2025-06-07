package com.halibiram.tomato.domain.usecase.download

import android.content.Context
import androidx.work.WorkManager
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PauseDownloadUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadRepository: DownloadRepository
) {
    /**
     * Pauses an ongoing download.
     * This implementation cancels the WorkManager job and updates the DB status to PAUSED.
     * The worker should handle cancellation gracefully and save its state if resumable.
     *
     * @param downloadId The unique ID of the download task to pause.
     * @return Result indicating success or failure of the operation.
     */
    suspend operator fun invoke(downloadId: String): Result<Unit> {
        return try {
            val downloadItem = downloadRepository.getDownload(downloadId)
            if (downloadItem != null &&
                (downloadItem.status == DownloadStatus.DOWNLOADING || downloadItem.status == DownloadStatus.PENDING)) {

                // Cancel the WorkManager work. The worker should detect isStopped.
                WorkManager.getInstance(context).cancelUniqueWork(downloadId)

                // Update status in repository to PAUSED.
                // The worker, upon cancellation, might also try to update its status.
                // Ensure DB updates are consistent.
                downloadRepository.updateDownloadStatus(downloadId, DownloadStatus.PAUSED)

                Result.success(Unit)
            } else {
                Result.failure(Exception("Download not found or not in a pausable state."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
