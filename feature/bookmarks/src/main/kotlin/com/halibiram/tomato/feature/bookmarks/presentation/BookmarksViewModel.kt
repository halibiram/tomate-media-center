package com.halibiram.tomato.feature.bookmarks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.core.database.entity.BookmarkEntity // Assuming this exists
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

// UI representation for a bookmark, can be same as BookmarkEntity or mapped
data class UiBookmarkItem(
    val mediaId: String,
    val title: String?,
    val posterPath: String?,
    val mediaType: String, // "movie" or "series"
    val bookmarkedDate: Date
)

data class BookmarksUiState(
    val bookmarks: List<UiBookmarkItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentFilter: String = "ALL" // e.g., "ALL", "MOVIE", "SERIES"
)

// @HiltViewModel
class BookmarksViewModel /*@Inject constructor(
    // private val bookmarksRepository: BookmarksRepository
)*/ : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState

    init {
        loadBookmarks()
    }

    fun loadBookmarks(filter: String = "ALL") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, currentFilter = filter)
            try {
                // bookmarksRepository.getBookmarksFlow(filter).collectLatest { entities ->
                //     val uiItems = entities.map { entity ->
                //         UiBookmarkItem(
                //             mediaId = entity.mediaId,
                //             title = entity.title,
                //             posterPath = entity.posterPath,
                //             mediaType = entity.mediaType,
                //             bookmarkedDate = entity.bookmarkedDate
                //         )
                //     }
                //     _uiState.value = _uiState.value.copy(isLoading = false, bookmarks = uiItems)
                // }

                // Simulate data fetch
                kotlinx.coroutines.delay(500)
                val simulatedBookmarks = listOf(
                    UiBookmarkItem("movie123", "Awesome Movie Title", null, BookmarkEntity.TYPE_MOVIE, Date()),
                    UiBookmarkItem("series456", "Great Series Name", null, BookmarkEntity.TYPE_SERIES, Date()),
                    UiBookmarkItem("movie789", "Another Movie Here", "path/img.jpg", BookmarkEntity.TYPE_MOVIE, Date())
                ).filter {
                    filter == "ALL" || it.mediaType.equals(filter, ignoreCase = true)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, bookmarks = simulatedBookmarks)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load bookmarks: ${e.message}")
            }
        }
    }

    fun removeBookmark(mediaId: String) {
        viewModelScope.launch {
            // bookmarksRepository.removeBookmark(mediaId)
            // Refresh list or rely on flow to update automatically
            loadBookmarks(_uiState.value.currentFilter)
        }
    }

    fun onFilterChange(newFilter: String) {
        loadBookmarks(newFilter)
    }
}
