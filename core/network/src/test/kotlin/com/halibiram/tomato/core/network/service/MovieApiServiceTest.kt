package com.halibiram.tomato.core.network.service

import com.halibiram.tomato.core.network.dto.MovieDto
import com.halibiram.tomato.core.network.model.ApiResponse
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class MovieApiServiceTest {

    private val mockApiKey = "test_api_key"

    private fun createMockClient(responseContent: String, statusCode: HttpStatusCode): HttpClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = responseContent,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            // No need for other plugins like Logging or Retry for these unit tests
        }
    }

    @Test
    fun `getPopularMovies returns success with movie list`() = runTest {
        val mockMovies = listOf(MovieDto(id = "1", title = "Test Movie", overview = "Overview", posterPath = null, backdropPath = null, releaseDate = null, voteAverage = null, genreIds = null))
        // The service's safeApiCall wraps the DTO list in ApiResponse
        // val mockApiResponse = ApiResponse(data = mockMovies, success = true)
        val client = createMockClient(Json.encodeToString(mockMovies), HttpStatusCode.OK) // Ktor returns raw DTO list
        val service = MovieApiService(client)
        service.baseUrl = "http://localhost/" // BaseUrl set in BaseApiService

        val result = service.getPopularMovies(1, mockApiKey)

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(1, result.data?.size)
        assertEquals("Test Movie", result.data?.first()?.title)
    }

    @Test
    fun `getPopularMovies returns error on API failure`() = runTest {
        val client = createMockClient("{\"error\":\"Not Found\"}", HttpStatusCode.NotFound)
        val service = MovieApiService(client)
        service.baseUrl = "http://localhost/"

        val result = service.getPopularMovies(1, mockApiKey)

        assertFalse(result.success)
        assertNull(result.data)
        assertTrue(result.message?.contains("Request failed: Not Found") == true)
    }

    @Test
    fun `getMovieDetails returns success with movie details`() = runTest {
        val mockMovie = MovieDto(id = "1", title = "Test Movie Detail", overview = "Detail Overview", posterPath = null, backdropPath = null, releaseDate = null, voteAverage = 7.0, genreIds = listOf(1,2))
        val client = createMockClient(Json.encodeToString(mockMovie), HttpStatusCode.OK)
        val service = MovieApiService(client)
        service.baseUrl = "http://localhost/"

        val result = service.getMovieDetails("1", mockApiKey)

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals("Test Movie Detail", result.data?.title)
        assertEquals(7.0, result.data?.voteAverage, 0.0)
    }

    @Test
    fun `searchMovies returns success with movie list`() = runTest {
        val mockMovies = listOf(MovieDto(id = "s1", title = "Search Result Movie", overview = "Overview", posterPath = null, backdropPath = null, releaseDate = null, voteAverage = null, genreIds = null))
        val client = createMockClient(Json.encodeToString(mockMovies), HttpStatusCode.OK)
        val service = MovieApiService(client)
        service.baseUrl = "http://localhost/"

        val result = service.searchMovies("test query", 1, mockApiKey)

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(1, result.data?.size)
        assertEquals("Search Result Movie", result.data?.first()?.title)
    }
}
