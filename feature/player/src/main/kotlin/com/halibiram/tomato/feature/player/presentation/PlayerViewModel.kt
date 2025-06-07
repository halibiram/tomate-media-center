package com.halibiram.tomato.feature.player.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.core.player.cast.CastManager
import com.halibiram.tomato.core.player.cast.CastState
import com.halibiram.tomato.core.player.exoplayer.PlayerManager
import com.halibiram.tomato.core.player.exoplayer.PlayerState // Core player state
import com.halibiram.tomato.feature.player.navigation.PlayerArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.media3.common.TrackGroup // For method parameters

// UI-specific state for PlayerScreen
data class PlayerScreenUiState(
    val controlsVisible: Boolean = true,
    val isFullScreen: Boolean = false,
    val mediaTitle: String = "Loading...",
    // availableSubtitleTracks and selectedSubtitleTrackParams are now directly from corePlayerState
    // val availableSubtitleTracks: List<androidx.media3.common.TrackGroup> = emptyList(),
    // val selectedSubtitleTrackParams: androidx.media3.common.TrackSelectionParameters? = null,
    val isCastAvailable: Boolean = false,
    val currentCastState: CastState = CastState.NO_DEVICES_AVAILABLE,
    val isCasting: Boolean = false,
    val showTrackSelectionDialog: Boolean = false // New state for dialog visibility
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val playerManager: PlayerManager,
    private val castManager: CastManager
) : ViewModel() {

    val corePlayerState: StateFlow<PlayerState> = playerManager.playerStateFlow

    private val _uiState = MutableStateFlow(PlayerScreenUiState())
    val uiState: StateFlow<PlayerScreenUiState> = _uiState.asStateFlow()

    private var hideControlsJob: Job? = null
    private val controlsTimeoutMs = 3000L

    private val mediaUrl: String? = savedStateHandle[PlayerArgs.MEDIA_URL_ARG]
    private var currentMediaUrl: String? = null
    private var currentMediaTitle: String = "Unknown Media"
    private var currentPosterUrl: String? = null

    init {
        currentMediaUrl = mediaUrl
        currentMediaTitle = currentMediaUrl?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Unknown Media"

        // Combine corePlayerState with other flows if needed, or update uiState based on corePlayerState
        viewModelScope.launch {
            corePlayerState.collectLatest { coreState ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        mediaTitle = extractTitleFromCoreState(coreState)
                        // availableSubtitleTracks and selectedTrackParams are now directly in corePlayerState,
                        // UI will collect corePlayerState directly for those.
                    )
                }
                if (_uiState.value.controlsVisible && coreState.isPlaying && !_uiState.value.isCasting) {
                    resetHideControlsTimer()
                }
            }
        }

        viewModelScope.launch {
            castManager.isCastAvailable.collectLatest { available ->
                _uiState.update { it.copy(isCastAvailable = available) }
            }
        }

        viewModelScope.launch {
            castManager.castState.collectLatest { castState ->
                val isNowCasting = castState == CastState.CONNECTED
                _uiState.update { it.copy(currentCastState = castState, isCasting = isNowCasting) }
                if (isNowCasting) {
                    playerManager.pausePlayback()
                }
            }
        }

        initializeAndPlayMedia(currentMediaUrl)
    }

    private fun extractTitleFromCoreState(coreState: PlayerState): String {
        // TODO: Use actual metadata from coreState.mediaItem if available
        return currentMediaTitle
    }

    private fun initializeAndPlayMedia(url: String?) {
        if (url != null) {
            if (_uiState.value.isCasting) {
                castManager.loadRemoteMedia(
                    mediaUrl = url,
                    title = currentMediaTitle,
                    posterUrl = currentPosterUrl,
                    currentLocalPlayerPosition = corePlayerState.value.currentPositionMs
                )
                playerManager.pausePlayback()
            } else {
                playerManager.playMedia(url)
            }
            showControlsThenAutoHide()
        } else {
            _uiState.update { it.copy(mediaTitle = "Error: Media URL not found") }
        }
    }

    fun retryPlayback() {
        initializeAndPlayMedia(currentMediaUrl)
    }

    fun play() {
        if (_uiState.value.isCasting) { /* TODO: Cast play */ } else { playerManager.resumePlayback() }
        resetHideControlsTimer()
    }

    fun pause() {
        if (_uiState.value.isCasting) { /* TODO: Cast pause */ } else { playerManager.pausePlayback() }
        showControlsPermanently()
    }

    fun togglePlayPause() {
        if (_uiState.value.isCasting) { /* TODO: Cast toggle */ } else {
            if (corePlayerState.value.isPlaying) pause() else play()
        }
    }

    fun seekTo(positionMs: Long) {
         if (_uiState.value.isCasting) { /* TODO: Cast seek */ } else { playerManager.seekPlayback(positionMs) }
        if (_uiState.value.controlsVisible && !_uiState.value.isCasting) {
            resetHideControlsTimer()
        }
    }

    fun seekForward(seconds: Int = 10) {
        val currentPosition = if (_uiState.value.isCasting) 0L else corePlayerState.value.currentPositionMs
        val duration = if (_uiState.value.isCasting) 0L else corePlayerState.value.durationMs
        val newPosition = (currentPosition + seconds * 1000).coerceAtMost(duration.takeIf { it > 0 } ?: Long.MAX_VALUE)
        seekTo(newPosition)
    }

    fun seekBackward(seconds: Int = 10) {
        val currentPosition = if (_uiState.value.isCasting) 0L else corePlayerState.value.currentPositionMs
        val newPosition = (currentPosition - seconds * 1000).coerceAtLeast(0L)
        seekTo(newPosition)
    }

    fun toggleControlsVisibility() {
        if (_uiState.value.controlsVisible) hideControls() else showControlsThenAutoHide()
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
        if (corePlayerState.value.isPlaying && _uiState.value.controlsVisible && !_uiState.value.isCasting) {
            hideControlsJob = viewModelScope.launch {
                delay(controlsTimeoutMs)
                hideControls()
            }
        }
    }

    fun getExoPlayerInstance(): androidx.media3.exoplayer.ExoPlayer = playerManager.getExoPlayerInstance()
    fun toggleFullScreen() { _uiState.update { it.copy(isFullScreen = !it.isFullScreen) } }

    // --- Track Selection ---
    fun openTrackSelectionDialog() {
        _uiState.update { it.copy(showTrackSelectionDialog = true) }
        showControlsPermanently() // Keep controls (and thus dialog) visible
    }

    fun closeTrackSelectionDialog() {
        _uiState.update { it.copy(showTrackSelectionDialog = false) }
        resetHideControlsTimer() // Resume auto-hide for controls
    }

    fun selectSubtitleTrack(trackGroup: TrackGroup, trackIndex: Int) {
        playerManager.setSubtitleTrack(trackIndex, groupIndex = corePlayerState.value.availableSubtitleTracks.indexOf(trackGroup))
        // PlayerManager needs the groupIndex as per its current signature, or it should find it.
        // For simplicity, let's assume PlayerManager's setSubtitleTrack can take the group directly.
        // The setSubtitleTrack in TomatoExoPlayer was updated to take group and trackIndex.
        // PlayerManager.setSubtitleTrack(trackGroup, trackIndex) // Assuming this method exists in PlayerManager
        // The current PlayerManager.setSubtitleTrack takes (trackIndex, groupIndex, builder).
        // Let's refine this in PlayerManager or TomatoExoPlayer.
        // For now, assuming TomatoExoPlayer's method is called directly or PlayerManager is adapted.
        getExoPlayerInstance().trackSelectionParameters = getExoPlayerInstance().trackSelectionParameters.buildUpon()
            .setOverrideForType(TrackSelectionOverride(trackGroup, listOf(trackIndex)))
            .setTrackSelectionDisabled(false, C.TRACK_TYPE_TEXT) // Ensure text tracks are enabled
            .build()

        resetHideControlsTimer()
    }

    fun disableSubtitles() {
        // playerManager.clearSubtitleTrack() // Preferred way via PlayerManager
        getExoPlayerInstance().trackSelectionParameters = getExoPlayerInstance().trackSelectionParameters.buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .setTrackSelectionDisabled(true, C.TRACK_TYPE_TEXT)
            .build()
        resetHideControlsTimer()
    }

    fun selectAudioTrack(trackGroup: TrackGroup, trackIndex: Int) {
        // playerManager.selectAudioTrack(trackGroup, trackIndex) // Preferred
         getExoPlayerInstance().trackSelectionParameters = getExoPlayerInstance().trackSelectionParameters.buildUpon()
            .setOverrideForType(TrackSelectionOverride(trackGroup, listOf(trackIndex)))
             .setTrackSelectionDisabled(false, C.TRACK_TYPE_AUDIO)
            .build()
        resetHideControlsTimer()
    }

    // --- Cast Methods ---
    fun startCasting() {
        if (currentMediaUrl != null && _uiState.value.currentCastState == CastState.CONNECTED) {
            playerManager.pausePlayback()
            val localPosition = corePlayerState.value.currentPositionMs
            castManager.loadRemoteMedia(
                mediaUrl = currentMediaUrl!!,
                title = currentMediaTitle,
                posterUrl = currentPosterUrl,
                currentLocalPlayerPosition = localPosition
            )
            _uiState.update { it.copy(isCasting = true) }
        }
    }

    fun stopCastingAndResumeLocal() {
        val castPosition = 0L // Placeholder: castManager.getCurrentRemotePosition()
        playerManager.seekPlayback(castPosition)
        playerManager.resumePlayback()
        _uiState.update { it.copy(isCasting = false) }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
        hideControlsJob?.cancel()
    }
}
