package com.halibiram.tomato.data.remote.api

// import com.halibiram.tomato.data.remote.dto.MovieDto
// import com.halibiram.tomato.data.remote.dto.PaginatedResponseDto // Assuming a generic DTO for paginated lists
// import io.ktor.client.HttpClient
// import io.ktor.client.request.get
// import io.ktor.client.call.body
// import javax.inject.Inject

// class MovieApi @Inject constructor(private val httpClient: HttpClient) {
//    suspend fun getPopularMovies(page: Int): PaginatedResponseDto<MovieDto> {
//        return httpClient.get("movie/popular") { parameter("page", page) }.body()
//    }
//    suspend fun getMovieDetails(movieId: String): MovieDto {
//        return httpClient.get("movie/{movieId}") { url { parameters.append("movie_id", movieId) } }.body()
//    }
    // Add other movie related API calls
// }

// Placeholder
interface MovieApi {
    // suspend fun getPopularMovies(page: Int): Any // Placeholder for PaginatedResponseDto<MovieDto>
    // suspend fun getMovieDetails(movieId: String): Any // Placeholder for MovieDto
}
