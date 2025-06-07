package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// BookmarkEntity
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val mediaId: String, // Could be movie, series, or episode ID
    val mediaType: String, // "movie", "series", "episode"
    val bookmarkedAt: Long
)
