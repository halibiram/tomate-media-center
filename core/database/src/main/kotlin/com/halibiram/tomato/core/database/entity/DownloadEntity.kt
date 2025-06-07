package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.halibiram.tomato.core.database.converter.DateConverter // Not used if storing Long timestamps

// Using Long for timestamps directly (System.currentTimeMillis())
// Using Long for sizes in bytes for precision. UI layer can convert to MB/GB.

@Entity(tableName = "downloads")
// @TypeConverters(DateConverter::class) // Only if using java.util.Date for timestamps
data class DownloadEntity(
    @PrimaryKey
    val id: String, // Unique ID for the download task (e.g., mediaId or UUID if one media can have multiple download attempts)
    val mediaId: String, // ID of the movie or episode from its source
    val mediaType: String, // Domain model's DownloadMediaType enum name: "MOVIE", "SERIES_EPISODE"
    val title: String,
    val downloadUrl: String, // URL from which media is downloaded
    val filePath: String?, // Path to the downloaded file on device
    val status: String, // Domain model's DownloadStatus enum name: "PENDING", "DOWNLOADING", etc.
    val progress: Int, // Percentage 0-100
    val totalSizeBytes: Long?, // Total size of the file in bytes, nullable if not yet known
    val downloadedSizeBytes: Long?, // Bytes downloaded so far, nullable if not started
    val addedDateTimestamp: Long = System.currentTimeMillis(), // Timestamp when download was added
    val completedDateTimestamp: Long? = null, // Timestamp when download completed
    val posterPath: String? = null, // Optional: for UI

    // Optional fields for richer context, especially for series episodes
    val seriesIdForEpisode: String? = null,
    val seasonNumberForEpisode: Int? = null,
    val episodeNumberForEpisode: Int? = null
) {
    companion object {
        // Status constants mirroring domain enum for DAO queries if needed, though direct enum.name is preferred
        const val STATUS_PENDING = "PENDING"
        const val STATUS_DOWNLOADING = "DOWNLOADING"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_PAUSED = "PAUSED"
        const val STATUS_CANCELLED = "CANCELLED"

        const val TYPE_MOVIE = "MOVIE"
        const val TYPE_SERIES_EPISODE = "SERIES_EPISODE"
    }
}
