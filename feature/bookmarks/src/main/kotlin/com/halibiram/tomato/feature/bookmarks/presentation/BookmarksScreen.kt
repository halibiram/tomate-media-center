package com.halibiram.tomato.feature.bookmarks.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halibiram.tomato.domain.model.Bookmark
import com.halibiram.tomato.domain.model.BookmarkMediaType
import com.halibiram.tomato.feature.bookmarks.presentation.component.BookmarkItem
import com.halibiram.tomato.ui.theme.TomatoTheme // Ensure this path is correct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (mediaId: String, mediaType: String) -> Unit // Pass mediaType as String
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookmarks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter Bookmarks")
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All") },
                                onClick = {
                                    viewModel.setFilter(null)
                                    showFilterMenu = false
                                }
                            )
                            BookmarkMediaType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name.lowercase().replaceFirstChar { it.titlecase() }) },
                                    onClick = {
                                        viewModel.setFilter(type)
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Display current filter (optional, could be part of TopAppBar or just reflected in list)
            // Text("Current Filter: ${uiState.filter?.name ?: "All"}", modifier = Modifier.padding(16.dp))

            if (uiState.isLoading && uiState.bookmarks.isEmpty()) { // Show loading only if list is empty initially
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.setFilter(uiState.filter) }) { // Retry with current filter
                            Text("Retry")
                        }
                    }
                }
            } else if (uiState.bookmarks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No bookmarks found${uiState.filter?.let { " for ${it.name.lowercase()}s" } ?: ""}. " +
                        "Add some from movie or series details!"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.bookmarks, key = { "${it.mediaId}_${it.mediaType.name}" }) { bookmark ->
                        BookmarkItem(
                            bookmark = bookmark,
                            onItemClick = {
                                viewModel.onBookmarkClick(it) // For VM logic if any
                                onNavigateToDetails(it.mediaId, it.mediaType.name)
                            },
                            onRemoveClick = { mediaId, mediaType ->
                                viewModel.removeBookmark(mediaId, mediaType)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Preview needs a fake ViewModel or Hilt setup for previews
@Preview(showBackground = true)
@Composable
fun BookmarksScreenPreview_Empty() {
    val mockViewModel: BookmarksViewModel = mockk(relaxed = true)
    every { mockViewModel.uiState } returns MutableStateFlow(BookmarksUiState(isLoading = false, bookmarks = emptyList()))
    TomatoTheme {
        BookmarksScreen(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onNavigateToDetails = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarksScreenPreview_WithItems() {
     val mockViewModel: BookmarksViewModel = mockk(relaxed = true)
    val sampleBookmarks = listOf(
        Bookmark("m1", BookmarkMediaType.MOVIE, "Awesome Movie", "/poster.jpg", System.currentTimeMillis()),
        Bookmark("s1", BookmarkMediaType.SERIES, "Cool Series", null, System.currentTimeMillis() - 10000)
    )
    every { mockViewModel.uiState } returns MutableStateFlow(BookmarksUiState(isLoading = false, bookmarks = sampleBookmarks))

    TomatoTheme {
        BookmarksScreen(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onNavigateToDetails = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarksScreenPreview_Error() {
     val mockViewModel: BookmarksViewModel = mockk(relaxed = true)
    every { mockViewModel.uiState } returns MutableStateFlow(BookmarksUiState(isLoading = false, error = "Failed to load bookmarks"))

    TomatoTheme {
        BookmarksScreen(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onNavigateToDetails = { _, _ -> }
        )
    }
}
