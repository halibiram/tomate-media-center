package com.halibiram.tomato.feature.bookmarks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.core.common.result.TomatoException // For potential error types
import com.halibiram.tomato.domain.model.Bookmark
import com.halibiram.tomato.domain.model.BookmarkMediaType
import com.halibiram.tomato.domain.usecase.bookmark.GetBookmarksUseCase
import com.halibiram.tomato.domain.usecase.bookmark.RemoveBookmarkUseCase
// Import IsBookmarkedUseCase if used for on-item status, though less common on this screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarksUiState(
    val isLoading: Boolean = false,
    val bookmarks: List<Bookmark> = emptyList(),
    val error: String? = null,
    val filter: BookmarkMediaType? = null // null for ALL, or specific type
)

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val removeBookmarkUseCase: RemoveBookmarkUseCase
    // private val isBookmarkedUseCase: IsBookmarkedUseCase // Potentially for quick status checks if needed
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    // Trigger to re-fetch bookmarks when filter changes
    private val filterState = MutableStateFlow<BookmarkMediaType?>(null)

    init {
        viewModelScope.launch {
            filterState
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .flatMapLatest { currentFilter ->
                    getBookmarksUseCase(filterType = currentFilter)
                }
                .catch { e -> // Catch errors from the GetBookmarksUseCase flow
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = (e as? TomatoException)?.message ?: "An unexpected error occurred while fetching bookmarks."
                        )
                    }
                }
                .collectLatest { bookmarks ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bookmarks = bookmarks,
                            error = null // Clear previous error on new data
                        )
                    }
                }
        }
        // Initial load with default filter (ALL)
        setFilter(null)
    }

    fun removeBookmark(mediaId: String, mediaType: BookmarkMediaType) {
        viewModelScope.launch {
            try {
                removeBookmarkUseCase(mediaId, mediaType)
                // The flow collection in init should automatically update the list.
                // If not, or for immediate feedback, can manually remove from current list:
                // _uiState.update { currentState ->
                //     currentState.copy(bookmarks = currentState.bookmarks.filterNot { it.mediaId == mediaId && it.mediaType == mediaType })
                // }
            } catch (e: Exception) {
                 _uiState.update { it.copy(error = "Failed to remove bookmark: ${e.message}") }
            }
        }
    }

    fun setFilter(filter: BookmarkMediaType?) {
        _uiState.update { it.copy(filter = filter, isLoading = true) } // Update UI filter state and show loading
        filterState.value = filter // Trigger re-fetch by updating the flow observed in init
    }

    fun onBookmarkClick(bookmark: Bookmark) {
        // Placeholder for navigation logic
        // e.g., viewModelScope.launch { _navigationEvent.emit(NavigationEvent.ToDetails(bookmark.mediaId, bookmark.mediaType)) }
        // Log.d("BookmarksViewModel", "Bookmark clicked: ${bookmark.title}")
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
