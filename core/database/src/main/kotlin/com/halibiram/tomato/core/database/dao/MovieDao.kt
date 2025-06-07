package com.halibiram.tomato.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.halibiram.tomato.core.database.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: String): MovieEntity? // Changed to suspend fun for one-shot query

    @Query("SELECT * FROM movies ORDER BY popularity DESC, title ASC") // Example: order by popularity then title
    fun getPopularMovies(): Flow<List<MovieEntity>> // Assuming this fetches all and sorts by popularity

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' ORDER BY releaseDate DESC")
    fun searchMovies(query: String): Flow<List<MovieEntity>>

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovieById(id: String) // Added specific delete by ID

    @Query("DELETE FROM movies")
    suspend fun clearMovies() // Renamed from deleteAllMovies for clarity

    // Example from old placeholder, kept if relevant for cache cleaning
    @Query("SELECT * FROM movies WHERE lastRefreshed < :timestamp")
    suspend fun getMoviesOlderThan(timestamp: Long): List<MovieEntity>
}
