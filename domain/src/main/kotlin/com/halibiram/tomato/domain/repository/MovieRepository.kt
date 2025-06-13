package com.halibiram.tomato.domain.repository

import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getPopularMovies(page: Int, forceRefresh: Boolean = false): Flow<Resource<List<Movie>>>
    fun getMovieById(movieId: String, forceRefresh: Boolean = false): Flow<Resource<Movie>>
    fun searchMovies(query: String, page: Int): Flow<Resource<List<Movie>>>
}
