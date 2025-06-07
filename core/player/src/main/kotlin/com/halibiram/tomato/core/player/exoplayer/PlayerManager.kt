package com.halibiram.tomato.core.player.exoplayer

// import android.content.Context // Not directly needed if TomatoExoPlayer and MediaSourceFactory are injected
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // PlayerManager could be a singleton to manage a single player instance for the app
class PlayerManager @Inject constructor(
    // @ApplicationContext private val context: Context, // Context can be obtained from TomatoExoPlayer if needed by it
    private val tomatoExoPlayer: TomatoExoPlayer,
    private val mediaSourceFactory: MediaSourceFactory
) {

    val playerStateFlow: StateFlow<PlayerState> = tomatoExoPlayer.playerStateFlow

    // No explicit initializePlayer() needed if TomatoExoPlayer handles its own init.
    // If specific setup is required when player screen is created, it can be added.
    // For example, setting up audio focus, media session, etc.
    // fun initializePlayer() { /* ... */ }

    fun playMedia(mediaUrl: String, playWhenReady: Boolean = true) {
        tomatoExoPlayer.prepareMedia(mediaUrl, playWhenReady, mediaSourceFactory)
    }

    fun pausePlayback() {
        tomatoExoPlayer.pause()
    }

    fun resumePlayback() {
        tomatoExoPlayer.play()
    }

    fun seekPlayback(positionMs: Long) {
        tomatoExoPlayer.seekTo(positionMs)
    }

    /**
     * Releases the player resources. Should be called when the player is no longer needed,
     * e.g., when the player screen is destroyed or the app is closing.
     */
    fun release() {
        tomatoExoPlayer.releasePlayer()
    }

    // --- Delegated methods for more specific controls if needed ---

    fun getExoPlayerInstance(): androidx.media3.exoplayer.ExoPlayer {
        return tomatoExoPlayer.exoPlayer
    }

    fun setSubtitleTrack(trackIndex: Int, groupIndex: Int, parametersBuilder: androidx.media3.common.TrackSelectionParameters.Builder? = null) {
        tomatoExoPlayer.setSubtitleTrack(trackIndex, groupIndex, parametersBuilder)
    }

    fun clearSubtitleTrack() {
        tomatoExoPlayer.clearSubtitleTrack()
    }

    fun getAvailableSubtitleTracks(): List<androidx.media3.common.TrackGroup> {
        return tomatoExoPlayer.getAvailableSubtitleTracks()
    }
}
