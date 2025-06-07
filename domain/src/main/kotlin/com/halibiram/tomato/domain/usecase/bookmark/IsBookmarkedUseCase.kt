package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.model.BookmarkMediaType // Import enum
import com.halibiram.tomato.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsBookmarkedUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    /**
     * Checks if a specific media item is bookmarked (one-shot).
     * @param mediaId The ID of the media item.
     * @param mediaType The type of the media item.
     * @return True if bookmarked, false otherwise.
     */
    suspend operator fun invoke(mediaId: String, mediaType: BookmarkMediaType): Boolean {
        return bookmarkRepository.isBookmarked(mediaId, mediaType)
    }

    /**
     * Observes the bookmark status of a specific media item.
     * @param mediaId The ID of the media item.
     * @param mediaType The type of the media item.
     * @return A Flow emitting true if bookmarked, false otherwise.
     */
    fun observe(mediaId: String, mediaType: BookmarkMediaType): Flow<Boolean> {
        return bookmarkRepository.isBookmarkedFlow(mediaId, mediaType)
    }
}
