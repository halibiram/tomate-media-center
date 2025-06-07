package com.halibiram.tomato.core.network.service

import com.halibiram.tomato.core.network.dto.MovieDto
import com.halibiram.tomato.core.network.model.ApiResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject

class MovieApiService @Inject constructor(
    httpClient: HttpClient,
) : BaseApiService(httpClient) {

    // Example: override val baseUrl: String = "https://api.themoviedb.org/3/"

    suspend fun getPopularMovies(page: Int, apiKey: String): ApiResponse<List<MovieDto>> {
        return safeApiCall {
            url("${super.baseUrl}movie/popular")
            method = HttpMethod.Get
            parameter("api_key", apiKey)
            parameter("page", page)
        }
    }

    suspend fun getMovieDetails(movieId: String, apiKey: String): ApiResponse<MovieDto> {
        return safeApiCall {
            url("${super.baseUrl}movie/$movieId")
            method = HttpMethod.Get
            parameter("api_key", apiKey)
        }
    }

    suspend fun searchMovies(query: String, page: Int, apiKey: String): ApiResponse<List<MovieDto>> {
         return safeApiCall {
            url("${super.baseUrl}search/movie")
            method = HttpMethod.Get
            parameter("api_key", apiKey)
            parameter("query", query)
            parameter("page", page)
        }
    }
}
