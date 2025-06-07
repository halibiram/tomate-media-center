package com.halibiram.tomato.domain.usecase.movie

import com.halibiram.tomato.domain.repository.MovieDetails
import com.halibiram.tomato.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovieDetailsUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    /**
     * Fetches details for a specific movie.
     * @param movieId The ID of the movie.
     * @return A Flow emitting the movie details, or null if not found.
     */
    operator fun invoke(movieId: String): Flow<MovieDetails?> {
        return movieRepository.getMovieDetails(movieId)
    }
}
