package com.halibiram.tomato.feature.downloads.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import androidx.work.WorkManager // Example
import com.halibiram.tomato.core.database.entity.DownloadEntity // Assuming this exists
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

// Simplified Download Item for UI representation, can be same as DownloadEntity or mapped
data class UiDownloadItem(
    val mediaId: String,
    val title: String?,
    val posterPath: String?,
    val downloadSizeBytes: Long,
    val downloadedSizeBytes: Long,
    val progressPercentage: Int,
    val downloadStatus: String,
    val addedDate: Date,
    val mediaType: String // "movie" or "episode"
)

data class DownloadsUiState(
    val downloads: List<UiDownloadItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentFilter: String = "ALL" // e.g., "ALL", "DOWNLOADING", "COMPLETED"
)

// @HiltViewModel
class DownloadsViewModel /*@Inject constructor(
    // private val downloadsRepository: DownloadsRepository,
    // private val workManager: WorkManager // To observe worker status
)*/ : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState

    init {
        loadDownloads()
        // observeDownloadWorkerStatus() // If you want to update UI based on worker progress directly
    }

    fun loadDownloads(filter: String = "ALL") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, currentFilter = filter)
            try {
                // downloadsRepository.getDownloadsByStatusFlow(filter).collectLatest { entities ->
                //     val uiItems = entities.map { entity ->
                //         UiDownloadItem(
                //             mediaId = entity.mediaId,
                //             title = entity.title,
                //             posterPath = entity.posterPath,
                //             downloadSizeBytes = entity.downloadSizeBytes,
                //             downloadedSizeBytes = entity.downloadedSizeBytes,
                //             progressPercentage = entity.progressPercentage,
                //             downloadStatus = entity.downloadStatus,
                //             addedDate = entity.addedDate,
                //             mediaType = entity.mediaType
                //         )
                //     }
                //     _uiState.value = _uiState.value.copy(isLoading = false, downloads = uiItems)
                // }

                // Simulate data fetch
                kotlinx.coroutines.delay(500)
                val simulatedDownloads = listOf(
                    UiDownloadItem("movie123", "Big Buck Bunny", null, 100000000, 50000000, 50, DownloadEntity.STATUS_DOWNLOADING, Date(), DownloadEntity.TYPE_MOVIE),
                    UiDownloadItem("episode456", "S01E01 - Pilot", null, 200000000, 200000000, 100, DownloadEntity.STATUS_COMPLETED, Date(), DownloadEntity.TYPE_EPISODE)
                ).filter {
                    filter == "ALL" || it.downloadStatus == filter
                }
                _uiState.value = _uiState.value.copy(isLoading = false, downloads = simulatedDownloads)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load downloads: ${e.message}")
            }
        }
    }

    fun cancelDownload(mediaId: String) {
        viewModelScope.launch {
            // downloadsRepository.updateDownloadStatus(mediaId, DownloadEntity.STATUS_CANCELLED)
            // workManager.cancelUniqueWork(mediaId) // Assuming mediaId is used as unique work name
            // Refresh list or rely on flow to update
            loadDownloads(_uiState.value.currentFilter)
        }
    }

    fun pauseDownload(mediaId: String) {
        viewModelScope.launch {
            // downloadsRepository.updateDownloadStatus(mediaId, DownloadEntity.STATUS_PAUSED)
            // Potentially signal worker to pause, though WorkManager's pause is not direct.
            // Often means cancelling and re-queueing with constraints or handling in worker.
            loadDownloads(_uiState.value.currentFilter)
        }
    }

    fun resumeDownload(mediaId: String) {
        viewModelScope.launch {
            // val downloadEntity = downloadsRepository.getDownloadById(mediaId)
            // if (downloadEntity != null && downloadEntity.downloadStatus == DownloadEntity.STATUS_PAUSED) {
            //     // Re-enqueue download worker or update its state
            //     // downloadService.enqueueDownload(downloadEntity.mediaId, downloadEntity.mediaType, downloadEntity.downloadUrl)
            // }
            loadDownloads(_uiState.value.currentFilter)
        }
    }

    fun deleteDownload(mediaId: String, filePath: String?) {
        viewModelScope.launch {
            // downloadsRepository.deleteDownload(mediaId)
            // if (filePath != null) {
            //     // Delete the actual file from storage
            //     // File(filePath).delete()
            // }
            loadDownloads(_uiState.value.currentFilter)
        }
    }

    fun retryDownload(mediaId: String) {
         viewModelScope.launch {
            // val downloadEntity = downloadsRepository.getDownloadById(mediaId)
            // if (downloadEntity != null && downloadEntity.downloadStatus == DownloadEntity.STATUS_FAILED) {
            //     // Re-enqueue download worker
            //     // downloadService.enqueueDownload(downloadEntity.mediaId, downloadEntity.mediaType, downloadEntity.downloadUrl)
            // }
            loadDownloads(_uiState.value.currentFilter)
        }
    }

    // private fun observeDownloadWorkerStatus() {
    //     // Example: If you want to observe WorkManager's LiveData for progress
    //     // This would typically be done by observing work info by tag or ID
    //     // For each download, you might observe its specific worker.
    //     // This can get complex and might be better handled by updating DB from worker
    //     // and having the UI react to DB changes.
    // }

    fun onFilterChange(newFilter: String) {
        loadDownloads(newFilter)
    }
}
