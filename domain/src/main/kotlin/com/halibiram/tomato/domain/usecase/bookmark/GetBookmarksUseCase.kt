package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.model.Bookmark
import com.halibiram.tomato.domain.model.BookmarkMediaType // Import enum
import com.halibiram.tomato.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    /**
     * Retrieves a flow of bookmark items, optionally filtered by type.
     * @param filterType The type to filter by (e.g., MOVIE, SERIES), or null for all.
     * @return A Flow emitting a list of bookmark items.
     */
    operator fun invoke(filterType: BookmarkMediaType? = null): Flow<List<Bookmark>> {
        return bookmarkRepository.getBookmarks(filterType)
    }

    /**
     * Retrieves a flow of bookmarked media IDs, optionally filtered by type.
     * @param filterType The type to filter by, or null for all.
     * @return A Flow emitting a set of bookmarked media IDs.
     */
    fun getBookmarkedIdsFlow(filterType: BookmarkMediaType? = null): Flow<Set<String>> {
        return bookmarkRepository.getBookmarkedIdsFlow(filterType)
    }
}
