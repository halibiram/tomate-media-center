package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.model.Bookmark
import com.halibiram.tomato.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    operator fun invoke(): Flow<List<Bookmark>> {
        return bookmarkRepository.getBookmarks()
    }
}
