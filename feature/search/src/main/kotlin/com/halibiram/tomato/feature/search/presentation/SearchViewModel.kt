package com.halibiram.tomato.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.core.common.result.Result
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.usecase.movie.GetExtensionMoviesUseCase // New UseCase
import com.halibiram.tomato.domain.usecase.movie.SearchMoviesUseCase
import com.halibiram.tomato.feature.extensions.api.MovieSourceItem // For extension results
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val searchQuery: String = "",
    // Internal Search
    val isLoadingInternalSearch: Boolean = false, // Renamed from isLoading
    val internalSearchResults: List<Movie> = emptyList(), // Renamed from searchResults
    val errorInternalSearch: String? = null, // Renamed from error
    val noInternalResultsFound: Boolean = false, // Renamed from noResultsFound

    // Extension Search
    val isLoadingExtensionSearch: Boolean = false,
    val extensionSearchResults: List<MovieSourceItem> = emptyList(),
    val errorExtensionSearch: String? = null,
    val noExtensionResultsFound: Boolean = false,

    val recentSearches: List<String> = emptyList()
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMoviesUseCase: SearchMoviesUseCase, // For internal search
    private val getExtensionMoviesUseCase: GetExtensionMoviesUseCase // For extension search
    // private val recentSearchesRepository: RecentSearchesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var internalSearchJob: Job? = null
    private var extensionSearchJob: Job? = null

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.length < 2) {
                        _uiState.update { it.copy(
                            internalSearchResults = emptyList(),
                            extensionSearchResults = emptyList(),
                            isLoadingInternalSearch = false,
                            isLoadingExtensionSearch = false,
                            noInternalResultsFound = false,
                            noExtensionResultsFound = false,
                            errorInternalSearch = null,
                            errorExtensionSearch = null
                        )}
                    } else {
                        performInternalSearch(query)
                        performExtensionSearch(query)
                    }
                }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.isBlank()) { // Clear results immediately if query becomes blank
             _uiState.update { it.copy(
                internalSearchResults = emptyList(),
                extensionSearchResults = emptyList(),
                isLoadingInternalSearch = false,
                isLoadingExtensionSearch = false,
                noInternalResultsFound = false,
                noExtensionResultsFound = false,
                errorInternalSearch = null,
                errorExtensionSearch = null,
                searchQuery = "" // also update uiState's searchQuery
            )}
        } else {
            // Update searchQuery in UiState immediately for responsiveness of input field
            _uiState.update { it.copy(searchQuery = newQuery) }
        }
    }

    private fun performInternalSearch(query: String, page: Int = 1) {
        internalSearchJob?.cancel()
        internalSearchJob = viewModelScope.launch {
            searchMoviesUseCase(query = query, page = page)
                .onStart { _uiState.update { it.copy(isLoadingInternalSearch = true, noInternalResultsFound = false, errorInternalSearch = null) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoadingInternalSearch = false, errorInternalSearch = e.localizedMessage ?: "Unknown internal search error") }
                }
                .collect { result ->
                    when (result) {
                        is Result.Loading -> _uiState.update { it.copy(isLoadingInternalSearch = true) }
                        is Result.Success -> {
                            val movies = result.data
                            _uiState.update {
                                it.copy(
                                    isLoadingInternalSearch = false,
                                    internalSearchResults = movies,
                                    noInternalResultsFound = movies.isEmpty() && query.isNotEmpty(),
                                    errorInternalSearch = null
                                )
                            }
                        }
                        is Result.Error -> _uiState.update {
                                it.copy(
                                    isLoadingInternalSearch = false,
                                    errorInternalSearch = result.exception.message ?: "Failed internal search",
                                    internalSearchResults = emptyList()
                                )
                            }
                    }
                }
        }
    }

    private fun performExtensionSearch(query: String, page: Int = 1) {
        extensionSearchJob?.cancel()
        extensionSearchJob = viewModelScope.launch {
            // Assuming getExtensionMoviesUseCase.searchMovies is a suspend fun returning Result
            _uiState.update { it.copy(isLoadingExtensionSearch = true, noExtensionResultsFound = false, errorExtensionSearch = null) }

            when (val result = getExtensionMoviesUseCase.searchMovies(query = query, page = page)) {
                is Result.Success -> {
                    val movies = result.data
                    _uiState.update {
                        it.copy(
                            isLoadingExtensionSearch = false,
                            extensionSearchResults = movies,
                            noExtensionResultsFound = movies.isEmpty() && query.isNotEmpty(),
                            errorExtensionSearch = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingExtensionSearch = false,
                            errorExtensionSearch = result.exception.message ?: "Failed extension search",
                            extensionSearchResults = emptyList()
                        )
                    }
                }
                is Result.Loading -> { // This case won't be hit if use case is suspend fun returning Result directly
                     _uiState.update { it.copy(isLoadingExtensionSearch = true) }
                }
            }
        }
    }

    fun clearSearchQuery() {
        _searchQuery.value = "" // This will trigger the collectLatest in init to clear states
        // Additional explicit clear just in case:
        _uiState.update { it.copy(
            searchQuery = "",
            internalSearchResults = emptyList(),
            extensionSearchResults = emptyList(),
            isLoadingInternalSearch = false,
            isLoadingExtensionSearch = false,
            noInternalResultsFound = false,
            noExtensionResultsFound = false,
            errorInternalSearch = null,
            errorExtensionSearch = null)
        }
        // loadRecentSearches()
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            _uiState.update { it.copy(recentSearches = listOf("Old query 1", "Previous search example")) }
        }
    }

    fun onClearRecentSearches() {
        viewModelScope.launch {
            _uiState.update { it.copy(recentSearches = emptyList()) }
        }
    }

    fun onRecentSearchClicked(query: String) {
        _searchQuery.value = query
    }

    fun onSearchResultClick(movieId: String) {
        // For internal search results
    }

    fun onExtensionSearchResultClick(item: MovieSourceItem) {
        // For extension search results
    }
}
