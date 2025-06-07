package com.halibiram.tomato.data.repository

import com.halibiram.tomato.core.database.dao.BookmarkDao
import com.halibiram.tomato.core.database.entity.BookmarkEntity
import com.halibiram.tomato.domain.model.Bookmark
import com.halibiram.tomato.domain.model.BookmarkMediaType
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class BookmarkRepositoryImplTest {

    private lateinit var mockBookmarkDao: BookmarkDao
    private lateinit var bookmarkRepository: BookmarkRepositoryImpl

    @BeforeEach
    fun setUp() {
        mockBookmarkDao = mockk(relaxed = true) // Relaxed for coJustRun on suspend fun
        bookmarkRepository = BookmarkRepositoryImpl(mockBookmarkDao)
    }

    private fun createSampleDomainBookmark(mediaId: String, mediaType: BookmarkMediaType) = Bookmark(
        mediaId = mediaId, mediaType = mediaType, title = "Title $mediaId",
        posterUrl = "/poster/$mediaId.jpg", addedDate = System.currentTimeMillis()
    )

    private fun createSampleEntityBookmark(mediaId: String, mediaType: String) = BookmarkEntity(
        mediaId = mediaId, mediaType = mediaType, title = "Title $mediaId",
        posterUrl = "/poster/$mediaId.jpg", addedDate = System.currentTimeMillis()
    )

    @Test
    fun `getBookmarks with null filter calls DAO getAllBookmarks and maps correctly`() = runTest {
        val entity1 = createSampleEntityBookmark("m1", BookmarkMediaType.MOVIE.name)
        val entity2 = createSampleEntityBookmark("s1", BookmarkMediaType.SERIES.name)
        every { mockBookmarkDao.getAllBookmarks() } returns flowOf(listOf(entity1, entity2))

        val result = bookmarkRepository.getBookmarks(null).first()

        assertEquals(2, result.size)
        assertEquals(entity1.mediaId, result[0].mediaId)
        assertEquals(BookmarkMediaType.MOVIE, result[0].mediaType)
        assertEquals(entity2.mediaId, result[1].mediaId)
        assertEquals(BookmarkMediaType.SERIES, result[1].mediaType)
        verify(exactly = 1) { mockBookmarkDao.getAllBookmarks() }
    }

    @Test
    fun `getBookmarks with MOVIE filter calls DAO getBookmarksByType and maps correctly`() = runTest {
        val entityMovie = createSampleEntityBookmark("m1", BookmarkMediaType.MOVIE.name)
        every { mockBookmarkDao.getBookmarksByType(BookmarkEntity.TYPE_MOVIE) } returns flowOf(listOf(entityMovie))

        val result = bookmarkRepository.getBookmarks(BookmarkMediaType.MOVIE).first()

        assertEquals(1, result.size)
        assertEquals(entityMovie.mediaId, result[0].mediaId)
        assertEquals(BookmarkMediaType.MOVIE, result[0].mediaType)
        verify(exactly = 1) { mockBookmarkDao.getBookmarksByType(BookmarkEntity.TYPE_MOVIE) }
    }

    @Test
    fun `getBookmarkedIdsFlow maps correctly`() = runTest {
        val entity1 = createSampleEntityBookmark("id1", BookmarkMediaType.MOVIE.name)
        val entity2 = createSampleEntityBookmark("id2", BookmarkMediaType.SERIES.name)
        every { mockBookmarkDao.getAllBookmarks() } returns flowOf(listOf(entity1, entity2))

        val result = bookmarkRepository.getBookmarkedIdsFlow(null).first()

        assertEquals(setOf("id1", "id2"), result)
    }


    @Test
    fun `addBookmark maps domain to entity and calls DAO insert`() = runTest {
        val domainBookmark = createSampleDomainBookmark("new1", BookmarkMediaType.MOVIE)
        coJustRun { mockBookmarkDao.insertBookmark(any()) }

        bookmarkRepository.addBookmark(domainBookmark)

        val entitySlot = slot<BookmarkEntity>()
        coVerify { mockBookmarkDao.insertBookmark(capture(entitySlot)) }
        assertEquals(domainBookmark.mediaId, entitySlot.captured.mediaId)
        assertEquals(domainBookmark.mediaType.name, entitySlot.captured.mediaType)
        assertEquals(domainBookmark.title, entitySlot.captured.title)
    }

    @Test
    fun `removeBookmark calls DAO deleteBookmark with correct parameters`() = runTest {
        val mediaId = "del1"
        val mediaType = BookmarkMediaType.SERIES
        coJustRun { mockBookmarkDao.deleteBookmark(mediaId, mediaType.name) }

        bookmarkRepository.removeBookmark(mediaId, mediaType)

        coVerify { mockBookmarkDao.deleteBookmark(mediaId, mediaType.name) }
    }

    @Test
    fun `isBookmarked calls DAO isBookmarked and returns result`() = runTest {
        val mediaId = "check1"
        val mediaType = BookmarkMediaType.MOVIE
        coEvery { mockBookmarkDao.isBookmarked(mediaId, mediaType.name) } returns true

        val result = bookmarkRepository.isBookmarked(mediaId, mediaType)

        assertTrue(result)
        coVerify { mockBookmarkDao.isBookmarked(mediaId, mediaType.name) }
    }

    @Test
    fun `isBookmarkedFlow calls DAO isBookmarkedFlow and returns result`() = runTest {
        val mediaId = "checkFlow1"
        val mediaType = BookmarkMediaType.MOVIE
        every { mockBookmarkDao.isBookmarkedFlow(mediaId, mediaType.name) } returns flowOf(true)

        val result = bookmarkRepository.isBookmarkedFlow(mediaId, mediaType).first()

        assertTrue(result)
        verify { mockBookmarkDao.isBookmarkedFlow(mediaId, mediaType.name) }
    }


    @Test
    fun `clearAllBookmarks calls DAO clearBookmarks`() = runTest {
        coJustRun { mockBookmarkDao.clearBookmarks() }
        bookmarkRepository.clearAllBookmarks()
        coVerify { mockBookmarkDao.clearBookmarks() }
    }
}
