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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.feature.search.presentation.component.SearchResultItem
import com.halibiram.tomato.ui.theme.TomatoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    // viewModel: SearchViewModel = hiltViewModel(), // With Hilt
    viewModel: SearchViewModel, // Pass for preview or non-Hilt
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (mediaId: String, mediaType: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Request focus on launch
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            SearchBar(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = {
                    keyboardController?.hide()
                    viewModel.onSearchSubmit()
                },
                onClear = viewModel::clearSearchQuery,
                onFocusChange = viewModel::onFocusChange,
                focusRequester = focusRequester,
                onNavigateBack = onNavigateBack,
                isFocused = uiState.isFocused
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.isFocused && uiState.query.isEmpty() && uiState.recentSearches.isNotEmpty()) {
                RecentSearchesList(
                    recentSearches = uiState.recentSearches,
                    onRecentSearchClick = { query ->
                        viewModel.onRecentSearchClick(query)
                        keyboardController?.hide()
                    },
                    onClearRecentSearches = viewModel::onClearRecentSearches
                )
            } else if (uiState.searchResults.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.searchResults, key = { it.id }) { result ->
                        SearchResultItem(
                            result = result,
                            onItemClick = { id, type ->
                                viewModel.onSearchResultClick(id, type)
                                onNavigateToDetails(id, type)
                            }
                        )
                    }
                }
            } else if (uiState.query.isNotEmpty() && uiState.noResultsFound) {
                 Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No results found for \"${uiState.query}\".")
                }
            } else if (!uiState.isFocused && uiState.query.isEmpty()) {
                 Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Start typing to search.")
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    onNavigateBack: () -> Unit,
    isFocused: Boolean // To control visibility of clear button or other elements
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        onFocusChange(focusState.isFocused)
                    },
                placeholder = { Text("Search movies, series...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Navigate back")
            }
        },
        actions = {
            IconButton(onClick = onSearch, enabled = query.isNotBlank()) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
    )
}

@Composable
private fun RecentSearchesList(
    recentSearches: List<String>,
    onRecentSearchClick: (String) -> Unit,
    onClearRecentSearches: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
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
            Text("No recent searches.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(recentSearches) { query ->
                    Text(
                        text = query,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRecentSearchClick(query) }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_Empty() {
    val emptyVM = SearchViewModel() // Assuming default state is empty query, not loading, no error
    TomatoTheme {
        SearchScreen(viewModel = emptyVM, onNavigateBack = {}, onNavigateToDetails = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_WithResults() {
    val vmWithResults = SearchViewModel().apply {
        // This direct state manipulation is for preview convenience.
        // _uiState.value = SearchUiState(
        // query = "Test",
        // searchResults = listOf(SearchResult("1", "Test Movie", "Desc", type = "Movie")),
        // isLoading = false
        // )
    }
    TomatoTheme {
        SearchScreen(viewModel = vmWithResults, onNavigateBack = {}, onNavigateToDetails = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview_RecentSearches() {
    val vmWithRecent = SearchViewModel().apply {
        // _uiState.value = SearchUiState(
        // query = "",
        // recentSearches = listOf("Old Movie", "Cool Series"),
        // isLoading = false,
        //     isFocused = true
        // )
    }
    TomatoTheme {
        SearchScreen(viewModel = vmWithRecent, onNavigateBack = {}, onNavigateToDetails = { _, _ -> })
    }
}
