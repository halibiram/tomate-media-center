package com.halibiram.tomato.feature.search.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
// import androidx.compose.material.icons.filled.Search // Not explicitly used in TextField here
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
// import androidx.compose.ui.focus.onFocusChanged // Not explicitly used in this version of SearchBar
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halibiram.tomato.feature.search.presentation.component.SearchResultItem
import com.halibiram.tomato.ui.theme.TomatoTheme // Assuming TomatoTheme is correctly pathed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (movieId: String) -> Unit // Changed from mediaId, mediaType
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle() // Collect searchQuery separately
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Request focus on launch
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        // keyboardController?.show() // Optionally show keyboard
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("Search movies...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            // ViewModel search is triggered by debounce on query change,
                            // but we can hide keyboard here.
                            keyboardController?.hide()
                        }),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearchQuery() }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors( // Updated for M3
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary, // Or Transparent if no underline desired
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) // Or Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                             Spacer(modifier = Modifier.height(8.dp))
                             Button(onClick = { viewModel.onSearchQueryChanged(searchQuery) /* Re-trigger search with current query */ }) {
                                 Text("Retry")
                             }
                        }
                    }
                }
                uiState.searchResults.isNotEmpty() -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.searchResults, key = { it.id }) { movie ->
                            SearchResultItem(
                                movie = movie,
                                onItemClick = { movieId ->
                                    viewModel.onSearchResultClick(movieId) // For VM logic if any
                                    onNavigateToDetails(movieId)
                                }
                            )
                        }
                        // Add pagination loading indicator here if implementing pagination
                    }
                }
                searchQuery.isNotEmpty() && uiState.noResultsFound && !uiState.isLoading -> {
                     Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No results found for \"$searchQuery\".")
                    }
                }
                searchQuery.isEmpty() && uiState.recentSearches.isEmpty() && !uiState.isLoading -> {
                     Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Start typing to search for movies.")
                    }
                }
                searchQuery.isEmpty() && uiState.recentSearches.isNotEmpty() && !uiState.isLoading -> {
                    RecentSearchesList(
                        recentSearches = uiState.recentSearches,
                        onRecentSearchClick = { query ->
                            viewModel.onRecentSearchClicked(query) // Updates searchQuery, search triggers automatically
                            keyboardController?.hide()
                        },
                        onClearRecentSearches = viewModel::onClearRecentSearches
                    )
                }

            }
        }
    }
}

@Composable
private fun RecentSearchesList(
    recentSearches: List<String>,
    onRecentSearchClick: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Searches", style = MaterialTheme.typography.titleMedium)
            if (recentSearches.isNotEmpty()) {
                TextButton(onClick = onClearRecentSearches) {
                    Text("Clear All")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (recentSearches.isEmpty()) {
            // This case might be handled by the main screen's "Start typing..." message
            // Text("No recent searches.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(recentSearches) { query ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRecentSearchClick(query) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon(Icons.Default.History, contentDescription = "Recent search", modifier = Modifier.padding(end = 8.dp)) // Optional icon
                        Text(text = query)
                    }
                    Divider()
                }
            }
        }
    }
}

// --- Previews ---
// For Hilt ViewModels in previews, you often need a way to provide a fake/mock instance.
// Or, the ViewModel constructor should have defaults for dependencies for previewing.

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_InitialEmpty() {
    val mockViewModel = SearchViewModel(mockk(relaxed = true)) // Mock use case
    every { mockViewModel.uiState } returns MutableStateFlow(SearchUiState(searchQuery = "", recentSearches = emptyList()))
    every { mockViewModel.searchQuery } returns MutableStateFlow("")

    TomatoTheme {
        SearchScreen(viewModel = mockViewModel, onNavigateBack = {}, onNavigateToDetails = { _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_WithRecentSearches() {
    val mockViewModel = SearchViewModel(mockk(relaxed = true))
    every { mockViewModel.uiState } returns MutableStateFlow(SearchUiState(searchQuery = "", recentSearches = listOf("Inception", "Interstellar")))
    every { mockViewModel.searchQuery } returns MutableStateFlow("")
    TomatoTheme {
        SearchScreen(viewModel = mockViewModel, onNavigateBack = {}, onNavigateToDetails = { _ -> })
    }
}


@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_Loading() {
    val mockViewModel = SearchViewModel(mockk(relaxed = true))
    every { mockViewModel.uiState } returns MutableStateFlow(SearchUiState(isLoading = true, searchQuery = "Loading..."))
     every { mockViewModel.searchQuery } returns MutableStateFlow("Loading...")
    TomatoTheme {
        SearchScreen(viewModel = mockViewModel, onNavigateBack = {}, onNavigateToDetails = { _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_WithResults() {
    val results = listOf(
        com.halibiram.tomato.domain.model.Movie("1", "Result Movie 1", "Desc 1", null, "2023", emptyList(), 7.0),
        com.halibiram.tomato.domain.model.Movie("2", "Result Movie 2", "Desc 2", null, "2022", emptyList(), 8.0)
    )
    val mockViewModel = SearchViewModel(mockk(relaxed = true))
    every { mockViewModel.uiState } returns MutableStateFlow(SearchUiState(searchResults = results, searchQuery = "Results"))
    every { mockViewModel.searchQuery } returns MutableStateFlow("Results")

    TomatoTheme {
        SearchScreen(viewModel = mockViewModel, onNavigateBack = {}, onNavigateToDetails = { _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_NoResults() {
    val mockViewModel = SearchViewModel(mockk(relaxed = true))
    every { mockViewModel.uiState } returns MutableStateFlow(SearchUiState(noResultsFound = true, searchQuery = "NoResultsQuery"))
    every { mockViewModel.searchQuery } returns MutableStateFlow("NoResultsQuery")
    TomatoTheme {
        SearchScreen(viewModel = mockViewModel, onNavigateBack = {}, onNavigateToDetails = { _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_Error() {
     val mockViewModel = SearchViewModel(mockk(relaxed = true))
    every { mockViewModel.uiState } returns MutableStateFlow(SearchUiState(error = "Network failed horribly", searchQuery = "ErrorQuery"))
    every { mockViewModel.searchQuery } returns MutableStateFlow("ErrorQuery")
    TomatoTheme {
        SearchScreen(viewModel = mockViewModel, onNavigateBack = {}, onNavigateToDetails = { _ -> })
    }
}
