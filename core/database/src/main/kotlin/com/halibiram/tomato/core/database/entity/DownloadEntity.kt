package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.halibiram.tomato.core.database.converter.DateConverter
import java.util.Date // Make sure this is java.util.Date if DateConverter expects it

// Re-aligning with domain model Download.kt which has DownloadStatus and DownloadMediaType enums.
// For Room, enums are often stored as Strings or Ints. Let's use String here.

@Entity(tableName = "downloads")
@TypeConverters(DateConverter::class) // For addedDate and completedDate
data class DownloadEntity(
    @PrimaryKey
    val id: String, // Unique ID for the download task itself
    val mediaId: String, // ID of the movie or episode
    val mediaType: String, // Store enum name: "MOVIE", "SERIES_EPISODE"
    val title: String,
    val downloadUrl: String, // URL from which media is downloaded (can be temporary)
    val status: String, // Store enum name: "PENDING", "DOWNLOADING", etc.
    val progress: Int, // Percentage 0-100
    val filePath: String?, // Path to the downloaded file
    val totalSizeBytes: Long = 0,
    val downloadedSizeBytes: Long = 0,
    val addedDate: Long = System.currentTimeMillis(), // Using Long for timestamp directly
    val posterPath: String? = null,

    // Fields from old DownloadEntity that might be useful if not flattening too much:
    val downloadSpeedBps: Long = 0, // Bytes per second - maybe track this dynamically, not store
    val completedDate: Long? = null, // Timestamp
    val seriesIdForEpisode: String? = null, // If mediaType is SERIES_EPISODE, this can be useful
    val seasonNumberForEpisode: Int? = null,
    val episodeNumberForEpisode: Int? = null
) {
    // Companion object with status constants was in old entity,
    // now these would map to the domain layer DownloadStatus enum values.
    // For direct usage in DAO queries if needed:
    companion object {
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
