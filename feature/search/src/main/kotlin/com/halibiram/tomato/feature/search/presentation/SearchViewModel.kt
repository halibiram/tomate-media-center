package com.halibiram.tomato.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.core.common.result.Result // Correct Result class
import com.halibiram.tomato.domain.model.Movie // Using Movie domain model for search results
import com.halibiram.tomato.domain.usecase.movie.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Re-defining SearchResult for UI if it's different from Domain Movie model.
// For this task, SearchResults will be List<Movie>.
// data class SearchResultItemUi( ... )

data class SearchUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val searchResults: List<Movie> = emptyList(), // Changed from generic SearchResult to Movie
    val error: String? = null,
    val noResultsFound: Boolean = false, // To distinguish empty list from "no results for query"
    val recentSearches: List<String> = emptyList() // Keep recent searches functionality
)

@OptIn(FlowPreview::class) // For debounce
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMoviesUseCase: SearchMoviesUseCase
    // private val recentSearchesRepository: RecentSearchesRepository // For managing recent searches
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        //  loadRecentSearches() // Load initial recent searches

        viewModelScope.launch {
            _searchQuery
                .debounce(500) // Debounce input: wait for 500ms of no new input
                .distinctUntilChanged() // Only search if query text actually changed
                .collectLatest { query -> // Use collectLatest to cancel previous searches if new query comes in
                    if (query.length < 2) { // Minimum query length to trigger search
                        _uiState.update { it.copy(searchResults = emptyList(), isLoading = false, noResultsFound = false, error = null) }
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        // isLoading will be handled by performSearch via collectLatest
    }

    private fun performSearch(query: String, page: Int = 1) { // Added page for future pagination
        searchJob?.cancel() // Cancel any existing search job
        searchJob = viewModelScope.launch {
            searchMoviesUseCase(query = query, page = page)
                .onStart { _uiState.update { it.copy(isLoading = true, noResultsFound = false, error = null, searchQuery = query) } }
                .catch { e -> // Catch unexpected errors from the flow itself
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Unknown search error", noResultsFound = false) }
                }
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Result.Success -> {
                            val movies = result.data
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    searchResults = movies,
                                    noResultsFound = movies.isEmpty() && query.isNotEmpty(),
                                    error = null
                                )
                            }
                            if (movies.isNotEmpty()) {
                                // addQueryToRecentSearches(query)
                            }
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.exception.message ?: "Failed to perform search",
                                    searchResults = emptyList(), // Clear results on error
                                    noResultsFound = false
                                )
                            }
                        }
                    }
                }
        }
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
        _uiState.update { it.copy(searchResults = emptyList(), isLoading = false, noResultsFound = false, error = null, searchQuery = "") }
        // loadRecentSearches() // Optionally show recent searches again
    }

    // --- Recent Searches (Placeholder, requires a repository) ---
    private fun loadRecentSearches() {
        viewModelScope.launch {
            // val recent = recentSearchesRepository.getRecentSearches().first()
            // _uiState.update { it.copy(recentSearches = recent) }
            _uiState.update { it.copy(recentSearches = listOf("Old query 1", "Previous search example")) } // Mock
        }
    }

    private fun addQueryToRecentSearches(query: String) {
        viewModelScope.launch {
            // recentSearchesRepository.addSearchQuery(query)
            // loadRecentSearches() // Refresh
        }
    }

    fun onClearRecentSearches() {
        viewModelScope.launch {
            // recentSearchesRepository.clearAllRecentSearches()
            _uiState.update { it.copy(recentSearches = emptyList()) }
        }
    }

    fun onRecentSearchClicked(query: String) {
        _searchQuery.value = query // This will trigger search due to collectLatest on _searchQuery
        // performSearch(query) // No longer needed here due to automatic collection
    }

    fun onSearchResultClick(movieId: String) {
        // Handle navigation to movie details
        // Log.d("SearchViewModel", "Movie clicked: $movieId")
    }
}
