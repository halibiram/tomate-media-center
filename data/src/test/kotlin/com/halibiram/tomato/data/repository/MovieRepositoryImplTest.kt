package com.halibiram.tomato.data.repository

import app.cash.turbine.test
import com.halibiram.tomato.core.common.Resource
import com.halibiram.tomato.core.database.dao.MovieDao
import com.halibiram.tomato.core.database.entity.MovieEntity
import com.halibiram.tomato.core.network.dto.MovieDto
import com.halibiram.tomato.core.network.model.ApiResponse
import com.halibiram.tomato.core.network.service.MovieApiService
import com.halibiram.tomato.data.mapper.toDomain
import com.halibiram.tomato.data.mapper.toEntity
import com.halibiram.tomato.domain.model.Movie
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class MovieRepositoryImplTest {

    private lateinit var movieDao: MovieDao
    private lateinit var movieApiService: MovieApiService
    private lateinit var movieRepository: MovieRepositoryImpl

    private val testMovieDto1 = MovieDto("1", "Movie 1 DTO", "Overview 1", null, null, "2023", 7.0, null)
    private val testMovieEntity1 = testMovieDto1.toEntity().copy(lastRefreshed = System.currentTimeMillis())
    private val testMovieDomain1 = testMovieEntity1.toDomain()

    private val testMovieDto2 = MovieDto("2", "Movie 2 DTO", "Overview 2", null, null, "2024", 8.0, null)
    // private val testMovieEntity2 = testMovieDto2.toEntity().copy(lastRefreshed = System.currentTimeMillis())


    @Before
    fun setUp() {
        movieDao = mockk(relaxed = true)
        movieApiService = mockk(relaxed = true)
        movieRepository = MovieRepositoryImpl(movieDao, movieApiService)
    }

    @Test
    fun `getPopularMovies fetches from network if cache is empty and stores in db`() = runTest {
        coEvery { movieDao.getAllMovies() } returns flowOf(emptyList())
        coEvery { movieDao.getMovieById(any()) } returns flowOf(null) // for cache check
        coEvery { movieApiService.getPopularMovies(1, any()) } returns ApiResponse(success = true, data = listOf(testMovieDto1))

        movieRepository.getPopularMovies(page = 1, forceRefresh = false).test {
            assertEquals(Resource.Loading<List<Movie>>().javaClass, awaitItem().javaClass) // Loading

            val successResult = awaitItem()
            assertTrue(successResult is Resource.Success)
            assertEquals(1, successResult.data?.size)
            assertEquals(testMovieDomain1.title, successResult.data?.first()?.title)

            coVerify { movieDao.deleteAllMovies() } // Page 1 refresh clears old popular movies
            coVerify { movieDao.insertMovies(any()) } // Verify insertion

            awaitComplete()
        }
    }

    @Test
    fun `getPopularMovies returns cached data if fresh and not forcing refresh`() = runTest {
        coEvery { movieDao.getAllMovies() } returns flowOf(listOf(testMovieEntity1))
        coEvery { movieDao.getMovieById(testMovieEntity1.id) } returns flowOf(testMovieEntity1) // For cache check

        movieRepository.getPopularMovies(page = 1, forceRefresh = false).test {
            val successResult = awaitItem()
            assertTrue(successResult is Resource.Success)
            assertEquals(1, successResult.data?.size)
            assertEquals(testMovieDomain1.title, successResult.data?.first()?.title)

            coVerify(exactly = 0) { movieApiService.getPopularMovies(any(), any()) }
            awaitComplete()
        }
    }

    @Test
    fun `getPopularMovies fetches from network if cache is expired`() = runTest {
        val expiredEntity = testMovieEntity1.copy(lastRefreshed = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(7))
        coEvery { movieDao.getAllMovies() } returns flowOf(listOf(expiredEntity))
        coEvery { movieDao.getMovieById(expiredEntity.id) } returns flowOf(expiredEntity)
        coEvery { movieApiService.getPopularMovies(1, any()) } returns ApiResponse(success = true, data = listOf(testMovieDto2)) // Fetches new data

        movieRepository.getPopularMovies(page = 1, forceRefresh = false).test {
            val loadingResult = awaitItem()
            assertTrue(loadingResult is Resource.Loading)
            // Check if stale data is emitted with Loading
            // assertEquals(1, loadingResult.data?.size)
            // assertEquals(expiredEntity.toDomain().title, loadingResult.data?.first()?.title)


            val successResult = awaitItem()
            assertTrue(successResult is Resource.Success)
            assertEquals(1, successResult.data?.size)
            assertEquals(testMovieDto2.title, successResult.data?.first()?.title) // New data

            coVerify { movieDao.deleteAllMovies() }
            coVerify { movieDao.insertMovies(any()) }
            awaitComplete()
        }
    }

    @Test
    fun `getPopularMovies fetches from network when forceRefresh is true`() = runTest {
        coEvery { movieDao.getAllMovies() } returns flowOf(listOf(testMovieEntity1)) // Cache is fresh
        coEvery { movieDao.getMovieById(testMovieEntity1.id) } returns flowOf(testMovieEntity1)
        coEvery { movieApiService.getPopularMovies(1, any()) } returns ApiResponse(success = true, data = listOf(testMovieDto2)) // Fetches new data

        movieRepository.getPopularMovies(page = 1, forceRefresh = true).test {
            val loadingResult = awaitItem()
            assertTrue(loadingResult is Resource.Loading)
            // Stale data might be emitted here with loading
            // if (loadingResult.data != null) {
            //    assertEquals(testMovieEntity1.toDomain().title, loadingResult.data?.first()?.title)
            // }

            val successResult = awaitItem()
            assertTrue(successResult is Resource.Success)
            assertEquals(testMovieDto2.title, successResult.data?.first()?.title) // New data

            coVerify { movieApiService.getPopularMovies(1, any()) }
            coVerify { movieDao.deleteAllMovies() }
            coVerify { movieDao.insertMovies(any()) }
            awaitComplete()
        }
    }

    @Test
    fun `getPopularMovies handles API error gracefully returning cached data if available`() = runTest {
        coEvery { movieDao.getAllMovies() } returns flowOf(listOf(testMovieEntity1)) // Cache exists
        coEvery { movieDao.getMovieById(testMovieEntity1.id) } returns flowOf(testMovieEntity1)
        coEvery { movieApiService.getPopularMovies(1, any()) } returns ApiResponse(success = false, message = "Network Error")

        movieRepository.getPopularMovies(page = 1, forceRefresh = true).test {
            val loading = awaitItem()
            assertTrue(loading is Resource.Loading)
            // Stale data might be emitted here
            // if (loading.data != null) {
            //     assertEquals(testMovieEntity1.toDomain().title, loading.data?.first()?.title)
            // }

            val errorResult = awaitItem()
            assertTrue(errorResult is Resource.Error)
            assertEquals("Network Error", errorResult.message)
            assertEquals(testMovieEntity1.toDomain().title, errorResult.data?.first()?.title) // Stale data emitted with error

            awaitComplete()
        }
    }

    @Test
    fun `getMovieById returns cached data if fresh`() = runTest {
        coEvery { movieDao.getMovieById(testMovieEntity1.id) } returns flowOf(testMovieEntity1)

        movieRepository.getMovieById(testMovieEntity1.id, forceRefresh = false).test {
            val successResult = awaitItem()
            assertTrue(successResult is Resource.Success)
            assertEquals(testMovieDomain1.title, successResult.data?.title)
            coVerify(exactly = 0) { movieApiService.getMovieDetails(any(), any()) }
            awaitComplete()
        }
    }

    @Test
    fun `getMovieById fetches from network if not in cache and stores`() = runTest {
        coEvery { movieDao.getMovieById(testMovieDto1.id) } returns flowOf(null) // Not in cache
        coEvery { movieApiService.getMovieDetails(testMovieDto1.id, any()) } returns ApiResponse(success = true, data = testMovieDto1)

        movieRepository.getMovieById(testMovieDto1.id, forceRefresh = false).test {
            assertTrue(awaitItem() is Resource.Loading)

            val successResult = awaitItem()
            assertTrue(successResult is Resource.Success)
            assertEquals(testMovieDto1.title, successResult.data?.title)

            coVerify { movieDao.insertMovie(any()) }
            awaitComplete()
        }
    }

    @Test
    fun `searchMovies fetches from network and returns domain models`() = runTest {
        coEvery { movieApiService.searchMovies("query", 1, any()) } returns ApiResponse(success = true, data = listOf(testMovieDto1))

        movieRepository.searchMovies("query", 1).test {
            assertTrue(awaitItem() is Resource.Loading)

            val successResult = awaitItem()
            assertTrue(successResult is Resource.Success)
            assertEquals(1, successResult.data?.size)
            assertEquals(testMovieDto1.title, successResult.data?.first()?.title)

            coVerify(exactly = 0) { movieDao.insertMovies(any()) } // Search results not cached by default in this impl
            awaitComplete()
        }
    }
}
