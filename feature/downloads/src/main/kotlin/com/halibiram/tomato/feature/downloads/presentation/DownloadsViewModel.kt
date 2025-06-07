package com.halibiram.tomato.feature.downloads.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.model.DownloadMediaType
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.usecase.download.* // Import all download use cases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// UiDownloadItem can be the same as domain.model.Download for simplicity
// typealias UiDownloadItem = com.halibiram.tomato.domain.model.Download

data class DownloadsUiState(
    val isLoading: Boolean = true,
    val downloads: List<Download> = emptyList(), // Using domain model directly
    val error: String? = null,
    val currentFilter: String = "ALL" // Example filter state
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val getDownloadsUseCase: GetDownloadsUseCase,
    private val downloadMediaUseCase: DownloadMediaUseCase,
    private val pauseDownloadUseCase: PauseDownloadUseCase,
    private val resumeDownloadUseCase: ResumeDownloadUseCase,
    private val cancelDownloadUseCase: CancelDownloadUseCase,
    private val deleteDownloadedFileUseCase: DeleteDownloadedFileUseCase
    // private val workManager: androidx.work.WorkManager // For observing WorkManager progress directly if needed
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        observeDownloads()
        // observeWorkManagerProgress() // Optional: For more real-time UI updates from WorkManager
    }

    private fun observeDownloads() {
        viewModelScope.launch {
            // TODO: Add filter parameter to getDownloadsUseCase or handle filtering here
            getDownloadsUseCase.invoke() // Default: get all
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Error fetching downloads") } }
                .collectLatest { downloads ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            downloads = downloads.sortedWith(compareByDescending<Download> { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PENDING }.thenByDescending { it.addedDate })
                        )
                    }
                }
        }
    }

    // Example: Start a new download (parameters would come from UI event)
    fun startNewDownload(mediaId: String, mediaUrl: String, title: String, mediaType: DownloadMediaType, posterPath: String?) {
        viewModelScope.launch {
            // Show some immediate feedback if needed (e.g., toast or temporary loading state for this item)
            val result = downloadMediaUseCase(
                mediaId = mediaId,
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                title = title,
                posterPath = posterPath
            )
            if (result == null) {
                // Handle case where download already exists or couldn't be enqueued
                 _uiState.update { it.copy(error = "Download for '$title' already exists or could not be started.") }
            }
            // UI will update automatically via observeDownloads() when DB changes
        }
    }

    fun pauseDownload(downloadId: String) {
        viewModelScope.launch {
            pauseDownloadUseCase(downloadId)
            // UI updates via DB observation
        }
    }

    fun resumeDownload(download: Download) {
        viewModelScope.launch {
            resumeDownloadUseCase(download)
            // UI updates via DB observation
        }
    }

    fun cancelDownload(downloadId: String) {
        viewModelScope.launch {
            cancelDownloadUseCase(downloadId)
            // UI updates via DB observation, or worker might delete from DB on cancel.
            // If it doesn't delete, the status will be CANCELLED.
        }
    }

    fun deleteDownload(download: Download) {
        viewModelScope.launch {
            deleteDownloadedFileUseCase(download.id)
            // UI updates via DB observation (item will be removed)
        }
    }

    fun retryDownload(download: Download) {
        // Retrying a failed download can be similar to resuming a paused one
        if (download.status == DownloadStatus.FAILED) {
            viewModelScope.launch {
                resumeDownloadUseCase(download) // Re-enqueue the worker
            }
        }
    }


    fun onFilterChange(newFilter: String) {
        // TODO: Implement filtering logic if getDownloadsUseCase doesn't support it directly.
        // This might involve re-fetching from use case with filter or client-side filtering.
        // For now, just updating state.
        _uiState.update { it.copy(currentFilter = newFilter) }
        // If use case supports filtering:
        // viewModelScope.launch {
        //     getDownloadsUseCase.invoke(filter = newFilter).collectLatest { ... }
        // }
    }
}
