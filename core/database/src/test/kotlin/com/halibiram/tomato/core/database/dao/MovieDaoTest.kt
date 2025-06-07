package com.halibiram.tomato.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.halibiram.tomato.core.database.AppDatabase
import com.halibiram.tomato.core.database.entity.MovieEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // Configure for a specific SDK version Robolectric supports
class MovieDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var movieDao: MovieDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // For testing only
            .build()
        movieDao = database.movieDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `insertMovie and getMovieById retrieves correct movie`() = runTest {
        val movie = MovieEntity("1", "Test Movie", "Overview", null, null, "2023-01-01", 7.5, listOf("Action"))
        movieDao.insertMovie(movie)

        val retrievedMovie = movieDao.getMovieById("1").first()
        assertNotNull(retrievedMovie)
        assertEquals("Test Movie", retrievedMovie?.title)
        assertEquals(listOf("Action"), retrievedMovie?.genres)
    }

    @Test
    fun `insertMovies and getAllMovies retrieves all movies`() = runTest {
        val movies = listOf(
            MovieEntity("1", "Movie 1", "Overview 1", null, null, "2023-01-01", 7.0, listOf("Genre1")),
            MovieEntity("2", "Movie 2", "Overview 2", null, null, "2023-01-02", 8.0, listOf("Genre2"))
        )
        movieDao.insertMovies(movies)

        val allMovies = movieDao.getAllMovies().first()
        assertEquals(2, allMovies.size)
    }

    @Test
    fun `deleteMovieById removes the movie`() = runTest {
        val movie = MovieEntity("1", "Test Movie", "Overview", null, null, "2023-01-01", 7.5, listOf("Action"))
        movieDao.insertMovie(movie)
        movieDao.deleteMovieById("1")

        val retrievedMovie = movieDao.getMovieById("1").first()
        assertNull(retrievedMovie)
    }

    @Test
    fun `deleteAllMovies removes all movies`() = runTest {
         val movies = listOf(
            MovieEntity("1", "Movie 1", "Overview 1", null, null, "2023-01-01", 7.0, listOf("Genre1")),
            MovieEntity("2", "Movie 2", "Overview 2", null, null, "2023-01-02", 8.0, listOf("Genre2"))
        )
        movieDao.insertMovies(movies)
        movieDao.deleteAllMovies()
        val allMovies = movieDao.getAllMovies().first()
        assertTrue(allMovies.isEmpty())
    }

    @Test
    fun `searchMovies returns matching movies`() = runTest {
        val movies = listOf(
            MovieEntity("1", "Action Movie", "An action film", null, null, "2023-01-01", 7.0, listOf("Action")),
            MovieEntity("2", "Comedy Movie", "A funny film", null, null, "2023-01-02", 8.0, listOf("Comedy")),
            MovieEntity("3", "Another Action Flick", "More action", null, null, "2023-01-03", 7.5, listOf("Action"))
        )
        movieDao.insertMovies(movies)

        val searchResults = movieDao.searchMovies("%Action%").first()
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.any { it.title == "Action Movie" })
        assertTrue(searchResults.any { it.title == "Another Action Flick" })
    }
}
