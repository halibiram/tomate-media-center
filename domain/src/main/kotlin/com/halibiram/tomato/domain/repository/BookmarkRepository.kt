package com.halibiram.tomato.domain.repository

import com.halibiram.tomato.domain.model.Bookmark
import com.halibiram.tomato.domain.model.MediaType
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun getBookmarks(): Flow<List<Bookmark>>
    suspend fun addBookmark(mediaId: String, mediaType: MediaType)
    suspend fun removeBookmark(mediaId: String)
    fun isBookmarked(mediaId: String): Flow<Boolean>
}
