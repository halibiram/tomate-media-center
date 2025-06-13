package com.halibiram.tomato.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.core.common.result.Result
// import com.halibiram.tomato.core.common.result.onFailure // Custom extension if you have one
// import com.halibiram.tomato.core.common.result.onSuccess
import com.halibiram.tomato.domain.model.Movie // Correct domain model
import com.halibiram.tomato.domain.usecase.movie.GetExtensionMoviesUseCase // New UseCase
import com.halibiram.tomato.domain.usecase.movie.GetMoviesUseCase
import com.halibiram.tomato.domain.usecase.movie.MovieListType
import com.halibiram.tomato.feature.extensions.api.MovieSourceItem // For extension movies
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    // Popular Movies Section (from main repository)
    val isLoadingPopular: Boolean = false,
    val popularMovies: List<Movie> = emptyList(),
    val errorPopular: String? = null,

    // Trending Movies Section (from main repository)
    val isLoadingTrending: Boolean = false,
    val trendingMovies: List<Movie> = emptyList(),
    val errorTrending: String? = null,

    // Category Movies Section (from main repository)
    val isLoadingCategory: Boolean = false,
    val categoryMovies: Map<String, List<Movie>> = emptyMap(),
    val errorCategory: String? = null,

    // Extension Popular Movies Section
    val isLoadingExtensionPopular: Boolean = false,
    val extensionPopularMovies: List<MovieSourceItem> = emptyList(),
    val errorExtensionPopular: String? = null,

    val screenError: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val getExtensionMoviesUseCase: GetExtensionMoviesUseCase // Injected new use case
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchPopularMovies()
        fetchTrendingMovies()
        fetchExtensionPopularMovies() // Fetch extension movies on init
        // fetchMoviesForCategory("action_movies_placeholder_id", "Action")
    }

    fun fetchPopularMovies(page: Int = 1) {
        viewModelScope.launch {
            getMoviesUseCase(type = MovieListType.POPULAR, page = page)
                .onStart { _uiState.update { it.copy(isLoadingPopular = true, errorPopular = null) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoadingPopular = false, errorPopular = e.localizedMessage ?: "Unknown error") }
                }
                .collect { result ->
                    when (result) {
                        is Result.Loading -> _uiState.update { it.copy(isLoadingPopular = true) }
                        is Result.Success -> _uiState.update {
                            it.copy(isLoadingPopular = false, popularMovies = result.data, errorPopular = null)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(isLoadingPopular = false, errorPopular = result.exception.message ?: "Failed to load popular movies")
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
                        is Result.Loading -> _uiState.update { it.copy(isLoadingTrending = true) }
                        is Result.Success -> _uiState.update {
                            it.copy(isLoadingTrending = false, trendingMovies = result.data, errorTrending = null)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(isLoadingTrending = false, errorTrending = result.exception.message ?: "Failed to load trending movies")
                        }
                    }
                }
        }
    }

    fun fetchMoviesForCategory(categoryId: String, categoryDisplayName: String) {
        viewModelScope.launch {
            getMoviesUseCase(type = MovieListType.BY_CATEGORY, categoryId = categoryId)
                .onStart { _uiState.update { it.copy(isLoadingCategory = true, errorCategory = null) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoadingCategory = false, errorCategory = e.localizedMessage ?: "Unknown error for category $categoryDisplayName") }
                }
                .collect { result ->
                     when (result) {
                        is Result.Loading -> _uiState.update { state ->
                                state.copy(isLoadingCategory = true)
                            }
                        is Result.Success -> _uiState.update { state ->
                                val updatedCategories = state.categoryMovies.toMutableMap()
                                updatedCategories[categoryDisplayName] = result.data
                                state.copy(isLoadingCategory = false, categoryMovies = updatedCategories, errorCategory = null)
                            }
                        is Result.Error -> _uiState.update { state ->
                                state.copy(isLoadingCategory = false, errorCategory = result.exception.message ?: "Failed to load $categoryDisplayName movies")
                            }
                    }
                }
        }
    }

    fun fetchExtensionPopularMovies(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingExtensionPopular = true, errorExtensionPopular = null) }
            // This use case is suspend, not Flow<Result> directly in this example
            // If it were Flow<Result>, the collection would be similar to others.
            // For a suspend fun returning Result:
            val result = getExtensionMoviesUseCase.getPopularMovies(page)
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingExtensionPopular = false,
                            extensionPopularMovies = result.data,
                            errorExtensionPopular = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingExtensionPopular = false,
                            errorExtensionPopular = result.exception.message ?: "Error loading popular movies from extensions"
                        )
                    }
                }
                is Result.Loading -> {
                     // If getExtensionMoviesUseCase.getPopularMovies was a Flow and emitted Loading
                    _uiState.update { it.copy(isLoadingExtensionPopular = true) }
                }
            }
        }
    }

    fun onMovieItemClick(movieId: String) {
        // Placeholder for navigation
    }

    fun onExtensionMovieItemClick(movieSourceItem: MovieSourceItem) {
        // Placeholder for navigation or detail view for extension item
        // Log.d("HomeViewModel", "Extension movie clicked: ${movieSourceItem.title}")
    }


    fun onRetrySection(sectionType: MovieListType, categoryId: String? = null, categoryDisplayName: String? = null) {
        when (sectionType) {
            MovieListType.POPULAR -> fetchPopularMovies()
            MovieListType.TRENDING -> fetchTrendingMovies()
            MovieListType.BY_CATEGORY -> {
                if (categoryId != null && categoryDisplayName != null) {
                    fetchMoviesForCategory(categoryId, categoryDisplayName)
                } else {
                    _uiState.update { it.copy(screenError = "Cannot retry category: Missing ID or name") }
                }
            }
        }
    }

    fun onRetryExtensionPopular() {
        fetchExtensionPopularMovies()
    }
}
