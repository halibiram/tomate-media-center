package com.halibiram.tomato.feature.bookmarks.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.core.database.entity.BookmarkEntity
import com.halibiram.tomato.feature.bookmarks.presentation.component.BookmarkItem
import com.halibiram.tomato.ui.theme.TomatoTheme
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    // viewModel: BookmarksViewModel = hiltViewModel(), // With Hilt
    viewModel: BookmarksViewModel, // Pass for preview or non-Hilt
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (mediaId: String, mediaType: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookmarks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            BookmarkFilters(
                currentFilter = uiState.currentFilter,
                onFilterChange = viewModel::onFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.bookmarks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No bookmarks found${if (uiState.currentFilter != "ALL") " for this filter" else ""}.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.bookmarks, key = { it.mediaId }) { bookmark ->
                        BookmarkItem(
                            item = bookmark,
                            onItemClick = { mediaId, mediaType ->
                                onNavigateToDetails(mediaId, mediaType)
                            },
                            onRemoveClick = viewModel::removeBookmark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarkFilters(
    currentFilter: String,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("ALL", BookmarkEntity.TYPE_MOVIE, BookmarkEntity.TYPE_SERIES)
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Filter: ${currentFilter.capitalize()}")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            filters.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter.capitalize()) },
                    onClick = {
                        onFilterChange(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BookmarksScreenPreview_Empty() {
    val emptyVM = BookmarksViewModel()
    TomatoTheme {
        BookmarksScreen(viewModel = emptyVM, onNavigateBack = {}, onNavigateToDetails = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarksScreenPreview_WithItems() {
    val vmWithItems = BookmarksViewModel().apply {
        // Simulate having items (ViewModel needs modification for easy preview state setting)
        // _uiState.value = BookmarksUiState(
        //     isLoading = false,
        //     bookmarks = listOf(
        //         UiBookmarkItem("m1", "Movie 1", null, BookmarkEntity.TYPE_MOVIE, Date()),
        //         UiBookmarkItem("s1", "Series 1", null, BookmarkEntity.TYPE_SERIES, Date())
        //     )
        // )
    }
    TomatoTheme {
        BookmarksScreen(viewModel = vmWithItems, onNavigateBack = {}, onNavigateToDetails = {_,_ ->})
    }
}
