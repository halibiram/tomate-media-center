package com.halibiram.tomato.domain.repository

import com.halibiram.tomato.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getPopularMovies(page: Int): Flow<List<Movie>> // Example: Using Flow for reactive streams
    fun getMovieDetails(movieId: String): Flow<Movie?>
    fun searchMovies(query: String, page: Int): Flow<List<Movie>>
    // Add other movie related repository methods
}
