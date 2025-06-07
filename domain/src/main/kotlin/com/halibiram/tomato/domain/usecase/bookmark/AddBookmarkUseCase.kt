package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.model.MediaType
import com.halibiram.tomato.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AddBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(mediaId: String, mediaType: MediaType) {
        val isCurrentlyBookmarked = bookmarkRepository.isBookmarked(mediaId).firstOrNull() ?: false
        if (isCurrentlyBookmarked) {
            bookmarkRepository.removeBookmark(mediaId)
        } else {
            bookmarkRepository.addBookmark(mediaId, mediaType)
        }
    }
}
