package com.halibiram.tomato.feature.home.presentation

import com.halibiram.tomato.core.common.result.Result
import com.halibiram.tomato.core.common.result.TomatoException
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.usecase.movie.GetMoviesUseCase
import com.halibiram.tomato.domain.usecase.movie.MovieListType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


// MainCoroutineExtension for JUnit 5
@ExperimentalCoroutinesApi
class MainCoroutineExtension(
    val testDispatcher: TestDispatcher = StandardTestDispatcher() // Use StandardTestDispatcher for more control
) : org.junit.jupiter.api.extension.BeforeEachCallback, org.junit.jupiter.api.extension.AfterEachCallback {
    override fun beforeEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun afterEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
@ExtendWith(MainCoroutineExtension::class) // Apply the extension for JUnit 5
class HomeViewModelTest {

    private lateinit var getMoviesUseCase: GetMoviesUseCase
    private lateinit var viewModel: HomeViewModel
    private lateinit var testDispatcher: TestDispatcher

    @BeforeEach
    fun setUp() {
        testDispatcher = StandardTestDispatcher() // Get dispatcher from rule for advancing time
        Dispatchers.setMain(testDispatcher) // Set main dispatcher for tests
        getMoviesUseCase = mockk()
        viewModel = HomeViewModel(getMoviesUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain() // Reset main dispatcher after tests
    }

    private fun sampleMovie(id: String, title: String) = Movie(id, title, "Desc", "url", "date", emptyList(), 0.0)

    @Test
    fun `initial UI state is correct`() {
        val initialState = viewModel.uiState.value
        assertTrue(initialState.isLoadingPopular) // Init calls fetchPopularMovies which sets loading
        assertTrue(initialState.isLoadingTrending) // Init calls fetchTrendingMovies
        assertEquals(emptyList<Movie>(), initialState.popularMovies)
        assertEquals(emptyList<Movie>(), initialState.trendingMovies)
        assertNull(initialState.errorPopular)
        assertNull(initialState.errorTrending)
    }

    @Test
    fun `fetchPopularMovies updates state on Loading`() = runTest(testDispatcher.scheduler) {
        coEvery { getMoviesUseCase(type = MovieListType.POPULAR, page = 1) } returns flowOf(Result.Loading())

        viewModel.fetchPopularMovies()
        advanceUntilIdle() // Ensure coroutines launched by fetchPopularMovies complete

        val uiState = viewModel.uiState.value
        assertTrue(uiState.isLoadingPopular)
        assertNull(uiState.errorPopular)
    }

    @Test
    fun `fetchPopularMovies updates state on Success`() = runTest(testDispatcher.scheduler) {
        val movies = listOf(sampleMovie("1", "Popular Movie 1"))
        coEvery { getMoviesUseCase(type = MovieListType.POPULAR, page = 1) } returns flowOf(Result.Success(movies))

        viewModel.fetchPopularMovies()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoadingPopular)
        assertEquals(movies, uiState.popularMovies)
        assertNull(uiState.errorPopular)
    }

    @Test
    fun `fetchPopularMovies updates state on Error`() = runTest(testDispatcher.scheduler) {
        val exception = TomatoException("Network Error Popular")
        coEvery { getMoviesUseCase(type = MovieListType.POPULAR, page = 1) } returns flowOf(Result.Error(exception))

        viewModel.fetchPopularMovies()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoadingPopular)
        assertEquals(exception.message, uiState.errorPopular)
        assertTrue(uiState.popularMovies.isEmpty())
    }

    @Test
    fun `fetchTrendingMovies updates state on Loading`() = runTest(testDispatcher.scheduler) {
        coEvery { getMoviesUseCase(type = MovieListType.TRENDING, timeWindow = "day") } returns flowOf(Result.Loading())

        viewModel.fetchTrendingMovies()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.isLoadingTrending)
        assertNull(uiState.errorTrending)
    }

    @Test
    fun `fetchTrendingMovies updates state on Success`() = runTest(testDispatcher.scheduler) {
        val movies = listOf(sampleMovie("2", "Trending Movie 1"))
        coEvery { getMoviesUseCase(type = MovieListType.TRENDING, timeWindow = "day") } returns flowOf(Result.Success(movies))

        viewModel.fetchTrendingMovies()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoadingTrending)
        assertEquals(movies, uiState.trendingMovies)
        assertNull(uiState.errorTrending)
    }

    @Test
    fun `fetchTrendingMovies updates state on Error`() = runTest(testDispatcher.scheduler) {
        val exception = TomatoException("Network Error Trending")
        coEvery { getMoviesUseCase(type = MovieListType.TRENDING, timeWindow = "day") } returns flowOf(Result.Error(exception))

        viewModel.fetchTrendingMovies()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoadingTrending)
        assertEquals(exception.message, uiState.errorTrending)
        assertTrue(uiState.trendingMovies.isEmpty())
    }

    @Test
    fun `fetchMoviesForCategory updates state successfully`() = runTest(testDispatcher.scheduler) {
        val categoryId = "action"
        val categoryDisplayName = "Action"
        val movies = listOf(sampleMovie("3", "Action Movie"))
        coEvery { getMoviesUseCase(type = MovieListType.BY_CATEGORY, categoryId = categoryId) } returns flowOf(Result.Success(movies))

        viewModel.fetchMoviesForCategory(categoryId, categoryDisplayName)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoadingCategory) // Assuming general category loading flag
        assertEquals(movies, uiState.categoryMovies[categoryDisplayName])
        assertNull(uiState.errorCategory)
    }

    @Test
    fun `retrySection calls correct fetch method for POPULAR`() = runTest(testDispatcher.scheduler) {
        coEvery { getMoviesUseCase(type = MovieListType.POPULAR, page = 1) } returns flowOf(Result.Success(emptyList()))

        viewModel.onRetrySection(MovieListType.POPULAR)
        advanceUntilIdle()

        coVerify(exactly = 1) { getMoviesUseCase(type = MovieListType.POPULAR, page = 1) }
    }

    @Test
    fun `retrySection calls correct fetch method for TRENDING`() = runTest(testDispatcher.scheduler) {
        coEvery { getMoviesUseCase(type = MovieListType.TRENDING, timeWindow = "day") } returns flowOf(Result.Success(emptyList()))

        viewModel.onRetrySection(MovieListType.TRENDING)
        advanceUntilIdle()

        coVerify(exactly = 1) { getMoviesUseCase(type = MovieListType.TRENDING, timeWindow = "day") }
    }

    @Test
    fun `retrySection calls correct fetch method for BY_CATEGORY`() = runTest(testDispatcher.scheduler) {
        val categoryId = "cat1"
        val categoryName = "Category1"
        coEvery { getMoviesUseCase(type = MovieListType.BY_CATEGORY, categoryId = categoryId) } returns flowOf(Result.Success(emptyList()))

        viewModel.onRetrySection(MovieListType.BY_CATEGORY, categoryId, categoryName)
        advanceUntilIdle()

        coVerify(exactly = 1) { getMoviesUseCase(type = MovieListType.BY_CATEGORY, categoryId = categoryId) }
    }
}
