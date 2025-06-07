package com.halibiram.tomato.domain.usecase.bookmark

import com.halibiram.tomato.domain.model.Bookmark
import com.halibiram.tomato.domain.model.BookmarkMediaType
import com.halibiram.tomato.domain.repository.BookmarkRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class GetBookmarksUseCaseTest {

    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var getBookmarksUseCase: GetBookmarksUseCase

    @BeforeEach
    fun setUp() {
        bookmarkRepository = mockk()
        getBookmarksUseCase = GetBookmarksUseCase(bookmarkRepository)
    }

    @Test
    fun `invoke with null filter calls repository getBookmarks with null filter`() = runTest {
        val expectedBookmarks = listOf(mockk<Bookmark>())
        every { bookmarkRepository.getBookmarks(null) } returns flowOf(expectedBookmarks)

        val resultFlow = getBookmarksUseCase.invoke(null) // Explicitly pass null
        val actualBookmarks = resultFlow.first()

        verify(exactly = 1) { bookmarkRepository.getBookmarks(null) }
        assertEquals(expectedBookmarks, actualBookmarks)
    }

    @Test
    fun `invoke with default null filter calls repository getBookmarks with null filter`() = runTest {
        val expectedBookmarks = listOf(mockk<Bookmark>())
        every { bookmarkRepository.getBookmarks(null) } returns flowOf(expectedBookmarks)

        val resultFlow = getBookmarksUseCase.invoke() // Default null filter
        val actualBookmarks = resultFlow.first()

        verify(exactly = 1) { bookmarkRepository.getBookmarks(null) }
        assertEquals(expectedBookmarks, actualBookmarks)
    }

    @Test
    fun `invoke with specific filter calls repository getBookmarks with that filter`() = runTest {
        val filter = BookmarkMediaType.MOVIE
        val expectedBookmarks = listOf(mockk<Bookmark>(relaxed = true))
        every { bookmarkRepository.getBookmarks(filter) } returns flowOf(expectedBookmarks)

        val resultFlow = getBookmarksUseCase.invoke(filter)
        val actualBookmarks = resultFlow.first()

        verify(exactly = 1) { bookmarkRepository.getBookmarks(filter) }
        assertEquals(expectedBookmarks, actualBookmarks)
    }

    @Test
    fun `getBookmarkedIdsFlow with null filter calls repository getBookmarkedIdsFlow with null`() = runTest {
        val expectedIds = setOf("id1", "id2")
        every { bookmarkRepository.getBookmarkedIdsFlow(null) } returns flowOf(expectedIds)

        val resultFlow = getBookmarksUseCase.getBookmarkedIdsFlow(null)
        val actualIds = resultFlow.first()

        verify(exactly = 1) { bookmarkRepository.getBookmarkedIdsFlow(null) }
        assertEquals(expectedIds, actualIds)
    }

    @Test
    fun `getBookmarkedIdsFlow with default null filter calls repository getBookmarkedIdsFlow with null`() = runTest {
        val expectedIds = setOf("id1", "id2")
        every { bookmarkRepository.getBookmarkedIdsFlow(null) } returns flowOf(expectedIds)

        val resultFlow = getBookmarksUseCase.getBookmarkedIdsFlow() // Default null filter
        val actualIds = resultFlow.first()

        verify(exactly = 1) { bookmarkRepository.getBookmarkedIdsFlow(null) }
        assertEquals(expectedIds, actualIds)
    }

    @Test
    fun `getBookmarkedIdsFlow with specific filter calls repository getBookmarkedIdsFlow with filter`() = runTest {
        val filter = BookmarkMediaType.SERIES
        val expectedIds = setOf("s1", "s2")
        every { bookmarkRepository.getBookmarkedIdsFlow(filter) } returns flowOf(expectedIds)

        val resultFlow = getBookmarksUseCase.getBookmarkedIdsFlow(filter)
        val actualIds = resultFlow.first()

        verify(exactly = 1) { bookmarkRepository.getBookmarkedIdsFlow(filter) }
        assertEquals(expectedIds, actualIds)
    }
}
