package com.halibiram.tomato.data.repository

import com.halibiram.tomato.core.common.result.*
import com.halibiram.tomato.core.database.dao.MovieDao
import com.halibiram.tomato.core.database.entity.MovieEntity
import com.halibiram.tomato.data.remote.api.MovieApi
import com.halibiram.tomato.data.remote.api.SearchApi // Added SearchApi
import com.halibiram.tomato.data.remote.dto.MovieDto
import com.halibiram.tomato.data.remote.mapper.toDomain // For List<MovieDto>
import com.halibiram.tomato.data.remote.mapper.toDomainDetails // For single MovieDto to MovieDetails
import com.halibiram.tomato.data.remote.mapper.toDomainMovie // For SearchResultDto to Movie
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.model.MovieDetails
import com.halibiram.tomato.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val movieApi: MovieApi,
    private val searchApi: SearchApi, // Injected SearchApi
    private val movieDao: MovieDao
) : MovieRepository {

    override fun getPopularMovies(page: Int): Flow<Result<List<Movie>>> = flow {
        emit(Result.Loading())
        try {
            val response = movieApi.getPopularMovies(page)
            val domainMovies = response.results.toDomain()
            movieDao.insertMovies(domainMovies.map { it.toEntity() })
            emit(Result.Success(domainMovies))
        } catch (e: Exception) {
            emit(Result.Error(NetworkException("Failed to fetch popular movies: ${e.message}", e)))
        }
    }.catch { e ->
        emit(Result.Error(UnknownErrorException("Unknown error in getPopularMovies flow", e)))
    }

    override fun getTrendingMovies(timeWindow: String): Flow<Result<List<Movie>>> = flow {
        emit(Result.Loading())
        try {
            val response = movieApi.getTrendingMovies(timeWindow)
            val domainMovies = response.results.toDomain()
            movieDao.insertMovies(domainMovies.map { it.toEntity() })
            emit(Result.Success(domainMovies))
        } catch (e: Exception) {
            emit(Result.Error(NetworkException("Failed to fetch trending movies: ${e.message}", e)))
        }
    }.catch { e ->
        emit(Result.Error(UnknownErrorException("Unknown error in getTrendingMovies flow", e)))
    }

    override fun getMoviesByCategory(categoryId: String): Flow<Result<List<Movie>>> = flow {
        emit(Result.Loading())
        try {
            kotlinx.coroutines.delay(200)
            emit(Result.Success(emptyList<Movie>()))
        } catch (e: Exception) {
            emit(Result.Error(NetworkException("Failed to fetch movies for category $categoryId: ${e.message}", e)))
        }
    }.catch { e ->
        emit(Result.Error(UnknownErrorException("Unknown error in getMoviesByCategory flow", e)))
    }

    override fun getMovieDetails(movieId: String): Flow<Result<MovieDetails>> = flow {
        emit(Result.Loading(null)) // Emit loading, optionally with current cached data if available
        val cachedEntity = movieDao.getMovieById(movieId)
        if (cachedEntity != null) {
            emit(Result.Loading(cachedEntity.toDomainDetailsModel())) // Emit cached data while loading fresh
        }

        try {
            val movieDetailsDto = movieApi.getMovieDetails(movieId)
            val domainMovieDetails = movieDetailsDto.toDomainDetails()
            movieDao.insertMovie(domainMovieDetails.toEntity()) // This should be MovieEntity from MovieDetails
            emit(Result.Success(domainMovieDetails))
        } catch (e: Exception) {
            // If there was no cached data, or if we want to signal error regardless
            if (cachedEntity == null) {
                 emit(Result.Error(NetworkException("Failed to fetch movie details for $movieId: ${e.message}", e)))
            } else {
                // Optionally, if there was cached data, we might choose not to overwrite it with an error
                // or emit a specific type of error indicating that cached data is shown but refresh failed.
                // For now, if cache was emitted, we just log the error or ignore if UI is already showing data.
                // To ensure UI can show "couldn't refresh" message, we can emit the error.
                 emit(Result.Error(NetworkException("Failed to refresh movie details for $movieId (showing cached): ${e.message}", e)))
            }
        }
    }.catch { e ->
         if (cachedEntity == null) { // Only emit general error if nothing was ever shown
            emit(Result.Error(UnknownErrorException("Unknown error in getMovieDetails flow for $movieId", e)))
         }
    }

    // Implementation for movie search
    override fun searchMovies(query: String, page: Int): Flow<Result<List<Movie>>> = flow {
        emit(Result.Loading())
        if (query.isBlank()) {
            emit(Result.Success(emptyList())) // Or some other behavior for blank query
            return@flow
        }
        try {
            val response = searchApi.searchMulti(query = query, page = page)
            // Filter for movies and map to domain model
            val domainMovies = response.results
                .filter { it.mediaType == "movie" }
                .mapNotNull { it.toDomainMovie() } // Use mapNotNull to skip any that fail mapping

            // Optionally, cache these search results if appropriate
            // movieDao.insertMovies(domainMovies.map { it.toEntity() }) // Be careful with search result caching strategy

            emit(Result.Success(domainMovies))
        } catch (e: Exception) {
            emit(Result.Error(NetworkException("Failed to search movies for query '$query': ${e.message}", e)))
        }
    }.catch { e ->
        emit(Result.Error(UnknownErrorException("Unknown error in searchMovies flow for query '$query'", e)))
    }

    // Mappers (Domain Model <-> Entity Model)
    private fun Movie.toEntity(): MovieEntity {
        return MovieEntity(
            id = this.id,
            title = this.title,
            overview = this.description,
            posterUrl = this.posterUrl,
            backdropUrl = null, // Movie domain model for lists might not have this.
            releaseDate = this.releaseDate?.let { parseDate(it) },
            genres = this.genres,
            voteAverage = this.rating,
            // Initialize other fields for MovieEntity from Movie domain model
            lastRefreshed = Date() // Update refresh timestamp
        )
    }

    private fun MovieDetails.toEntity(): MovieEntity {
        return MovieEntity(
            id = this.id,
            title = this.title,
            overview = this.overview,
            posterUrl = this.posterUrl,
            backdropUrl = this.backdropUrl,
            releaseDate = this.releaseDate?.let { parseDate(it) },
            genres = this.genres,
            voteAverage = this.rating,
            runtimeMinutes = this.runtimeMinutes,
            lastRefreshed = Date()
        )
    }

    private fun MovieEntity.toDomainModel(): Movie {
        return Movie(
            id = this.id,
            title = this.title ?: "Unknown Title",
            description = this.overview ?: "",
            posterUrl = this.posterUrl,
            releaseDate = this.releaseDate?.toFormattedString(),
            genres = this.genres,
            rating = this.voteAverage
        )
    }

    private fun MovieEntity.toDomainDetailsModel(): MovieDetails {
        return MovieDetails(
            id = this.id,
            title = this.title ?: "Unknown Title",
            overview = this.overview ?: "",
            posterUrl = this.posterUrl,
            backdropUrl = this.backdropUrl,
            releaseDate = this.releaseDate?.toFormattedString(),
            genres = this.genres,
            rating = this.voteAverage,
            runtimeMinutes = this.runtimeMinutes
        )
    }

    // Dummy date parser/formatter, replace with robust parsing/formatting
    private fun parseDate(dateString: String): Date? {
        // Implement proper date parsing based on expected string format (e.g., "yyyy-MM-dd")
        return try { java.text.SimpleDateFormat("yyyy-MM-dd").parse(dateString) } catch (e: Exception) { null }
    }
    private fun Date.toFormattedString(): String {
        // Implement proper date formatting
        return java.text.SimpleDateFormat("yyyy-MM-dd").format(this)
    }
}
