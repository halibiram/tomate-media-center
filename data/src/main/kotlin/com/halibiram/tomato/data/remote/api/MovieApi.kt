package com.halibiram.tomato.data.remote.api

import com.halibiram.tomato.data.remote.dto.CreditsDto
import com.halibiram.tomato.data.remote.dto.MovieDto
import com.halibiram.tomato.data.remote.dto.PaginatedMovieResponseDto // Assuming paginated response

interface MovieApi {
    // Example: suspend fun getPopularMovies(@Query("page") page: Int): PaginatedMovieResponseDto
    suspend fun getPopularMovies(page: Int): PaginatedMovieResponseDto // Returning DTO list directly for simplicity now

    // Example: suspend fun getTrendingMovies(@Path("time_window") timeWindow: String): PaginatedMovieResponseDto
    suspend fun getTrendingMovies(timeWindow: String): PaginatedMovieResponseDto // day or week

    suspend fun getMovieDetails(movieId: String): MovieDto

    suspend fun getMovieCredits(movieId: String): CreditsDto

    suspend fun getSimilarMovies(movieId: String): PaginatedMovieResponseDto
}
