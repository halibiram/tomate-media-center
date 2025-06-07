package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val mediaId: String, // Can be movieId or seriesId.
    val mediaType: String, // "movie" or "series" to distinguish
    val title: String?, // Denormalized for quick display
    val posterPath: String?, // Denormalized for quick display
    val bookmarkedDate: Date = Date()
) {
    companion object {
        const val TYPE_MOVIE = "movie"
        const val TYPE_SERIES = "series"
    }
}
