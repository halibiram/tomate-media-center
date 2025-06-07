package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.repository.BookmarkRepository
import javax.inject.Inject

class AddBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    /**
     * Adds a media item to bookmarks.
     * @param mediaId ID of the media.
     * @param mediaType Type of media ("movie" or "series").
     * @param title Title of the media.
     * @param posterPath Optional poster path for UI.
     */
    suspend operator fun invoke(
        mediaId: String,
        mediaType: String,
        title: String?,
        posterPath: String?
    ) {
        // In a real app, you might fetch title/poster if not provided, or ensure they are non-null.
        bookmarkRepository.addBookmark(mediaId, mediaType, title, posterPath)
    }
}
