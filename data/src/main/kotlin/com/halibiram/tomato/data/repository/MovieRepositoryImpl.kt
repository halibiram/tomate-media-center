package com.halibiram.tomato.data.repository

import com.halibiram.tomato.core.common.Resource
import com.halibiram.tomato.core.database.dao.MovieDao
import com.halibiram.tomato.core.network.service.MovieApiService
import com.halibiram.tomato.data.mapper.toDomain
import com.halibiram.tomato.data.mapper.toEntity
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val movieDao: MovieDao,
    private val movieApiService: MovieApiService
) : MovieRepository {

    private val CACHE_EXPIRY_HOURS = 6L
    private val API_KEY_PLACEHOLDER = "dummy_api_key" // Replace with actual key mechanism

    override fun getPopularMovies(page: Int, forceRefresh: Boolean): Flow<Resource<List<Movie>>> = flow {
        val localMoviesFlow = movieDao.getAllMovies().map { entities -> entities.map { it.toDomain() } }
        val currentLocalMovies = localMoviesFlow.firstOrNull() ?: emptyList()

        val firstMovieEntity = if (currentLocalMovies.isNotEmpty()) movieDao.getMovieById(currentLocalMovies.first().id).firstOrNull() else null
        var needsRefresh = forceRefresh || isCacheExpired(firstMovieEntity?.lastRefreshed) || currentLocalMovies.isEmpty()

        if (page > 1 && currentLocalMovies.isEmpty()) {
            needsRefresh = true
        }

        if (currentLocalMovies.isNotEmpty() && !needsRefresh && page == 1) {
            emit(Resource.Success(currentLocalMovies))
            // No network call needed if page 1 is cached and fresh
        } else if (needsRefresh) {
            // Emit Loading, potentially with stale data
            if (currentLocalMovies.isEmpty() || page == 1) { // For page 1, or if no cache, emit Loading without data first if refreshing
                 emit(Resource.Loading())
            } else { // For subsequent pages with existing cache, show stale data while loading
                 emit(Resource.Loading(currentLocalMovies))
            }

            try {
                val response = movieApiService.getPopularMovies(page, API_KEY_PLACEHOLDER)
                if (response.success && response.data != null) {
                    val movieEntities = response.data.map { it.toEntity() }
                    if (page == 1) {
                        movieDao.deleteAllMovies()
                    }
                    movieDao.insertMovies(movieEntities)

                    localMoviesFlow.collect { updatedMovies -> emit(Resource.Success(updatedMovies)) }
                } else {
                    emit(Resource.Error(response.message ?: "Failed to fetch popular movies", if (currentLocalMovies.isEmpty()) null else currentLocalMovies))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Network error", if (currentLocalMovies.isEmpty()) null else currentLocalMovies))
            }
        } else if (page > 1 && currentLocalMovies.isNotEmpty() && !needsRefresh) {
            // This case implies we are navigating to a subsequent page that should be cached and fresh.
            // The current DAO fetches all movies, so localMoviesFlow will emit the complete list.
            // For true pagination from cache, DAO would need page support.
            // Emitting the whole list is acceptable given current DAO.
            localMoviesFlow.collect { updatedMovies -> emit(Resource.Success(updatedMovies)) }
        }
    }

    override fun getMovieById(movieId: String, forceRefresh: Boolean): Flow<Resource<Movie>> = flow {
        val localMovieFlow = movieDao.getMovieById(movieId).map { entity -> entity?.toDomain() }
        var localMovie = localMovieFlow.firstOrNull()

        val movieEntity = movieDao.getMovieById(movieId).firstOrNull()
        val needsRefresh = forceRefresh || isCacheExpired(movieEntity?.lastRefreshed) || localMovie == null

        if (localMovie != null && !needsRefresh) {
            emit(Resource.Success(localMovie))
            return@flow
        }

        if (localMovie == null) {
            emit(Resource.Loading())
        } else {
            emit(Resource.Loading(localMovie))
        }

        try {
            val response = movieApiService.getMovieDetails(movieId, API_KEY_PLACEHOLDER)
            if (response.success && response.data != null) {
                movieDao.insertMovie(response.data.toEntity())
                localMovieFlow.collect{ updatedMovie ->
                    if(updatedMovie != null) emit(Resource.Success(updatedMovie))
                }
            } else {
                 emit(Resource.Error(response.message ?: "Failed to fetch movie details", localMovie))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error", localMovie))
        }
    }

    override fun searchMovies(query: String, page: Int): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieApiService.searchMovies(query, page, API_KEY_PLACEHOLDER)
            if (response.success && response.data != null) {
                emit(Resource.Success(response.data.map { it.toDomain() }))
            } else {
                emit(Resource.Error(response.message ?: "Search failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error during search"))
        }
    }

    private fun isCacheExpired(lastRefreshed: Long?): Boolean {
        if (lastRefreshed == null) return true
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastRefreshed) > TimeUnit.HOURS.toMillis(CACHE_EXPIRY_HOURS)
    }
}
