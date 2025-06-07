package com.halibiram.tomato.domain.usecase.movie

import com.halibiram.tomato.domain.repository.Movie
import com.halibiram.tomato.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    /**
     * Searches for movies based on a query.
     * @param query The search query.
     * @param page The page number for pagination.
     * @return A Flow emitting a list of movies matching the query.
     */
    operator fun invoke(query: String, page: Int): Flow<List<Movie>> {
        if (query.isBlank()) {
            // Optionally return popular movies or an empty list if query is blank
            return movieRepository.getPopularMovies(page) // Or kotlinx.coroutines.flow.flowOf(emptyList())
        }
        return movieRepository.searchMovies(query, page)
    }
}
