package com.halibiram.tomato.domain.usecase.movie

import com.halibiram.tomato.domain.repository.Movie
import com.halibiram.tomato.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    /**
     * Fetches popular movies.
     * @param page The page number to retrieve.
     * @return A Flow emitting a list of popular movies.
     */
    operator fun invoke(page: Int): Flow<List<Movie>> {
        return movieRepository.getPopularMovies(page)
    }
}
