package com.halibiram.tomato.feature.search.presentation

import com.halibiram.tomato.core.common.result.Result
import com.halibiram.tomato.core.common.result.TomatoException
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.usecase.movie.SearchMoviesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

// Assume MainCoroutineExtension is in a shared test utility module
// For this example, defining it here if not already present globally for the tool
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
class SearchViewModelTest {

    private lateinit var searchMoviesUseCase: SearchMoviesUseCase
    private lateinit var viewModel: SearchViewModel
    private lateinit var testDispatcher: TestDispatcher // To control time for debounce

    @BeforeEach
    fun setUp() {
        // Get the TestDispatcher from the rule, or create one if rule not directly providing it
        // For JUnit 5 extension, we can create one and it's set/reset by the extension.
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher) // Ensure it's set for each test

        searchMoviesUseCase = mockk(relaxed = true) // relaxed = true to avoid mocking all suspend functions
        viewModel = SearchViewModel(searchMoviesUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleMovie(id: String, title: String) = Movie(id, title, "Desc", "url", "date", emptyList(), 0.0)

    @Test
    fun `initial UI state is correct`() {
        val initialState = viewModel.uiState.value
        assertEquals("", viewModel.searchQuery.value)
        assertTrue(initialState.searchResults.isEmpty())
        assertFalse(initialState.isLoading)
        assertNull(initialState.error)
        assertFalse(initialState.noResultsFound)
    }

    @Test
    fun `onSearchQueryChanged updates searchQuery state`() {
        val newQuery = "Inception"
        viewModel.onSearchQueryChanged(newQuery)
        assertEquals(newQuery, viewModel.searchQuery.value)
    }

    @Test
    fun `performSearch is triggered after debounce and query length met`() = runTest(testDispatcher.scheduler) {
        val query = "Matrix"
        val movies = listOf(sampleMovie("1", "The Matrix"))
        coEvery { searchMoviesUseCase(query, 1) } returns flowOf(Result.Success(movies))

        viewModel.onSearchQueryChanged("Ma") // Too short
        advanceTimeBy(600) // Past debounce
        coVerify(exactly = 0) { searchMoviesUseCase(any(), any()) } // Not called for "Ma"

        viewModel.onSearchQueryChanged(query) // Meets length
        var uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading) // Should be loading immediately or after very short delay from onStart

        advanceTimeBy(600) // Past debounce for "Matrix"
        advanceUntilIdle() // Ensure all coroutines complete

        coVerify(exactly = 1) { searchMoviesUseCase(query, 1) }
        uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(movies, uiState.searchResults)
    }

    @Test
    fun `performSearch updates state on Loading from use case`() = runTest(testDispatcher.scheduler) {
        val query = "Tenet"
        coEvery { searchMoviesUseCase(query, 1) } returns flowOf(Result.Loading())

        viewModel.onSearchQueryChanged(query)
        advanceTimeBy(600) // Pass debounce
        advanceUntilIdle()


        val uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)
        assertEquals(query, uiState.searchQuery) // searchQuery should be updated in uiState
    }


    @Test
    fun `performSearch updates state on Success with data`() = runTest(testDispatcher.scheduler) {
        val query = "Dune"
        val movies = listOf(sampleMovie("3", "Dune"))
        coEvery { searchMoviesUseCase(query, 1) } returns flowOf(Result.Success(movies))

        viewModel.onSearchQueryChanged(query)
        advanceTimeBy(600)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(movies, uiState.searchResults)
        assertFalse(uiState.noResultsFound)
        assertNull(uiState.error)
    }

    @Test
    fun `performSearch updates state on Success with empty list`() = runTest(testDispatcher.scheduler) {
        val query = "NonExistentMovie123"
        coEvery { searchMoviesUseCase(query, 1) } returns flowOf(Result.Success(emptyList()))

        viewModel.onSearchQueryChanged(query)
        advanceTimeBy(600)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.searchResults.isEmpty())
        assertTrue(uiState.noResultsFound)
        assertNull(uiState.error)
    }

    @Test
    fun `performSearch updates state on Error`() = runTest(testDispatcher.scheduler) {
        val query = "ErrorQuery"
        val exception = TomatoException("Network Error")
        coEvery { searchMoviesUseCase(query, 1) } returns flowOf(Result.Error(exception))

        viewModel.onSearchQueryChanged(query)
        advanceTimeBy(600)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(exception.message, uiState.error)
        assertTrue(uiState.searchResults.isEmpty())
        assertFalse(uiState.noResultsFound)
    }

    @Test
    fun `blank query clears results and does not trigger search`() = runTest(testDispatcher.scheduler) {
        // First, perform a search to populate results
        val query = "Populate"
        val movies = listOf(sampleMovie("1", "Populated Movie"))
        coEvery { searchMoviesUseCase(query, 1) } returns flowOf(Result.Success(movies))
        viewModel.onSearchQueryChanged(query)
        advanceTimeBy(600)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.searchResults.isEmpty())

        // Now, change query to blank
        viewModel.onSearchQueryChanged("")
        advanceTimeBy(600) // Past debounce
        advanceUntilIdle()

        coVerify(exactly = 1) { searchMoviesUseCase(query, 1) } // Original call
        coVerify(exactly = 0) { searchMoviesUseCase("", 1) }    // No call for blank query

        val uiState = viewModel.uiState.value
        assertTrue(uiState.searchResults.isEmpty())
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        assertFalse(uiState.noResultsFound) // Should not show "no results" for blank query
    }

    @Test
    fun `clearSearchQuery clears query and results`() = runTest(testDispatcher.scheduler) {
        // Populate query and results first
        val query = "ClearMe"
        val movies = listOf(sampleMovie("c1", "Clear Me Movie"))
        coEvery { searchMoviesUseCase(query, 1) } returns flowOf(Result.Success(movies))
        viewModel.onSearchQueryChanged(query)
        advanceTimeBy(600)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.searchResults.isEmpty())
        assertEquals(query, viewModel.searchQuery.value)

        // Action: Clear query
        viewModel.clearSearchQuery()
        advanceUntilIdle() // Process any state updates

        assertEquals("", viewModel.searchQuery.value)
        val uiState = viewModel.uiState.value
        assertTrue(uiState.searchResults.isEmpty())
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        assertFalse(uiState.noResultsFound)
    }

    // TODO: Add tests for recent searches if that logic is more fleshed out in ViewModel
}
