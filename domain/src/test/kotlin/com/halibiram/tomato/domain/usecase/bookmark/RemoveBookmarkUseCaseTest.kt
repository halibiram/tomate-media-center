package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.model.BookmarkMediaType
import com.halibiram.tomato.domain.repository.BookmarkRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class RemoveBookmarkUseCaseTest {

    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var removeBookmarkUseCase: RemoveBookmarkUseCase

    @BeforeEach
    fun setUp() {
        bookmarkRepository = mockk()
        removeBookmarkUseCase = RemoveBookmarkUseCase(bookmarkRepository)
    }

    @Test
    fun `invoke calls repository removeBookmark with correct parameters`() = runTest {
        val mediaId = "movie789"
        val mediaType = BookmarkMediaType.MOVIE

        coJustRun { bookmarkRepository.removeBookmark(mediaId, mediaType) }

        removeBookmarkUseCase.invoke(mediaId, mediaType)

        coVerify(exactly = 1) { bookmarkRepository.removeBookmark(mediaId, mediaType) }
    }
}
