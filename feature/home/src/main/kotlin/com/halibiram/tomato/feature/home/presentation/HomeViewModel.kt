package com.halibiram.tomato.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.core.common.result.Result
import com.halibiram.tomato.core.common.result.onFailure // Custom extension if you have one
import com.halibiram.tomato.core.common.result.onSuccess
import com.halibiram.tomato.domain.model.Movie // Correct domain model
import com.halibiram.tomato.domain.usecase.movie.GetMoviesUseCase
import com.halibiram.tomato.domain.usecase.movie.MovieListType
import dagger.hilt.android.lifecycle.HiltViewModel // For Hilt injection
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    // Popular Movies Section
    val isLoadingPopular: Boolean = false,
    val popularMovies: List<Movie> = emptyList(),
    val errorPopular: String? = null,

    // Trending Movies Section
    val isLoadingTrending: Boolean = false,
    val trendingMovies: List<Movie> = emptyList(),
    val errorTrending: String? = null,

    // Category Movies Section (Example: could be a map or dynamic list of categories)
    val isLoadingCategory: Boolean = false,
    val categoryMovies: Map<String, List<Movie>> = emptyMap(), // Key: categoryId/name, Value: List of movies
    val errorCategory: String? = null,

    // General error for the screen if needed
    val screenError: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMoviesUseCase: GetMoviesUseCase
    // Add other use cases for different content types (e.g., GetSeriesUseCase) if needed for home screen
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Initial data load for all sections
        fetchPopularMovies()
        fetchTrendingMovies()
        // Example: Fetch movies for a default category or a list of categories
        // fetchMoviesForCategory("action_movies_placeholder_id", "Action") // Example
    }

    fun fetchPopularMovies(page: Int = 1) {
        viewModelScope.launch {
            getMoviesUseCase(type = MovieListType.POPULAR, page = page)
                .onStart { _uiState.update { it.copy(isLoadingPopular = true, errorPopular = null) } }
                .catch { e -> // Catch unexpected errors from the flow itself
                    _uiState.update { it.copy(isLoadingPopular = false, errorPopular = e.localizedMessage ?: "Unknown error") }
                }
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoadingPopular = true) }
                        }
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoadingPopular = false,
                                    popularMovies = result.data, // Assuming use case returns full list, not paginated for now in VM
                                    errorPopular = null
                                )
                            }
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoadingPopular = false,
                                    errorPopular = result.exception.message ?: "Failed to load popular movies"
                                )
                            }
                        }
                    }
                }
        }
    }

    fun fetchTrendingMovies(timeWindow: String = "day") {
        viewModelScope.launch {
            getMoviesUseCase(type = MovieListType.TRENDING, timeWindow = timeWindow)
                .onStart { _uiState.update { it.copy(isLoadingTrending = true, errorTrending = null) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoadingTrending = false, errorTrending = e.localizedMessage ?: "Unknown error") }
                }
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoadingTrending = true) }
                        }
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoadingTrending = false,
                                    trendingMovies = result.data,
                                    errorTrending = null
                                )
                            }
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoadingTrending = false,
                                    errorTrending = result.exception.message ?: "Failed to load trending movies"
                                )
                            }
                        }
                    }
                }
        }
    }

    fun fetchMoviesForCategory(categoryId: String, categoryDisplayName: String) {
        viewModelScope.launch {
            getMoviesUseCase(type = MovieListType.BY_CATEGORY, categoryId = categoryId)
                .onStart { _uiState.update { it.copy(isLoadingCategory = true, errorCategory = null) } } // Might need per-category loading state
                .catch { e ->
                    _uiState.update { it.copy(isLoadingCategory = false, errorCategory = e.localizedMessage ?: "Unknown error for category $categoryDisplayName") }
                }
                .collect { result ->
                     when (result) {
                        is Result.Loading -> {
                             _uiState.update { state ->
                                // If handling multiple categories, update specific one or use a general category loading flag
                                state.copy(isLoadingCategory = true)
                            }
                        }
                        is Result.Success -> {
                            _uiState.update { state ->
                                val updatedCategories = state.categoryMovies.toMutableMap()
                                updatedCategories[categoryDisplayName] = result.data
                                state.copy(
                                    isLoadingCategory = false, // Or manage per-category loading
                                    categoryMovies = updatedCategories,
                                    errorCategory = null
                                )
                            }
                        }
                        is Result.Error -> {
                            _uiState.update { state ->
                                state.copy(
                                    isLoadingCategory = false,
                                    errorCategory = result.exception.message ?: "Failed to load $categoryDisplayName movies"
                                    // Optionally, clear movies for this category on error:
                                    // categoryMovies = state.categoryMovies.toMutableMap().apply { remove(categoryDisplayName) }
                                )
                            }
                        }
                    }
                }
        }
    }


    fun onMovieItemClick(movieId: String) {
        // Handle navigation to movie details screen
        // This might involve setting a navigation event (e.g., using a SharedFlow or Channel)
        // For now, just a placeholder.
        // Log.d("HomeViewModel", "Movie clicked: $movieId")
    }

    fun onRetrySection(sectionType: MovieListType, categoryId: String? = null, categoryDisplayName: String? = null) {
        when (sectionType) {
            MovieListType.POPULAR -> fetchPopularMovies()
            MovieListType.TRENDING -> fetchTrendingMovies()
            MovieListType.BY_CATEGORY -> {
                if (categoryId != null && categoryDisplayName != null) {
                    fetchMoviesForCategory(categoryId, categoryDisplayName)
                } else {
                    // Log error or update UI state about missing category info for retry
                    _uiState.update { it.copy(screenError = "Cannot retry category: Missing ID or name") }
                }
            }
        }
    }
}
