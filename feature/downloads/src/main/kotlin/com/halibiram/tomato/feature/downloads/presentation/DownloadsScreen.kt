package com.halibiram.tomato.feature.downloads.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep // For clear all
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halibiram.tomato.domain.model.Download // Domain model
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.feature.downloads.presentation.component.DownloadItem
import com.halibiram.tomato.ui.theme.TomatoTheme // Ensure this path is correct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (mediaId: String, mediaType: String, filePath: String) -> Unit // Added filePath
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Downloads") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.downloads.isNotEmpty()) {
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All Downloads")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // TODO: Implement filter UI if needed, e.g., using uiState.currentFilter and viewModel.onFilterChange
            // DownloadFilters(...)

            if (uiState.isLoading && uiState.downloads.isEmpty()) { // Show loading only if list is empty initially
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.downloads.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No downloads yet.")
                        // Optionally, a button to browse content to download
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.downloads, key = { it.id }) { download ->
                        DownloadItem(
                            download = download,
                            onPauseClick = { viewModel.pauseDownload(download.id) },
                            onResumeClick = { viewModel.resumeDownload(download) },
                            onCancelClick = { viewModel.cancelDownload(download.id) },
                            onDeleteClick = { viewModel.deleteDownload(download) },
                            onPlayClick = {
                                if (download.status == DownloadStatus.COMPLETED && download.filePath != null) {
                                    onNavigateToPlayer(download.mediaId, download.mediaType.name, download.filePath)
                                }
                                // Else, could show a toast or disable play button
                            },
                            onRetryClick = { viewModel.retryDownload(download) }
                        )
                    }
                }
            }
        }

        if (showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog = false },
                title = { Text("Clear All Downloads?") },
                text = { Text("Are you sure you want to delete all download records? This action might not delete the files themselves unless explicitly handled.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // viewModel.clearAllDownloads() // TODO: Implement this in ViewModel and UseCase
                            showClearAllDialog = false
                        }
                    ) { Text("Clear All", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showClearAllDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

// Preview needs a fake ViewModel or Hilt setup for previews
@Preview(showBackground = true)
@Composable
fun DownloadsScreenPreview_Empty() {
    val mockViewModel: DownloadsViewModel = mockk(relaxed = true)
    every { mockViewModel.uiState } returns MutableStateFlow(DownloadsUiState(isLoading = false, downloads = emptyList()))
    TomatoTheme {
        DownloadsScreen(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onNavigateToPlayer = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadsScreenPreview_WithItems() {
     val mockViewModel: DownloadsViewModel = mockk(relaxed = true)
    val sampleDownloads = listOf(
        Download("1", "m1", com.halibiram.tomato.domain.model.DownloadMediaType.MOVIE, "Movie Title 1", "url1", DownloadStatus.DOWNLOADING, 50, null, 100MB, 50MB, System.currentTimeMillis()),
        Download("2", "e1", com.halibiram.tomato.domain.model.DownloadMediaType.SERIES_EPISODE, "S01E01 - Episode Title", "url2", DownloadStatus.COMPLETED, 100, "/path/to/file.mp4", 200MB, 200MB, System.currentTimeMillis() - 100000)
    )
    every { mockViewModel.uiState } returns MutableStateFlow(DownloadsUiState(isLoading = false, downloads = sampleDownloads))

    TomatoTheme {
        DownloadsScreen(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onNavigateToPlayer = { _, _, _ -> }
        )
    }
}

// Helper for preview sizes
private val Long.MB: Long get() = this * 1024 * 1024
