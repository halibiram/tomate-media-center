package com.halibiram.tomato.domain.model

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    PAUSED,
    CANCELLED // Added for completeness
}

enum class DownloadMediaType {
    MOVIE,
    SERIES_EPISODE, // More specific than just SERIES
    OTHER
}

data class Download(
    val id: String, // Unique ID for the download entry itself (e.g., UUID)
    val mediaId: String, // ID of the movie or episode
    val mediaType: DownloadMediaType,
    val title: String, // Denormalized for easy display
    val downloadUrl: String, // The URL from which the media is being downloaded (might be temporary)
    val status: DownloadStatus,
    val progress: Int, // Percentage 0-100
    val filePath: String?, // Path to the downloaded file on device, null if not yet completed/started
    val totalSizeBytes: Long = 0, // Total size of the file, known after headers are fetched
    val downloadedSizeBytes: Long = 0, // Bytes downloaded so far
    val addedDate: Long = System.currentTimeMillis(), // Timestamp when download was added
    val posterPath: String? = null // Optional: for UI
)

// This replaces the DownloadItem data class previously defined in DownloadRepository.kt.
// The fields are slightly adjusted for clarity and completeness (e.g., id for the download task itself).
