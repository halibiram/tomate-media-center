package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val mediaId: String, // Can be movieId or episodeId. Need a way to distinguish type.
    val mediaType: String, // "movie" or "episode"
    val title: String?, // Store title for easier display
    val posterPath: String?, // Store poster for easier display
    val downloadPath: String, // Path to the downloaded file on device
    val downloadSizeBytes: Long,
    val downloadedSizeBytes: Long,
    val downloadStatus: String, // e.g., PENDING, DOWNLOADING, COMPLETED, FAILED, PAUSED
    val downloadSpeedBps: Long = 0, // Bytes per second
    val progressPercentage: Int = 0,
    val addedDate: Date = Date(),
    val completedDate: Date? = null,
    val seriesId: String? = null, // Optional: if it's an episode, link to series for grouping
    val seasonNumber: Int? = null, // Optional: for episodes
    val episodeNumber: Int? = null // Optional: for episodes
) {
    companion object {
        const val TYPE_MOVIE = "movie"
        const val TYPE_EPISODE = "episode"

        const val STATUS_PENDING = "PENDING"
        const val STATUS_DOWNLOADING = "DOWNLOADING"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_PAUSED = "PAUSED"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}
