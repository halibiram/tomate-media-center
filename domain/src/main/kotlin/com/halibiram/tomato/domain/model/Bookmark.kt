package com.halibiram.tomato.domain.model

enum class BookmarkMediaType {
    MOVIE,
    SERIES
    // Add TV_SHOW if you differentiate from SERIES, or other types like ANIME
}

data class Bookmark(
    // val id: String, // Database primary key, can be auto-generated. MediaId might serve as unique key for bookmarks.
    // If a separate ID is needed for the bookmark entry itself (e.g. for syncing), it can be added.
    // For now, assuming mediaId + mediaType is the composite key for a bookmark.
    val mediaId: String,
    val mediaType: BookmarkMediaType,
    val title: String?, // Denormalized for UI
    val posterUrl: String?, // Denormalized for UI
    val addedDate: Long // Timestamp
)
// This replaces the Bookmark data class previously defined in BookmarkRepository.kt
// posterUrl was already nullable. Added enum for mediaType. 'id' field commented out for now.
