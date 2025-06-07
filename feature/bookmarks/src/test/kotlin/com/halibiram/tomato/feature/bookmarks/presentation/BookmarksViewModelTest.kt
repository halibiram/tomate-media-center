package com.halibiram.tomato.feature.bookmarks.presentation

import com.halibiram.tomato.core.common.result.TomatoException
import com.halibiram.tomato.domain.model.Bookmark
import com.halibiram.tomato.domain.model.BookmarkMediaType
import com.halibiram.tomato.domain.usecase.bookmark.GetBookmarksUseCase
import com.halibiram.tomato.domain.usecase.bookmark.RemoveBookmarkUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

// Assume MainCoroutineExtension is in a shared test utility module
@ExperimentalCoroutinesApi
class MainCoroutineExtension(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : org.junit.jupiter.api.extension.BeforeEachCallback, org.junit.jupiter.api.extension.AfterEachCallback {
    override fun beforeEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun afterEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
@ExtendWith(MainCoroutineExtension::class)
class BookmarksViewModelTest {

    private lateinit var getBookmarksUseCase: GetBookmarksUseCase
    private lateinit var removeBookmarkUseCase: RemoveBookmarkUseCase
    private lateinit var viewModel: BookmarksViewModel
    private lateinit var testDispatcher: TestDispatcher

    // To control the flow returned by GetBookmarksUseCase
    private val bookmarksFlow = MutableStateFlow<List<Bookmark>>(emptyList())

    @BeforeEach
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        getBookmarksUseCase = mockk()
        removeBookmarkUseCase = mockk(coJustRun = true) // coJustRun for suspend fun ()

        // Default behavior for getBookmarksUseCase
        every { getBookmarksUseCase.invoke(any()) } returns bookmarksFlow

        viewModel = BookmarksViewModel(getBookmarksUseCase, removeBookmarkUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createSampleBookmark(id: String, type: BookmarkMediaType, title: String = "Sample Bookmark") = Bookmark(
        mediaId = id, mediaType = type, title = title, posterUrl = null, addedDate = System.currentTimeMillis()
    )

    @Test
    fun `initial state is loading then updates with empty list and null filter`() = runTest(testDispatcher.scheduler) {
        // ViewModel's init calls setFilter(null) which triggers the flow.
        // Initially, isLoading should be true.
        var uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)

        // Emit an empty list from the use case's flow
        bookmarksFlow.value = emptyList()
        advanceUntilIdle() // Allow collection to process

        uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.bookmarks.isEmpty())
        assertNull(uiState.filter)
        assertNull(uiState.error)
    }

    @Test
    fun `getBookmarksUseCase emitting data updates bookmarks in UiState`() = runTest(testDispatcher.scheduler) {
        val bookmarks = listOf(createSampleBookmark("1", BookmarkMediaType.MOVIE))

        bookmarksFlow.value = bookmarks // Emit data through the flow
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(bookmarks, uiState.bookmarks)
    }

    @Test
    fun `getBookmarksUseCase emitting error updates error in UiState`() = runTest(testDispatcher.scheduler) {
        val errorMessage = "Failed to fetch bookmarks"
        // Make the use case return an erroring flow for this test
        every { getBookmarksUseCase.invoke(any()) } returns flow { throw TomatoException(errorMessage) }

        // Re-trigger collection by setting filter (ViewModel re-initialization is another option)
        viewModel.setFilter(null)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(errorMessage, uiState.error)
        assertTrue(uiState.bookmarks.isEmpty()) // Assuming bookmarks are cleared on error
    }


    @Test
    fun `removeBookmark calls RemoveBookmarkUseCase`() = runTest(testDispatcher.scheduler) {
        val mediaId = "1"
        val mediaType = BookmarkMediaType.MOVIE

        viewModel.removeBookmark(mediaId, mediaType)
        advanceUntilIdle() // For coroutine in ViewModel to complete

        coVerify { removeBookmarkUseCase(mediaId, mediaType) }
        // Note: List update is tested by observing the flow from GetBookmarksUseCase
    }

    @Test
    fun `removeBookmark failure updates error in UiState`() = runTest(testDispatcher.scheduler) {
        val mediaId = "1"
        val mediaType = BookmarkMediaType.MOVIE
        val errorMessage = "Failed to remove"
        coEvery { removeBookmarkUseCase(mediaId, mediaType) } throws TomatoException(errorMessage)

        viewModel.removeBookmark(mediaId, mediaType)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("Failed to remove bookmark: $errorMessage", uiState.error)
    }


    @Test
    fun `setFilter updates filter in UiState and triggers reload`() = runTest(testDispatcher.scheduler) {
        val newFilter = BookmarkMediaType.SERIES
        val seriesBookmarks = listOf(createSampleBookmark("s1", BookmarkMediaType.SERIES, "Series Title"))

        // Mock the use case to return specific data for the new filter
        every { getBookmarksUseCase.invoke(newFilter) } returns flowOf(seriesBookmarks)

        viewModel.setFilter(newFilter) // This updates filterState and should trigger flatMapLatest

        // First update: isLoading true, filter set
        var uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)
        assertEquals(newFilter, uiState.filter)

        advanceUntilIdle() // Allow flow collection to process the new filter

        // Second update: data loaded, isLoading false
        uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(seriesBookmarks, uiState.bookmarks)
        assertEquals(newFilter, uiState.filter) // Filter should persist
        coVerify { getBookmarksUseCase.invoke(newFilter) } // Verify use case was called with new filter
    }

    @Test
    fun `clearError sets error to null`() {
        // Set an error first
        _uiState.update { it.copy(error = "Some error") }
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }
}
