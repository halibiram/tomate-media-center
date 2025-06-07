package com.halibiram.tomato.domain.model

data class Download(
    val mediaId: String,
    val title: String,
    val downloadUrl: String, // May or may not be exposed directly in domain
    val filePath: String?,
    val status: DownloadStatus,
    val progress: Int, // Percentage 0-100
    val mediaType: MediaType // MOVIE, EPISODE
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class MediaType {
    MOVIE,
    SERIES, // For the whole series (e.g. a .zip) - less common for streaming apps
    EPISODE
}
