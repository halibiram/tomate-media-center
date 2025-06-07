package com.halibiram.tomato.feature.downloads.presentation

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
import com.halibiram.tomato.core.database.entity.DownloadEntity
import com.halibiram.tomato.feature.downloads.presentation.component.DownloadItem
import com.halibiram.tomato.ui.theme.TomatoTheme
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    // viewModel: DownloadsViewModel = hiltViewModel(), // With Hilt
    viewModel: DownloadsViewModel, // Pass for preview or non-Hilt
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (mediaId: String, mediaType: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Downloads") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
                // You could add filter actions here if desired
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            DownloadFilters(
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
            } else if (uiState.downloads.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No downloads found${if (uiState.currentFilter != "ALL") " for this filter" else ""}.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.downloads, key = { it.mediaId }) { download ->
                        DownloadItem(
                            item = download,
                            onItemClick = { mediaId, mediaType, status ->
                                if (status == DownloadEntity.STATUS_COMPLETED) {
                                    onNavigateToPlayer(mediaId, mediaType)
                                } else {
                                    // Handle click on non-completed items, e.g., show details or toast
                                }
                            },
                            onCancelClick = viewModel::cancelDownload,
                            onPauseClick = viewModel::pauseDownload,
                            onResumeClick = viewModel::resumeDownload,
                            onDeleteClick = { mediaId, _ -> viewModel.deleteDownload(mediaId, null /* path TODO */) },
                            onRetryClick = viewModel::retryDownload
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadFilters(
    currentFilter: String,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("ALL", DownloadEntity.STATUS_DOWNLOADING, DownloadEntity.STATUS_COMPLETED, DownloadEntity.STATUS_FAILED) // Add more as needed
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Filter: $currentFilter")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            filters.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter.replace("_", " ").capitalize()) },
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
fun DownloadsScreenPreview_Empty() {
    val emptyVM = DownloadsViewModel() // Default state might be loading or empty
    TomatoTheme {
        DownloadsScreen(viewModel = emptyVM, onNavigateBack = {}, onNavigateToPlayer = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadsScreenPreview_WithItems() {
    val vmWithItems = DownloadsViewModel().apply {
        // Simulate having items (ViewModel needs modification for easy preview state setting)
        // _uiState.value = DownloadsUiState(
        //     isLoading = false,
        //     downloads = listOf(
        //         UiDownloadItem("m1", "Movie 1", null, 1L, 1L, 100, DownloadEntity.STATUS_COMPLETED, Date(), "movie"),
        //         UiDownloadItem("e1", "Episode 1", null, 2L, 1L, 50, DownloadEntity.STATUS_DOWNLOADING, Date(), "episode")
        //     )
        // )
    }
    TomatoTheme {
        DownloadsScreen(viewModel = vmWithItems, onNavigateBack = {}, onNavigateToPlayer = {_,_ ->})
    }
}
