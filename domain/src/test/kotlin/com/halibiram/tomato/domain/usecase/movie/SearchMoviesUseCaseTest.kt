package com.halibiram.tomato.domain.usecase.movie

import com.halibiram.tomato.core.common.result.Result
import com.halibiram.tomato.core.common.result.TomatoException
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class SearchMoviesUseCaseTest {

    private lateinit var movieRepository: MovieRepository
    private lateinit var searchMoviesUseCase: SearchMoviesUseCase

    @BeforeEach
    fun setUp() {
        movieRepository = mockk(relaxed = true) // Relaxed mock for simplicity
        searchMoviesUseCase = SearchMoviesUseCase(movieRepository)
    }

    private fun sampleMovie(id: String, title: String) = Movie(id, title, "Desc", "url", "date", emptyList(), 0.0)

    @Test
    fun `invoke with non-blank query calls repository searchMovies and returns its result`() = runTest {
        val query = "Inception"
        val page = 1
        val expectedMovies = listOf(sampleMovie("1", "Inception Movie"))
        val expectedResultFlow = flowOf(Result.Success(expectedMovies))
        coEvery { movieRepository.searchMovies(query, page) } returns expectedResultFlow

        val actualResultFlow = searchMoviesUseCase(query, page)

        assertEquals(expectedResultFlow.first(), actualResultFlow.first())
        coVerify(exactly = 1) { movieRepository.searchMovies(query, page) }
    }

    @Test
    fun `invoke with blank query returns success with empty list and does not call repository`() = runTest {
        val query = "  " // Blank query
        val page = 1

        val resultFlow = searchMoviesUseCase(query, page)
        val actualResult = resultFlow.first()

        coVerify(exactly = 0) { movieRepository.searchMovies(any(), any()) }
        assertTrue(actualResult is Result.Success && actualResult.data.isEmpty(), "Result should be Success with empty data for blank query")
    }

    @Test
    fun `invoke with empty query returns success with empty list and does not call repository`() = runTest {
        val query = "" // Empty query
        val page = 1

        val resultFlow = searchMoviesUseCase(query, page)
        val actualResult = resultFlow.first()

        coVerify(exactly = 0) { movieRepository.searchMovies(any(), any()) }
        assertTrue(actualResult is Result.Success && actualResult.data.isEmpty(), "Result should be Success with empty data for empty query")
    }

    @Test
    fun `use case propagates error result from repository during search`() = runTest {
        val query = "ErrorSearch"
        val page = 1
        val exception = TomatoException("Search Network error")
        val expectedErrorFlow = flowOf(Result.Error(exception))
        coEvery { movieRepository.searchMovies(query, page) } returns expectedErrorFlow

        val actualResultFlow = searchMoviesUseCase(query, page)

        assertEquals(expectedErrorFlow.first(), actualResultFlow.first())
        coVerify(exactly = 1) { movieRepository.searchMovies(query, page) }
    }
}
