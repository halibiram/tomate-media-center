package com.halibiram.tomato.data.repository

import com.halibiram.tomato.core.database.dao.BookmarkDao
import com.halibiram.tomato.core.database.entity.BookmarkEntity
import com.halibiram.tomato.domain.model.Bookmark // Domain model
import com.halibiram.tomato.domain.model.BookmarkMediaType // Domain enum
import com.halibiram.tomato.domain.repository.BookmarkRepository // Domain interface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {

    override fun getBookmarks(filterType: BookmarkMediaType?): Flow<List<Bookmark>> {
        val entityFlow = when (filterType) {
            null -> bookmarkDao.getAllBookmarks() // Get all if filterType is null
            BookmarkMediaType.MOVIE -> bookmarkDao.getBookmarksByType(BookmarkEntity.TYPE_MOVIE)
            BookmarkMediaType.SERIES -> bookmarkDao.getBookmarksByType(BookmarkEntity.TYPE_SERIES)
            // Add other types if BookmarkMediaType enum expands
        }
        return entityFlow.map { entities ->
            entities.map { mapEntityToDomain(it) }
        }
    }

    override fun getBookmarkedIdsFlow(mediaType: BookmarkMediaType?): Flow<Set<String>> {
        return getBookmarks(mediaType).map { bookmarks ->
            bookmarks.map { it.mediaId }.toSet()
        }
    }

    override suspend fun addBookmark(bookmark: Bookmark) {
        bookmarkDao.insertBookmark(mapDomainToEntity(bookmark))
    }

    override suspend fun removeBookmark(mediaId: String, mediaType: BookmarkMediaType) {
        bookmarkDao.deleteBookmark(mediaId, mediaType.name)
    }

    override suspend fun isBookmarked(mediaId: String, mediaType: BookmarkMediaType): Boolean {
        // Use the suspend DAO function for a one-shot check
        return bookmarkDao.isBookmarked(mediaId, mediaType.name)
    }

    override fun isBookmarkedFlow(mediaId: String, mediaType: BookmarkMediaType): Flow<Boolean> {
        // Use the Flow DAO function for observation
        return bookmarkDao.isBookmarkedFlow(mediaId, mediaType.name)
    }


    override suspend fun clearAllBookmarks() {
        bookmarkDao.clearBookmarks()
    }

    // --- Mappers ---
    private fun mapEntityToDomain(entity: BookmarkEntity): Bookmark {
        return Bookmark(
            mediaId = entity.mediaId,
            mediaType = try { BookmarkMediaType.valueOf(entity.mediaType) } catch (e: IllegalArgumentException) {
                // Handle unknown or legacy types if necessary, or throw error
                // For now, if it's an unknown string, it might be an issue.
                // Depending on strictness, could default or error.
                // Assuming BookmarkMediaType enum covers all valid DB strings.
                BookmarkMediaType.MOVIE // Default or throw error
            },
            title = entity.title,
            posterUrl = entity.posterUrl,
            addedDate = entity.addedDate
        )
    }

    private fun mapDomainToEntity(domain: Bookmark): BookmarkEntity {
        return BookmarkEntity(
            mediaId = domain.mediaId,
            mediaType = domain.mediaType.name, // Store enum name as string
            title = domain.title,
            posterUrl = domain.posterUrl,
            addedDate = domain.addedDate
        )
    }
}
