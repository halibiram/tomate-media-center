package com.halibiram.tomato.domain.repository

import kotlinx.coroutines.flow.Flow
import java.util.Date

// Placeholder data model (could mirror DownloadEntity or be a domain-specific version)
data class DownloadItem(
    val mediaId: String,
    val title: String?,
    val posterPath: String?,
    val downloadSizeBytes: Long,
    val downloadedSizeBytes: Long,
    val progressPercentage: Int,
    val downloadStatus: String, // Use constants e.g., DownloadStatus.DOWNLOADING
    val addedDate: Date,
    val mediaType: String, // "movie" or "episode"
    val filePath: String? = null // Path to the downloaded file on device
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED, PAUSED, CANCELLED
}

interface DownloadRepository {
    fun getDownloadsByStatusFlow(statusFilter: String): Flow<List<DownloadItem>>
    suspend fun getDownloadById(mediaId: String): DownloadItem?
    suspend fun addDownload(item: DownloadItem) // Item might be more detailed with URL etc.
    suspend fun updateDownloadStatus(mediaId: String, newStatus: String, downloadedBytes: Long? = null, progress: Int? = null)
    suspend fun updateDownloadSize(mediaId: String, totalSizeBytes: Long)
    suspend fun deleteDownload(mediaId: String): Boolean // Return true if successful
    suspend fun clearAllDownloads()
    // Add methods for pausing, resuming if these are managed at repository level
}
