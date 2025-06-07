package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// DownloadEntity
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val mediaId: String, // Could be movie or episode ID
    val title: String,
    val downloadUrl: String,
    val filePath: String?,
    val status: Int, // e.g., PENDING, DOWNLOADING, COMPLETED, FAILED
    val progress: Int,
    val mediaType: String // "movie" or "episode"
)
