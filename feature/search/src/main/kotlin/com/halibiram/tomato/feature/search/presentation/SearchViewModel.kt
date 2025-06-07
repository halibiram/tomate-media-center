package com.halibiram.tomato.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Placeholder for a search result item
data class SearchResult(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val type: String // e.g., "Movie", "Series"
)

data class SearchUiState(
    val query: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val recentSearches: List<String> = emptyList(), // Could be more complex objects
    val isLoading: Boolean = false,
    val isFocused: Boolean = false, // To show recent searches or suggestions
    val error: String? = null,
    val noResultsFound: Boolean = false
)

// @HiltViewModel
class SearchViewModel /*@Inject constructor(
    // private val searchRepository: SearchRepository,
    // private val recentSearchesRepository: RecentSearchesRepository
)*/ : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private var searchJob: Job? = null

    init {
        loadRecentSearches()
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery, isLoading = true, noResultsFound = false, error = null)
        searchJob?.cancel() // Cancel previous search if any
        if (newQuery.length < 2) { // Minimum query length to trigger search
            _uiState.value = _uiState.value.copy(isLoading = false, searchResults = emptyList())
            return
        }
        searchJob = viewModelScope.launch {
            delay(500) // Debounce search queries
            try {
                // val results = searchRepository.search(newQuery)
                // Simulate API call
                val simulatedResults = List(5) { index ->
                    SearchResult(
                        id = "$newQuery$index",
                        title = "Result for '$newQuery' #${index + 1}",
                        description = "This is a description for item $index of query $newQuery.",
                        type = if (index % 2 == 0) "Movie" else "Series"
                    )
                }.filter { it.title.contains(newQuery, ignoreCase = true) }

                if (simulatedResults.isEmpty()){
                    _uiState.value = _uiState.value.copy(isLoading = false, searchResults = emptyList(), noResultsFound = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, searchResults = simulatedResults, noResultsFound = false)
                }
                addQueryToRecentSearches(newQuery)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Search failed")
            }
        }
    }

    fun onSearchSubmit(query: String = _uiState.value.query) {
        if (query.isBlank()) return
        _uiState.value = _uiState.value.copy(isFocused = false) // Hide recent searches/suggestions
        onQueryChange(query) // Trigger search if not already triggered by text change
    }

    fun onFocusChange(isFocused: Boolean) {
        _uiState.value = _uiState.value.copy(isFocused = isFocused)
        if (isFocused) {
            loadRecentSearches() // Show recent searches when search bar is focused
        }
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            // val recent = recentSearchesRepository.getRecentSearches()
            _uiState.value = _uiState.value.copy(recentSearches = listOf("Old query 1", "Previous search"))
        }
    }

    private fun addQueryToRecentSearches(query: String) {
        viewModelScope.launch {
            // recentSearchesRepository.addSearchQuery(query)
            // For UI update, could also update the list here or rely on flow from repository
        }
    }

    fun clearSearchQuery() {
        _uiState.value = _uiState.value.copy(query = "", searchResults = emptyList(), isLoading = false, noResultsFound = false)
        onFocusChange(true) // Show recent searches again
    }

    fun onClearRecentSearches() {
        viewModelScope.launch {
            // recentSearchesRepository.clearAllRecentSearches()
            _uiState.value = _uiState.value.copy(recentSearches = emptyList())
        }
    }

    fun onRecentSearchClick(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        onSearchSubmit(query)
    }

    fun onSearchResultClick(resultId: String, resultType: String) {
        // Handle navigation to details, etc.
        // analyticsService.trackEvent("search_result_clicked", mapOf("id" to resultId, "type" to resultType))
    }
}
