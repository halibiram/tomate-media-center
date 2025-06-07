package com.halibiram.tomato.domain.usecase.download

import android.content.Context
import androidx.work.WorkManager
import com.halibiram.tomato.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeleteDownloadedFileUseCase @Inject constructor(
    @ApplicationContext private val context: Context, // May not be needed if repo handles file deletion
    private val downloadRepository: DownloadRepository
) {
    /**
     * Deletes a download record from the database and its associated file from disk.
     * Also cancels any ongoing WorkManager job for this download.
     *
     * @param downloadId The unique ID of the download task to delete.
     * @param filePath The path to the downloaded file (can be retrieved from Download item).
     *                 The repository's removeDownload method is now responsible for file deletion.
     * @return Result indicating success or failure.
     */
    suspend operator fun invoke(downloadId: String): Result<Unit> {
        return try {
            // Cancel any ongoing WorkManager job first to prevent it from re-creating files/records
            // after deletion, or writing to a file that's about to be deleted.
            WorkManager.getInstance(context).cancelUniqueWork(downloadId)

            // The repository's removeDownload method should handle both file deletion (if path exists)
            // and DB record removal.
            val success = downloadRepository.removeDownload(downloadId)

            if (success) {
                Result.success(Unit)
            } else {
                // This case might occur if the item was already deleted or some other specific error.
                Result.failure(Exception("Failed to delete download record from database or file."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
