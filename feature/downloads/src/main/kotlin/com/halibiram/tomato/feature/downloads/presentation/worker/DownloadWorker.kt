package com.halibiram.tomato.feature.downloads.presentation.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
// import com.halibiram.tomato.core.common.error.TomatoException
// import com.halibiram.tomato.core.database.dao.DownloadDao
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.withContext

// DownloadWorker using WorkManager
class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
    // private val downloadDao: DownloadDao, // Injected (e.g. via HiltWorkerFactory)
    // private val ktorClient: io.ktor.client.HttpClient // Injected
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // val mediaUrl = inputData.getString("MEDIA_URL") ?: return Result.failure()
        // val mediaId = inputData.getString("MEDIA_ID") ?: return Result.failure()
        // val title = inputData.getString("TITLE") ?: return Result.failure()

        // return try {
            // Update DB: PENDING
            // Start download with Ktor, update progress to DB
            // Update DB: COMPLETED or FAILED
        //    Result.success()
        // } catch (e: Exception) {
            // Update DB: FAILED
        //    Result.failure()
        // }
        return Result.success() // Placeholder
    }

    companion object {
        const val WORK_NAME = "TomatoDownloadWorker"
        // const val MEDIA_URL_KEY = "MEDIA_URL"
        // const val MEDIA_ID_KEY = "MEDIA_ID"
        // const val TITLE_KEY = "TITLE"
    }
}
