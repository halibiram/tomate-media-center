package com.halibiram.tomato.domain.repository

import kotlinx.coroutines.flow.Flow

// Placeholder data models (should be defined in domain layer if not already)
data class Movie(val id: String, val title: String, val overview: String, val posterUrl: String?)
data class MovieDetails(val id: String, val title: String, val overview: String, val posterUrl: String?, val backdropUrl: String?, val releaseDate: String?, val genres: List<String>?)

interface MovieRepository {
    fun getPopularMovies(page: Int): Flow<List<Movie>>
    fun getMovieDetails(movieId: String): Flow<MovieDetails?>
    fun searchMovies(query: String, page: Int): Flow<List<Movie>>
    // Add other movie-related methods, e.g., getTrendingMovies, getUpcomingMovies, etc.
}
