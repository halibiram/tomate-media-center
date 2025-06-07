package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String, // Could be movieId or episodeId
    val mediaType: String, // "movie" or "episode"
    val title: String,
    val posterPath: String?,
    val downloadPath: String,
    val downloadStatus: String, // e.g., "PENDING", "DOWNLOADING", "COMPLETED", "FAILED"
    val progress: Int = 0, // 0-100
    val fileSize: Long = 0L,
    val downloadedBytes: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
