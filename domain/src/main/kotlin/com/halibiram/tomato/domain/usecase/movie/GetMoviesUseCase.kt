package com.halibiram.tomato.domain.usecase.movie

import com.halibiram.tomato.core.common.result.Result // Ensure this is the correct Result class
import com.halibiram.tomato.domain.model.Movie // Correct domain model
import com.halibiram.tomato.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Define MovieListType enum, could be in a separate file in domain/model or within use case file if specific.
enum class MovieListType {
    POPULAR,
    TRENDING,
    BY_CATEGORY // Add more types as needed (e.g., UPCOMING, TOP_RATED)
}

class GetMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    /**
     * Fetches a list of movies based on the specified type.
     *
     * @param type The type of movie list to fetch (e.g., POPULAR, TRENDING).
     * @param page The page number for pagination (default is 1).
     * @param timeWindow The time window for trending movies (e.g., "day", "week", default is "day").
     *                 Applicable only if type is TRENDING.
     * @param categoryId The category ID for fetching movies by category.
     *                   Applicable only if type is BY_CATEGORY.
     * @return A Flow emitting a Result containing a list of movies.
     */
    suspend operator fun invoke(
        type: MovieListType,
        page: Int = 1,
        timeWindow: String = "day", // Default for trending
        categoryId: String? = null
    ): Flow<Result<List<Movie>>> {
        return when (type) {
            MovieListType.POPULAR -> movieRepository.getPopularMovies(page)
            MovieListType.TRENDING -> movieRepository.getTrendingMovies(timeWindow) // Assuming page is handled by API or not needed for trending's first page
            MovieListType.BY_CATEGORY -> {
                if (categoryId == null) {
                    throw IllegalArgumentException("CategoryId must be provided for MovieListType.BY_CATEGORY")
                    // Or return Flow<Result.Error> immediately
                    // kotlinx.coroutines.flow.flowOf(Result.Error(com.halibiram.tomato.core.common.result.IllegalArgumentException("Category ID missing")))
                }
                movieRepository.getMoviesByCategory(categoryId) // Assuming page is handled by API or not needed for category's first page
            }
            // Add other cases as new types are introduced
        }
    }
}
