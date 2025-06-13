package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String, // Could be movieId or seriesId
    val mediaType: String, // "movie" or "series"
    val title: String,
    val posterPath: String?,
    val bookmarkedAt: Long = System.currentTimeMillis()
)
