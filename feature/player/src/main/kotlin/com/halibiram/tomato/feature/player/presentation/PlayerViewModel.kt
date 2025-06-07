package com.halibiram.tomato.feature.player.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.google.android.exoplayer2.ExoPlayer // Assuming ExoPlayer usage
// import com.halibiram.tomato.feature.player.navigation.PlayerArgs // If using Args class
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Data class for media item being played (simplified)
data class MediaItem(
    val id: String,
    val title: String,
    val videoUrl: String,
    val artworkUrl: String? = null,
    val seriesTitle: String? = null, // For TV shows
    val episodeTitle: String? = null // For TV shows
)

// UI State for the player
data class PlayerUiState(
    val mediaItem: MediaItem? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val error: String? = null,
    val controlsVisible: Boolean = true,
    val isBuffering: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    // Add other relevant states: subtitles, quality, next episode, etc.
)

// @HiltViewModel
class PlayerViewModel /*@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    // private val exoPlayer: ExoPlayer, // Injected ExoPlayer instance
    // private val playerRepository: PlayerRepository, // To fetch media details or save progress
    // private val playerSettings: PlayerPreferences // To get user's player preferences
)*/ : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState

    // private val mediaId: String? = savedStateHandle[PlayerArgs.MEDIA_ID_ARG] // Example with Nav Args
    // private val mediaType: String? = savedStateHandle[PlayerArgs.MEDIA_TYPE_ARG]

    init {
        // val mediaIdFromArgs = savedStateHandle.get<String>(PlayerArgs.MEDIA_ID_ARG)
        // val mediaTypeFromArgs = savedStateHandle.get<String>(PlayerArgs.MEDIA_TYPE_ARG)
        // if (mediaIdFromArgs != null && mediaTypeFromArgs != null) {
        //     loadMediaContent(mediaIdFromArgs, mediaTypeFromArgs)
        // } else {
        //     _uiState.value = _uiState.value.copy(isLoading = false, error = "Media ID or Type missing")
        // }
        // setupExoPlayerListeners()
        // applyPlayerPreferences()

        // Placeholder if no nav args are set up yet:
        loadMediaContent("placeholder_id", "movie")
    }

    private fun loadMediaContent(mediaId: String, mediaType: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // val item = playerRepository.getMediaDetails(mediaId, mediaType)
                // Simulate fetch
                kotlinx.coroutines.delay(500)
                val simulatedItem = MediaItem(
                    id = mediaId,
                    title = if (mediaType == "movie") "Sample Movie Title" else "Sample Episode Title",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", // Sample URL
                    artworkUrl = "https://example.com/artwork.jpg",
                    seriesTitle = if (mediaType == "series_episode") "Awesome Series" else null,
                    episodeTitle = if (mediaType == "series_episode") "S01E01 - Pilot" else null
                )
                _uiState.value = _uiState.value.copy(mediaItem = simulatedItem, isLoading = false)
                // preparePlayer(simulatedItem.videoUrl)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load media: ${e.message}")
            }
        }
    }

    // private fun preparePlayer(videoUrl: String) {
    //     val mediaItem = com.google.android.exoplayer2.MediaItem.fromUri(videoUrl)
    //     exoPlayer.setMediaItem(mediaItem)
    //     exoPlayer.prepare()
    //     // exoPlayer.playWhenReady = true // Auto-play or based on preference
    // }

    // private fun setupExoPlayerListeners() {
    //     exoPlayer.addListener(object : Player.Listener {
    //         override fun onIsPlayingChanged(isPlaying: Boolean) {
    //             _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
    //         }
    //         override fun onPlaybackStateChanged(playbackState: Int) {
    //             _uiState.value = _uiState.value.copy(
    //                 isBuffering = playbackState == Player.STATE_BUFFERING,
    //                 totalDurationMs = exoPlayer.duration.coerceAtLeast(0L)
    //             )
    //             if (playbackState == Player.STATE_ENDED) {
    //                 // Handle playback end (e.g., play next episode, show related)
    //             }
    //         }
    //         // Implement other listeners: onPlayerError, onTimelineChanged, etc.
    //     })
    //     // Regularly update current position
    //     viewModelScope.launch {
    //         while (true) {
    //             if (exoPlayer.isPlaying) {
    //                 _uiState.value = _uiState.value.copy(currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L))
    //             }
    //             delay(1000) // Update every second
    //         }
    //     }
    // }

    // private fun applyPlayerPreferences() {
    //     viewModelScope.launch {
    //         playerSettings.playerSettingsFlow.collect { settings ->
    //             exoPlayer.setPlaybackSpeed(settings.playbackSpeed)
    //             _uiState.value = _uiState.value.copy(playbackSpeed = settings.playbackSpeed)
    //             // Apply other settings like preferred subtitle language, quality etc.
    //         }
    //     }
    // }

    fun togglePlayPause() {
        // if (exoPlayer.isPlaying) {
        //     exoPlayer.pause()
        // } else {
        //     exoPlayer.play()
        // }
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying) // Simulate
    }

    fun seekForward(seconds: Int = 10) {
        // val newPosition = (exoPlayer.currentPosition + seconds * 1000).coerceIn(0, exoPlayer.duration)
        // exoPlayer.seekTo(newPosition)
        // _uiState.value = _uiState.value.copy(currentPositionMs = newPosition) // Simulate
    }

    fun seekBackward(seconds: Int = 10) {
        // val newPosition = (exoPlayer.currentPosition - seconds * 1000).coerceIn(0, exoPlayer.duration)
        // exoPlayer.seekTo(newPosition)
        // _uiState.value = _uiState.value.copy(currentPositionMs = newPosition) // Simulate
    }

    fun onPlaybackSpeedChange(speed: Float) {
        // exoPlayer.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun toggleControlsVisibility() {
        _uiState.value = _uiState.value.copy(controlsVisible = !_uiState.value.controlsVisible)
    }

    override fun onCleared() {
        super.onCleared()
        // exoPlayer.release() // Release ExoPlayer when ViewModel is cleared
    }
}
