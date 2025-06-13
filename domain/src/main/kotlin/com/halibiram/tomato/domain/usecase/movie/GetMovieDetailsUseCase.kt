package com.halibiram.tomato.domain.usecase.movie

import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovieDetailsUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(movieId: String): Flow<Movie?> {
        return movieRepository.getMovieDetails(movieId)
    }
}
