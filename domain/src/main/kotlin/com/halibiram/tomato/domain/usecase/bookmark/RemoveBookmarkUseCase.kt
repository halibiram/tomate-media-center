package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.model.BookmarkMediaType // Import enum
import com.halibiram.tomato.domain.repository.BookmarkRepository
import javax.inject.Inject

class RemoveBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    /**
     * Removes a media item from bookmarks.
     * @param mediaId The ID of the media to remove.
     * @param mediaType The type of the media to remove.
     */
    suspend operator fun invoke(mediaId: String, mediaType: BookmarkMediaType) { // Added mediaType
        bookmarkRepository.removeBookmark(mediaId, mediaType)
    }
}
