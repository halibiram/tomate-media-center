package com.halibiram.tomato.domain.usecase.movie

import com.halibiram.tomato.core.common.result.Result // Ensure this is the correct Result class
import com.halibiram.tomato.domain.model.Movie // Correct domain model
import com.halibiram.tomato.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository // Assuming MovieRepository now has searchMovies
) {
    /**
     * Searches for movies based on a query.
     * @param query The search query.
     * @param page The page number for pagination.
     * @return A Flow emitting a Result containing a list of movies matching the query.
     */
    suspend operator fun invoke(query: String, page: Int): Flow<Result<List<Movie>>> {
        if (query.isBlank()) {
            // Return success with empty list if query is blank, or specific error/result if preferred
            return flowOf(Result.Success(emptyList()))
        }
        // Debouncing logic, if not handled in ViewModel or UI, could be considered here,
        // but typically ViewModel is a better place for UI-related debounce.
        return movieRepository.searchMovies(query, page)
    }
}
