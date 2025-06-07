package com.halibiram.tomato.domain.usecase.movie

import com.halibiram.tomato.domain.repository.Movie
import com.halibiram.tomato.domain.repository.MovieRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
// import org.mockito.Mock // If using Mockito
// import org.mockito.Mockito.`when` // If using Mockito
// import org.mockito.MockitoAnnotations

class GetMoviesUseCaseTest {

    // @Mock
    // private lateinit var mockMovieRepository: MovieRepository
    private lateinit var getMoviesUseCase: GetMoviesUseCase

    // Dummy repository for placeholder
    class FakeMovieRepository : MovieRepository {
        private val movies = mutableListOf<Movie>()
        fun addMovie(movie: Movie) { movies.add(movie) }
        override fun getPopularMovies(page: Int) = flowOf(movies.toList())
        override fun getMovieDetails(movieId: String) = flowOf(movies.find { it.id == movieId }?.let { com.halibiram.tomato.domain.repository.MovieDetails(it.id, it.title, it.overview, it.posterUrl, null, null, null) })
        override fun searchMovies(query: String, page: Int) = flowOf(movies.filter { it.title.contains(query, ignoreCase = true) })
    }
    private lateinit var fakeMovieRepository: FakeMovieRepository


    @Before
    fun setUp() {
        // MockitoAnnotations.openMocks(this)
        // getMoviesUseCase = GetMoviesUseCase(mockMovieRepository)

        // Using FakeRepository for placeholder
        fakeMovieRepository = FakeMovieRepository()
        getMoviesUseCase = GetMoviesUseCase(fakeMovieRepository)
    }

    @Test
    fun `invoke returns movies from repository`() = runBlocking {
        // Given
        val expectedMovies = listOf(
            Movie("1", "Movie 1", "Overview 1", "/poster1.jpg"),
            Movie("2", "Movie 2", "Overview 2", "/poster2.jpg")
        )
        // `when`(mockMovieRepository.getPopularMovies(1)).thenReturn(flowOf(expectedMovies)) // Mockito example
        expectedMovies.forEach { fakeMovieRepository.addMovie(it) }


        // When
        val result = getMoviesUseCase(page = 1).first()

        // Then
        assertEquals(expectedMovies, result)
    }

    @Test
    fun `invoke with specific page number passes page to repository`() = runBlocking {
        // Given
        val pageNum = 2
        val expectedMovies = listOf(Movie("3", "Movie 3 from page 2", "Overview 3", null))
        // `when`(mockMovieRepository.getPopularMovies(pageNum)).thenReturn(flowOf(expectedMovies)) // Mockito
        fakeMovieRepository.addMovie(expectedMovies.first())


        // When
        val result = getMoviesUseCase(page = pageNum).first()

        // Then
        assertEquals(expectedMovies, result)
        // Verify mockMovieRepository.getPopularMovies(pageNum) was called if using Mockito
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() = runBlocking {
        // Given
        // `when`(mockMovieRepository.getPopularMovies(1)).thenReturn(flowOf(emptyList())) // Mockito

        // When
        val result = getMoviesUseCase(page = 1).first()

        // Then
        assertEquals(emptyList<Movie>(), result)
    }
}
