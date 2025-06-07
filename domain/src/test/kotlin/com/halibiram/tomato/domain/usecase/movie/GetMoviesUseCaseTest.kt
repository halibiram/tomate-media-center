package com.halibiram.tomato.domain.usecase.movie

import com.halibiram.tomato.core.common.result.Result
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.repository.MovieRepository
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
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
class GetMoviesUseCaseTest {

    private lateinit var movieRepository: MovieRepository
    private lateinit var getMoviesUseCase: GetMoviesUseCase

    @BeforeEach
    fun setUp() {
        movieRepository = mockk()
        getMoviesUseCase = GetMoviesUseCase(movieRepository)
    }

    private fun sampleMovie(id: String, title: String) = Movie(id, title, "Desc", "url", "date", emptyList(), 0.0)

    @Test
    fun `invoke with POPULAR type calls repository getPopularMovies`() = runTest {
        // Given
        val page = 1
        val expectedMovies = listOf(sampleMovie("1", "Popular Movie"))
        val expectedResult = Result.Success(expectedMovies)
        every { movieRepository.getPopularMovies(page) } returns flowOf(expectedResult)

        // When
        val resultFlow = getMoviesUseCase(type = MovieListType.POPULAR, page = page)
        val actualResult = resultFlow.first()

        // Then
        coVerify(exactly = 1) { movieRepository.getPopularMovies(page) }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `invoke with TRENDING type calls repository getTrendingMovies`() = runTest {
        // Given
        val timeWindow = "week"
        val expectedMovies = listOf(sampleMovie("2", "Trending Movie"))
        val expectedResult = Result.Success(expectedMovies)
        // For trending, page might not be applicable or handled differently by repository
        every { movieRepository.getTrendingMovies(timeWindow) } returns flowOf(expectedResult)

        // When
        val resultFlow = getMoviesUseCase(type = MovieListType.TRENDING, timeWindow = timeWindow)
        val actualResult = resultFlow.first()

        // Then
        coVerify(exactly = 1) { movieRepository.getTrendingMovies(timeWindow) }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `invoke with BY_CATEGORY type calls repository getMoviesByCategory`() = runTest {
        // Given
        val categoryId = "action"
        val expectedMovies = listOf(sampleMovie("3", "Action Movie"))
        val expectedResult = Result.Success(expectedMovies)
        every { movieRepository.getMoviesByCategory(categoryId) } returns flowOf(expectedResult)

        // When
        val resultFlow = getMoviesUseCase(type = MovieListType.BY_CATEGORY, categoryId = categoryId)
        val actualResult = resultFlow.first()

        // Then
        coVerify(exactly = 1) { movieRepository.getMoviesByCategory(categoryId) }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `invoke with BY_CATEGORY type and null categoryId throws IllegalArgumentException`() = runTest {
        // Given
        // No setup needed for repository as it shouldn't be called

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            // Collecting is necessary to trigger the execution path that throws.
            getMoviesUseCase(type = MovieListType.BY_CATEGORY, categoryId = null).first()
        }
        assertEquals("CategoryId must be provided for MovieListType.BY_CATEGORY", exception.message)
        coVerify(exactly = 0) { movieRepository.getMoviesByCategory(any()) }
    }

    @Test
    fun `use case propagates error result from repository`() = runTest {
        // Given
        val page = 1
        val exception = TomatoException("Network error")
        val expectedResult = Result.Error(exception)
        every { movieRepository.getPopularMovies(page) } returns flowOf(expectedResult)

        // When
        val resultFlow = getMoviesUseCase(type = MovieListType.POPULAR, page = page)
        val actualResult = resultFlow.first()

        // Then
        assertEquals(expectedResult, actualResult)
    }
}
