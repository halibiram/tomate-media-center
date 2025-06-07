package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.repository.Bookmark
import com.halibiram.tomato.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    /**
     * Retrieves a flow of bookmark items based on a type filter.
     * @param filterType The type to filter by (e.g., "ALL", "MOVIE", "SERIES").
     * @return A Flow emitting a list of bookmark items.
     */
    operator fun invoke(filterType: String = "ALL"): Flow<List<Bookmark>> {
        return bookmarkRepository.getBookmarksFlow(filterType)
    }

    /**
     * Checks if a specific item is bookmarked.
     * @param mediaId The ID of the media item.
     * @return A Flow emitting true if bookmarked, false otherwise.
     */
    fun isBookmarked(mediaId: String): Flow<Boolean> {
        return bookmarkRepository.isBookmarkedFlow(mediaId)
    }
}
