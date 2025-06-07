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
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.halibiram.tomato.core.player.R // Assuming common R file for drawables
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.repository.DownloadRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.contentLength
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val downloadRepository: DownloadRepository, // Injected repository
    private val httpClient: HttpClient // Injected HttpClient (ensure provided by DI)
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_DOWNLOAD_ID = "downloadId" // Use the unique Download ID
        const val KEY_MEDIA_URL = "mediaUrl"
        const val KEY_MEDIA_TITLE = "mediaTitle"
        // Output file name can be derived from mediaId or title + extension

        const val NOTIFICATION_CHANNEL_ID = "tomato_downloads_channel_worker" // Unique channel ID
        const val NOTIFICATION_CHANNEL_NAME = "Tomato Downloads Worker"
        const val PROGRESS_NOTIFICATION_ID_BASE = 2000 // Base for progress notifications
        const val KEY_PROGRESS = "progress"

        private const val BUFFER_SIZE = 8 * 1024 // 8KB buffer
        private const val UPDATE_THROTTLE_MS = 500L // Update progress every 500ms
    }

    override suspend fun doWork(): Result {
        val downloadId = inputData.getString(KEY_DOWNLOAD_ID) ?: return Result.failure(
            workDataOf("error" to "Download ID missing")
        )
        val mediaUrl = inputData.getString(KEY_MEDIA_URL) ?: return Result.failure(
            workDataOf("error" to "Media URL missing")
        )
        val mediaTitle = inputData.getString(KEY_MEDIA_TITLE) ?: "Downloading..."
        // Example: derive output file name, ensure it's unique and valid
        val outputFileName = "$downloadId.${mediaUrl.substringAfterLast('.', "mp4")}"


        val notificationId = PROGRESS_NOTIFICATION_ID_BASE + Random.nextInt(10000)
        createNotificationChannel()

        // Update DB: Set status to DOWNLOADING
        downloadRepository.updateDownloadStatus(downloadId, DownloadStatus.DOWNLOADING)
        setForeground(createForegroundInfo(mediaTitle, 0, notificationId))

        return try {
            downloadFile(downloadId, mediaUrl, outputFileName, mediaTitle, notificationId)
            Result.success()
        } catch (e: IOException) {
            // Network or file I/O errors
            downloadRepository.updateDownloadState(downloadId, 0, DownloadStatus.FAILED, 0L, null, null, null)
            Result.failure(workDataOf("error" to ("IO Error: " + e.message)))
        } catch (e: CancellationException) { // Specifically handle coroutine cancellation
            downloadRepository.updateDownloadState(downloadId, getCurrentProgress(downloadId), DownloadStatus.PAUSED, null, null, null, null) // Or CANCELLED
            Result.failure(workDataOf("error" to "Download cancelled by user"))
        } catch (e: Exception) {
            // Other errors
            downloadRepository.updateDownloadState(downloadId, 0, DownloadStatus.FAILED, 0L, null, null, null)
            Result.failure(workDataOf("error" to ("General Error: " + e.message)))
        } finally {
             NotificationManagerCompat.from(appContext).cancel(notificationId)
        }
    }

    private suspend fun getCurrentProgress(downloadId: String): Int {
        return downloadRepository.getDownload(downloadId)?.progress ?: 0
    }


    private suspend fun downloadFile(
        downloadId: String,
        mediaUrl: String,
        outputFileName: String,
        mediaTitle: String,
        notificationId: Int
    ) = withContext(Dispatchers.IO) {
        val outputFile = File(appContext.filesDir, "downloads/$outputFileName")
        outputFile.parentFile?.mkdirs() // Ensure "downloads" directory exists

        val response = httpClient.get(mediaUrl)
        val channel: ByteReadChannel = response.body()
        val totalBytes = response.contentLength() ?: -1L // Total size from header

        downloadRepository.updateDownloadState(downloadId, 0, DownloadStatus.DOWNLOADING, 0L, totalBytes, null, null)

        var downloadedBytes = 0L
        var lastUpdateTime = 0L

        FileOutputStream(outputFile).use { outputStream ->
            while (!channel.isClosedForRead) {
                if (isStopped) { // Check for cancellation by WorkManager
                    outputStream.close()
                    outputFile.delete()
                    throw CancellationException("Download stopped by WorkManager")
                }

                val packet = channel.readRemaining(BUFFER_SIZE.toLong())
                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()
                    outputStream.write(bytes)
                    downloadedBytes += bytes.size

                    val currentTime = System.currentTimeMillis()
                    if (totalBytes > 0 && (currentTime - lastUpdateTime > UPDATE_THROTTLE_MS || downloadedBytes == totalBytes)) {
                        val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                        setForeground(createForegroundInfo(mediaTitle, progress, notificationId))
                        setProgress(workDataOf(KEY_PROGRESS to progress, "id" to downloadId))
                        downloadRepository.updateDownloadState(downloadId, progress, DownloadStatus.DOWNLOADING, downloadedBytes, totalBytes, null, null)
                        lastUpdateTime = currentTime
                    }
                }
            }
        }

        if (downloadedBytes == totalBytes || totalBytes == -1L) { // Consider totalBytes == -1L as success if stream ends
            downloadRepository.updateDownloadState(downloadId, 100, DownloadStatus.COMPLETED, downloadedBytes, totalBytes, outputFile.absolutePath, System.currentTimeMillis())
            showFinalNotification(mediaTitle, "Download complete.", notificationId + 1) // Use different ID for completion
        } else {
            // This case might occur if stream ends prematurely and downloadedBytes != totalBytes
            throw IOException("Download incomplete: $downloadedBytes / $totalBytes bytes downloaded.")
        }
    }

    private fun createForegroundInfo(title: String, progress: Int, notificationId: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(if (progress < 100) "Downloading: $progress%" else "Download complete")
            .setSmallIcon(R.drawable.ic_stat_player_notification) // Use existing placeholder
            .setOngoing(true)
            .setOnlyAlertOnce(true) // Don't repeatedly alert for progress updates
            .setProgress(100, progress, progress == -1) // Indeterminate if progress is -1
            .build()
        return ForegroundInfo(notificationId, notification)
    }

    private fun showFinalNotification(title: String, message: String, notificationId: Int) {
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return // Cannot post notification if permission not granted on Android 13+
        }
         val notification = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_stat_player_notification)
            .setAutoCancel(true) // Dismiss on click
            .build()
        NotificationManagerCompat.from(appContext).notify(notificationId, notification)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for ongoing and completed downloads via Worker."
            }
            val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
