package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.model.Bookmark
import com.halibiram.tomato.domain.model.BookmarkMediaType
import com.halibiram.tomato.domain.repository.BookmarkRepository
import javax.inject.Inject

class AddBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    /**
     * Adds a media item to bookmarks.
     * @param mediaId ID of the media.
     * @param mediaType Type of media (MOVIE or SERIES).
     * @param title Title of the media.
     * @param posterUrl Optional poster path for UI.
     */
    suspend operator fun invoke(
        mediaId: String,
        mediaType: BookmarkMediaType, // Changed from String to Enum
        title: String?, // Made nullable to match domain model
        posterUrl: String?
    ) {
        val bookmark = Bookmark(
            mediaId = mediaId,
            mediaType = mediaType,
            title = title,
            posterUrl = posterUrl,
            addedDate = System.currentTimeMillis() // Generate timestamp here
        )
        bookmarkRepository.addBookmark(bookmark)
    }
}
