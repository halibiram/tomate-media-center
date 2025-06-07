package com.halibiram.tomato.feature.downloads.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
// import com.halibiram.tomato.core.database.dao.DownloadDao // Assuming Hilt for DI here is tricky for Workers
// import com.halibiram.tomato.core.database.entity.DownloadEntity
// import dagger.assisted.Assisted // For Hilt assisted injection
// import dagger.assisted.AssistedInject // For Hilt assisted injection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

// @HiltWorker // If using Hilt for worker injection
class DownloadWorker /*@AssistedInject*/ constructor(
    // @Assisted
    private val appContext: Context,
    // @Assisted
    private val workerParams: WorkerParameters,
    // private val downloadDao: DownloadDao // Injected via Hilt or passed if not using Hilt for Worker
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_MEDIA_ID = "mediaId"
        const val KEY_MEDIA_TITLE = "mediaTitle"
        const val KEY_DOWNLOAD_URL = "downloadUrl"
        const val KEY_OUTPUT_FILE_NAME = "outputFileName" // e.g., "movie_123.mp4"

        const val NOTIFICATION_CHANNEL_ID = "tomato_downloads_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Tomato Downloads"
        const val PROGRESS_NOTIFICATION_ID_BASE = 1000 // Base for progress notifications
        const val KEY_PROGRESS = "progress"
    }

    override suspend fun doWork(): Result {
        val mediaId = inputData.getString(KEY_MEDIA_ID) ?: return Result.failure()
        val mediaTitle = inputData.getString(KEY_MEDIA_TITLE) ?: "Downloading..."
        val downloadUrlString = inputData.getString(KEY_DOWNLOAD_URL) ?: return Result.failure()
        val outputFileName = inputData.getString(KEY_OUTPUT_FILE_NAME) ?: "$mediaId.mp4"

        val notificationId = PROGRESS_NOTIFICATION_ID_BASE + Random.nextInt(10000) // Unique enough for concurrent downloads
        createNotificationChannel()

        // Initial status update (if using DAO directly, otherwise through a repository)
        // downloadDao.updateDownloadStatus(mediaId, DownloadEntity.STATUS_DOWNLOADING)

        try {
            val url = URL(downloadUrlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                // downloadDao.updateDownloadStatus(mediaId, DownloadEntity.STATUS_FAILED, "Server error: ${connection.responseCode}")
                return Result.failure(workDataOf("error" to "Server error: ${connection.responseCode}"))
            }

            val fileSize = connection.contentLengthLong
            // downloadDao.updateDownloadSize(mediaId, fileSize)

            val outputFile = File(appContext.getExternalFilesDir("downloads"), outputFileName)
            // Ensure parent directory exists
            outputFile.parentFile?.mkdirs()

            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null
            var totalBytesDownloaded: Long = 0

            withContext(Dispatchers.IO) {
                inputStream = connection.inputStream
                outputStream = FileOutputStream(outputFile)
                val buffer = ByteArray(4 * 1024) // 4KB buffer
                var bytesRead: Int

                while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                    if (isStopped) { // Check if worker is cancelled
                        // downloadDao.updateDownloadStatus(mediaId, DownloadEntity.STATUS_CANCELLED, "Download Cancelled by user")
                        outputFile.delete() // Clean up partial file
                        throw CancellationException("Download Cancelled by user")
                    }

                    outputStream!!.write(buffer, 0, bytesRead)
                    totalBytesDownloaded += bytesRead
                    val progress = ((totalBytesDownloaded * 100) / fileSize).toInt()

                    // Update progress in DB
                    // downloadDao.updateDownloadProgress(mediaId, progress, totalBytesDownloaded)

                    // Update notification
                    val foregroundInfo = createForegroundInfo(mediaTitle, progress, notificationId)
                    setForeground(foregroundInfo)
                    setProgress(workDataOf(KEY_PROGRESS to progress, "mediaId" to mediaId))

                    delay(10) // Small delay to allow UI updates and prevent tight loop if download is too fast
                }
            }
            // downloadDao.updateDownloadStatus(mediaId, DownloadEntity.STATUS_COMPLETED)
            showCompletionNotification(mediaTitle, notificationId + 10000) // Use a different ID for completion
            return Result.success(workDataOf("output_path" to outputFile.absolutePath))

        } catch (e: Exception) {
            // downloadDao.updateDownloadStatus(mediaId, DownloadEntity.STATUS_FAILED, e.message)
            return Result.failure(workDataOf("error" to e.message))
        } finally {
            withContext(Dispatchers.IO) {
                inputStream?.close()
                outputStream?.close()
            }
            // Remove progress notification if it wasn't replaced by completion/failure one
            NotificationManagerCompat.from(appContext).cancel(notificationId)
        }
    }

    private fun createForegroundInfo(title: String, progress: Int, notificationId: Int): ForegroundInfo {
        val intent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
        // val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE) // Add appropriate PendingIntent

        val notification = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Downloading: $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download) // Replace with your app's icon
            .setOngoing(true)
            .setProgress(100, progress, false)
            // .setContentIntent(pendingIntent) // To open app on click
            .build()
        return ForegroundInfo(notificationId, notification)
    }
     private fun showCompletionNotification(title: String, notificationId: Int) {
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Cannot post notification
            return
        }
        val notification = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Download complete.")
            .setSmallIcon(android.R.drawable.stat_sys_download_done) // Replace with your app's icon
            .setAutoCancel(true)
            // .setContentIntent(pendingIntent) // To open app or downloaded file list
            .build()
        NotificationManagerCompat.from(appContext).notify(notificationId, notification)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Use LOW to avoid sound for progress
            ).apply {
                description = "Notifications for ongoing and completed downloads"
            }
            val notificationManager: NotificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // CancellationException to be thrown when the work is stopped.
    class CancellationException(message: String) : Exception(message)
}
