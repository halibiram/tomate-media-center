package com.halibiram.tomato.domain.repository

import kotlinx.coroutines.flow.Flow
import java.util.Date

// Placeholder data model
data class Bookmark(
    val mediaId: String,
    val mediaType: String, // "movie" or "series"
    val title: String?,
    val posterPath: String?,
    val bookmarkedDate: Date
)

interface BookmarkRepository {
    fun getBookmarksFlow(filterType: String): Flow<List<Bookmark>> // "ALL", "MOVIE", "SERIES"
    fun isBookmarkedFlow(mediaId: String): Flow<Boolean>
    suspend fun addBookmark(mediaId: String, mediaType: String, title: String?, posterPath: String?)
    suspend fun removeBookmark(mediaId: String)
    suspend fun clearAllBookmarks()
}
