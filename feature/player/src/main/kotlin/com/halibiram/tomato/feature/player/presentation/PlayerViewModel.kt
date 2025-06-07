package com.halibiram.tomato.feature.player.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.core.player.exoplayer.PlayerManager
import com.halibiram.tomato.core.player.exoplayer.PlayerState // Core player state
import com.halibiram.tomato.feature.player.navigation.PlayerArgs // Assuming nav args helper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI-specific state for PlayerScreen, if needed beyond core PlayerState
data class PlayerScreenUiState(
    val controlsVisible: Boolean = true,
    val isFullScreen: Boolean = false, // Placeholder for fullscreen state
    val mediaTitle: String = "Loading...", // To display title on overlay
    val availableSubtitleTracks: List<androidx.media3.common.TrackGroup> = emptyList(),
    val selectedSubtitleTrackParams: androidx.media3.common.TrackSelectionParameters? = null
    // Add other UI specific states: e.g., error messages formatted for UI, settings panel visibility
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val playerManager: PlayerManager
) : ViewModel() {

    val corePlayerState: StateFlow<PlayerState> = playerManager.playerStateFlow

    private val _uiState = MutableStateFlow(PlayerScreenUiState())
    val uiState: StateFlow<PlayerScreenUiState> = _uiState.asStateFlow()

    private var hideControlsJob: Job? = null
    private val controlsTimeoutMs = 3000L // Hide controls after 3 seconds of inactivity

    // Media URL passed via navigation
    private val mediaUrl: String? = savedStateHandle[PlayerArgs.MEDIA_URL_ARG] // Using PlayerArgs helper

    init {
        // Collect from core player state to update media title or other UI specific states
        viewModelScope.launch {
            corePlayerState.collectLatest { coreState ->
                _uiState.update {
                    it.copy(
                        mediaTitle = extractTitleFromCoreState(coreState), // Example
                        availableSubtitleTracks = coreState.availableSubtitleTracks,
                        selectedSubtitleTrackParams = coreState.selectedSubtitleTrackParameters
                    )
                }
                // If controls are visible and player is playing, reset hide controls timer
                if (_uiState.value.controlsVisible && coreState.isPlaying) {
                    resetHideControlsTimer()
                }
            }
        }

        initializeAndPlayMedia()
    }

    private fun extractTitleFromCoreState(coreState: PlayerState): String {
        // Placeholder: In a real app, media metadata (title, etc.) might come from
        // the MediaItem in ExoPlayer, or be fetched separately based on mediaId.
        // For now, using a generic title or trying to get it from mediaUrl.
        return mediaUrl?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Unknown Media"
    }

    private fun initializeAndPlayMedia() {
        // playerManager.initializePlayer() // PlayerManager constructor can handle init if needed
        if (mediaUrl != null) {
            playerManager.playMedia(mediaUrl)
            showControlsThenAutoHide() // Show controls initially
        } else {
            // Handle missing media URL - update UI state with an error
            _uiState.update { it.copy(mediaTitle = "Error: Media URL not found") }
            // corePlayerState might also reflect an error if playMedia fails due to no URL
        }
    }

    fun play() {
        playerManager.resumePlayback()
    }

    fun pause() {
        playerManager.pausePlayback()
    }

    fun togglePlayPause() {
        if (corePlayerState.value.isPlaying) {
            playerManager.pausePlayback()
            showControlsPermanently() // Keep controls visible when paused by user
        } else {
            playerManager.resumePlayback()
            resetHideControlsTimer() // Hide controls after timeout when playback resumes
        }
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekPlayback(positionMs)
        resetHideControlsTimer() // User interaction, show controls
    }

    fun showControlsThenAutoHide() {
        _uiState.update { it.copy(controlsVisible = true) }
        resetHideControlsTimer()
    }

    private fun showControlsPermanently() {
        hideControlsJob?.cancel()
        _uiState.update { it.copy(controlsVisible = true) }
    }

    fun hideControls() {
        _uiState.update { it.copy(controlsVisible = false) }
    }

    private fun resetHideControlsTimer() {
        hideControlsJob?.cancel()
        if (corePlayerState.value.isPlaying) { // Only auto-hide if playing
            hideControlsJob = viewModelScope.launch {
                delay(controlsTimeoutMs)
                hideControls()
            }
        }
    }

    fun getExoPlayerInstance(): androidx.media3.exoplayer.ExoPlayer {
        return playerManager.getExoPlayerInstance()
    }

    fun toggleFullScreen() { // Placeholder
        _uiState.update { it.copy(isFullScreen = !it.isFullScreen) }
        // Actual fullscreen implementation would involve system UI changes, activity window flags etc.
    }

    fun selectSubtitleTrack(groupIndex: Int, trackIndex: Int) {
        playerManager.setSubtitleTrack(trackIndex, groupIndex)
        resetHideControlsTimer()
    }

    fun clearSubtitles() {
        playerManager.clearSubtitleTrack()
        resetHideControlsTimer()
    }


    override fun onCleared() {
        super.onCleared()
        playerManager.release() // Release player when ViewModel is cleared
        hideControlsJob?.cancel()
    }
}
