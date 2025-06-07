package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.model.Bookmark
import com.halibiram.tomato.domain.model.BookmarkMediaType
import com.halibiram.tomato.domain.repository.BookmarkRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class AddBookmarkUseCaseTest {

    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var addBookmarkUseCase: AddBookmarkUseCase

    @BeforeEach
    fun setUp() {
        bookmarkRepository = mockk()
        addBookmarkUseCase = AddBookmarkUseCase(bookmarkRepository)
    }

    @Test
    fun `invoke calls repository addBookmark with correctly constructed Bookmark object`() = runTest {
        val mediaId = "movie123"
        val mediaType = BookmarkMediaType.MOVIE
        val title = "Test Movie Title"
        val posterUrl = "http://example.com/poster.jpg"

        coJustRun { bookmarkRepository.addBookmark(any()) }

        val beforeTimestamp = System.currentTimeMillis()
        addBookmarkUseCase.invoke(mediaId, mediaType, title, posterUrl)
        val afterTimestamp = System.currentTimeMillis()

        val bookmarkSlot = slot<Bookmark>()
        coVerify(exactly = 1) { bookmarkRepository.addBookmark(capture(bookmarkSlot)) }

        val capturedBookmark = bookmarkSlot.captured
        assertEquals(mediaId, capturedBookmark.mediaId)
        assertEquals(mediaType, capturedBookmark.mediaType)
        assertEquals(title, capturedBookmark.title)
        assertEquals(posterUrl, capturedBookmark.posterUrl)
        assertTrue(capturedBookmark.addedDate >= beforeTimestamp && capturedBookmark.addedDate <= afterTimestamp)
    }

    @Test
    fun `invoke with null title and posterUrl calls repository correctly`() = runTest {
        val mediaId = "series456"
        val mediaType = BookmarkMediaType.SERIES

        coJustRun { bookmarkRepository.addBookmark(any()) }

        addBookmarkUseCase.invoke(mediaId, mediaType, null, null)

        val bookmarkSlot = slot<Bookmark>()
        coVerify(exactly = 1) { bookmarkRepository.addBookmark(capture(bookmarkSlot)) }

        val capturedBookmark = bookmarkSlot.captured
        assertEquals(mediaId, capturedBookmark.mediaId)
        assertEquals(mediaType, capturedBookmark.mediaType)
        assertNull(capturedBookmark.title)
        assertNull(capturedBookmark.posterUrl)
    }
}
