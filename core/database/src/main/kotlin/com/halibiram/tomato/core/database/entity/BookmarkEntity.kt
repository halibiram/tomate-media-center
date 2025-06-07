package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
// import androidx.room.PrimaryKey // mediaId + mediaType could be composite primary key
import androidx.room.TypeConverters
import com.halibiram.tomato.core.database.converter.DateConverter // Not used if addedDate is Long
import java.util.Date // Not used if addedDate is Long

// Aligning with domain model Bookmark.kt
// Using mediaId as primary key for simplicity, assuming mediaId is unique across types for bookmarking purposes
// or that the combination of (mediaId, mediaType) would be unique if enforced elsewhere or by composite PK.
// For a simple PK on just mediaId, if you bookmark a movie and series with same ID "123", that's an issue.
// Let's use a composite primary key.

@Entity(
    tableName = "bookmarks",
    primaryKeys = ["mediaId", "mediaType"] // Composite primary key
)
// No need for DateConverter if addedDate is Long
// @TypeConverters(DateConverter::class)
data class BookmarkEntity(
    val mediaId: String,
    val mediaType: String, // Store enum name: "MOVIE", "SERIES"
    val title: String?,
    val posterUrl: String?, // 'posterPath' previously
    val addedDate: Long // Timestamp
) {
    // Companion object with type constants was in old entity,
    // now these would map to the domain layer BookmarkMediaType enum values.
    companion object {
        const val TYPE_MOVIE = "MOVIE"
        const val TYPE_SERIES = "SERIES"
    }
}
