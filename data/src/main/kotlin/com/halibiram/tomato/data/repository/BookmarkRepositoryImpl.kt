package com.halibiram.tomato.data.repository

import com.halibiram.tomato.core.database.dao.BookmarkDao // Placeholder
import com.halibiram.tomato.core.database.entity.BookmarkEntity
import com.halibiram.tomato.domain.repository.Bookmark
import com.halibiram.tomato.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao? // Nullable for placeholder
) : BookmarkRepository {

    override fun getBookmarksFlow(filterType: String): Flow<List<Bookmark>> {
        val flow = when (filterType.uppercase()) {
            "ALL" -> bookmarkDao?.getAllBookmarks()
            BookmarkEntity.TYPE_MOVIE.uppercase() -> bookmarkDao?.getBookmarksByType(BookmarkEntity.TYPE_MOVIE)
            BookmarkEntity.TYPE_SERIES.uppercase() -> bookmarkDao?.getBookmarksByType(BookmarkEntity.TYPE_SERIES)
            else -> bookmarkDao?.getAllBookmarks() // Default to all
        }
        return flow?.map { entities ->
            entities.map { entity -> mapToDomain(entity) }
        } ?: flow { emit(emptyList()) }
    }

    override fun isBookmarkedFlow(mediaId: String): Flow<Boolean> {
        return bookmarkDao?.isBookmarked(mediaId) ?: flow { emit(false) }
    }

    override suspend fun addBookmark(mediaId: String, mediaType: String, title: String?, posterPath: String?) {
        val entity = BookmarkEntity(
            mediaId = mediaId,
            mediaType = mediaType,
            title = title,
            posterPath = posterPath,
            bookmarkedDate = Date()
        )
        // bookmarkDao?.insertBookmark(entity)
    }

    override suspend fun removeBookmark(mediaId: String) {
        // bookmarkDao?.deleteBookmarkById(mediaId)
    }

    override suspend fun clearAllBookmarks() {
        // bookmarkDao?.deleteAllBookmarks()
    }

    // --- Mapper ---
    private fun mapToDomain(entity: BookmarkEntity): Bookmark {
        return Bookmark(
            mediaId = entity.mediaId,
            mediaType = entity.mediaType,
            title = entity.title,
            posterPath = entity.posterPath,
            bookmarkedDate = entity.bookmarkedDate
        )
    }
}
