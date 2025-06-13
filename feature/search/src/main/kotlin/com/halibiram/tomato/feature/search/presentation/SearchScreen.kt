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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.feature.extensions.api.MovieSourceItem
import com.halibiram.tomato.feature.home.presentation.component.ExtensionMovieCard // Re-use from home for now
import com.halibiram.tomato.feature.search.presentation.component.SearchResultItem // For internal results
import com.halibiram.tomato.ui.theme.TomatoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (movieId: String) -> Unit,
    onNavigateToExtensionDetails: (movieSourceItemId: String, extensionId: String?) -> Unit // Added
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        placeholder = { Text("Search movies & extensions...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearchQuery() }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
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
        // Determine overall content display state
        val showInitialMessage = searchQuery.isBlank() && uiState.recentSearches.isEmpty() && !uiState.isLoadingInternalSearch && !uiState.isLoadingExtensionSearch
        val showRecentSearches = searchQuery.isBlank() && uiState.recentSearches.isNotEmpty() && !uiState.isLoadingInternalSearch && !uiState.isLoadingExtensionSearch
        val showLoading = (uiState.isLoadingInternalSearch || uiState.isLoadingExtensionSearch) && (uiState.internalSearchResults.isEmpty() && uiState.extensionSearchResults.isEmpty())
        val showNoResultsOverall = searchQuery.isNotEmpty() &&
                                   !uiState.isLoadingInternalSearch && uiState.internalSearchResults.isEmpty() && uiState.noInternalResultsFound &&
                                   !uiState.isLoadingExtensionSearch && uiState.extensionSearchResults.isEmpty() && uiState.noExtensionResultsFound &&
                                   uiState.errorInternalSearch == null && uiState.errorExtensionSearch == null

        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (showLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (showInitialMessage) {
                 Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Start typing to search for movies and from extensions.")
                }
            } else if (showRecentSearches) {
                 RecentSearchesList(
                    recentSearches = uiState.recentSearches,
                    onRecentSearchClick = { query ->
                        viewModel.onRecentSearchClicked(query)
                        keyboardController?.hide()
                    },
                    onClearRecentSearches = viewModel::onClearRecentSearches
                )
            } else if (showNoResultsOverall) {
                 Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No results found for \"$searchQuery\".")
                }
            }
            else {
                // Use a single LazyColumn for both sections
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp), // Only vertical padding for LazyColumn
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Internal Search Results Section
                    if (uiState.errorInternalSearch != null) {
                        item {
                            ErrorItem("Internal Search Error: ${uiState.errorInternalSearch}", onRetry = { viewModel.onSearchQueryChanged(searchQuery) /* Re-trigger */ })
                        }
                    } else if (uiState.internalSearchResults.isNotEmpty()) {
                        item { SectionTitle("Results from Library") }
                        items(uiState.internalSearchResults, key = { "internal_${it.id}" }) { movie ->
                            SearchResultItem(
                                movie = movie,
                                onItemClick = { movieId ->
                                    viewModel.onSearchResultClick(movieId)
                                    onNavigateToDetails(movieId)
                                },
                                modifier = Modifier.padding(horizontal = 16.dp) // Add horizontal padding to items
                            )
                        }
                    } else if (searchQuery.isNotEmpty() && uiState.noInternalResultsFound && !uiState.isLoadingInternalSearch) {
                        item { NoResultsForItem("No library results for \"$searchQuery\".") }
                    }

                    // Spacer between sections if both have content or potential content
                    if ((uiState.internalSearchResults.isNotEmpty() || uiState.errorInternalSearch != null || (searchQuery.isNotEmpty() && uiState.noInternalResultsFound)) &&
                        (uiState.extensionSearchResults.isNotEmpty() || uiState.errorExtensionSearch != null || (searchQuery.isNotEmpty() && uiState.noExtensionResultsFound))) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    // Extension Search Results Section
                    if (uiState.errorExtensionSearch != null) {
                        item {
                            ErrorItem("Extension Search Error: ${uiState.errorExtensionSearch}", onRetry = { viewModel.onSearchQueryChanged(searchQuery) })
                        }
                    } else if (uiState.extensionSearchResults.isNotEmpty()) {
                        item { SectionTitle("Results from Extensions") }
                        items(uiState.extensionSearchResults, key = { "ext_${it.id}_${it.title}" }) { movieItem ->
                            ExtensionMovieCard( // Using ExtensionMovieCard
                                movieItem = movieItem,
                                onClick = {
                                    viewModel.onExtensionSearchResultClick(movieItem)
                                    // Extension ID might be part of MovieSourceItem or resolved by VM/Use Case
                                    onNavigateToExtensionDetails(movieItem.id, null /* TODO: extensionId */)
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else if (searchQuery.isNotEmpty() && uiState.noExtensionResultsFound && !uiState.isLoadingExtensionSearch) {
                         item { NoResultsForItem("No extension results for \"$searchQuery\".") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ErrorItem(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun NoResultsForItem(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(message)
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
                TextButton(onClick = onClearRecentSearches) { Text("Clear All") }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(recentSearches) { query ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onRecentSearchClick(query) }.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = query)
                }
                HorizontalDivider()
            }
        }
    }
}

// --- Previews ---
@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_BothResults() {
    val mockViewModel: SearchViewModel = mockk(relaxed = true)
    val internalResults = listOf(Movie("1", "Internal Movie", "Desc", null, "2023", emptyList(), 7.0))
    val extensionResults = listOf(MovieSourceItem("e1", "Extension Movie", null, "2024"))
    every { mockViewModel.uiState } returns MutableStateFlow(
        SearchUiState(
            searchQuery = "test",
            internalSearchResults = internalResults,
            extensionSearchResults = extensionResults
        )
    )
    every { mockViewModel.searchQuery } returns MutableStateFlow("test")

    TomatoTheme {
        SearchScreen(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onNavigateToDetails = { _ -> },
            onNavigateToExtensionDetails = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_OnlyInternalResults() {
    val mockViewModel: SearchViewModel = mockk(relaxed = true)
    val internalResults = listOf(Movie("1", "Internal Movie Only", "Desc", null, "2023", emptyList(), 7.0))
    every { mockViewModel.uiState } returns MutableStateFlow(
        SearchUiState(
            searchQuery = "test",
            internalSearchResults = internalResults,
            extensionSearchResults = emptyList(),
            noExtensionResultsFound = true // Important for showing "no extension results"
        )
    )
    every { mockViewModel.searchQuery } returns MutableStateFlow("test")

    TomatoTheme {
        SearchScreen(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onNavigateToDetails = { _ -> },
            onNavigateToExtensionDetails = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_NoResultsOverall() {
    val mockViewModel: SearchViewModel = mockk(relaxed = true)
    every { mockViewModel.uiState } returns MutableStateFlow(
        SearchUiState(
            searchQuery = "nothing",
            noInternalResultsFound = true,
            noExtensionResultsFound = true
        )
    )
    every { mockViewModel.searchQuery } returns MutableStateFlow("nothing")

    TomatoTheme {
        SearchScreen(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onNavigateToDetails = { _ -> },
            onNavigateToExtensionDetails = { _, _ -> }
        )
    }
}
