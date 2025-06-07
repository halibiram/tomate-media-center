package com.halibiram.tomato.core.database.dao

import androidx.room.Dao
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

    @Query("SELECT * FROM movies WHERE id = :movieId")
    fun getMovieById(movieId: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies ORDER BY releaseDate DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("DELETE FROM movies WHERE id = :movieId")
    suspend fun deleteMovieById(movieId: String)

    @Query("DELETE FROM movies")
    suspend fun deleteAllMovies()

    // Example: Query to get movies older than a certain date for cache cleaning
    @Query("SELECT * FROM movies WHERE lastRefreshed < :timestamp")
    suspend fun getMoviesOlderThan(timestamp: Long): List<MovieEntity>
}
