package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.model.BookmarkMediaType
import com.halibiram.tomato.domain.repository.BookmarkRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class IsBookmarkedUseCaseTest {

    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var isBookmarkedUseCase: IsBookmarkedUseCase

    @BeforeEach
    fun setUp() {
        bookmarkRepository = mockk()
        isBookmarkedUseCase = IsBookmarkedUseCase(bookmarkRepository)
    }

    @Test
    fun `invoke (suspend) calls repository isBookmarked and returns its result`() = runTest {
        val mediaId = "series123"
        val mediaType = BookmarkMediaType.SERIES
        val expectedIsBookmarked = true
        coEvery { bookmarkRepository.isBookmarked(mediaId, mediaType) } returns expectedIsBookmarked

        val actualIsBookmarked = isBookmarkedUseCase.invoke(mediaId, mediaType)

        coVerify(exactly = 1) { bookmarkRepository.isBookmarked(mediaId, mediaType) }
        assertEquals(expectedIsBookmarked, actualIsBookmarked)
    }

    @Test
    fun `observe calls repository isBookmarkedFlow and returns its flow`() = runTest {
        val mediaId = "movie456"
        val mediaType = BookmarkMediaType.MOVIE
        val expectedFlow = flowOf(false) // Example flow
        every { bookmarkRepository.isBookmarkedFlow(mediaId, mediaType) } returns expectedFlow

        val actualFlow = isBookmarkedUseCase.observe(mediaId, mediaType)

        assertEquals(expectedFlow.first(), actualFlow.first()) // Compare emitted values
        verify(exactly = 1) { bookmarkRepository.isBookmarkedFlow(mediaId, mediaType) }
    }
}
