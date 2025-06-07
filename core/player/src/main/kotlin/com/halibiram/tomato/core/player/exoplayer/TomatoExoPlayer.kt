package com.halibiram.tomato.core.player.exoplayer

import android.content.Context
import androidx.media3.common.C // For C.TRACK_TYPE_TEXT
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Assuming TomatoExoPlayer might be a singleton or scoped if player persists
class TomatoExoPlayer @Inject constructor(@ApplicationContext private val context: Context) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerStateFlow: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var positionUpdateJob: Job? = null
    // Use SupervisorJob so if one child coroutine fails, it doesn't cancel the whole scope
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Public accessor for the ExoPlayer instance
    fun getExoPlayerInstance(): ExoPlayer = exoPlayer

    init {
        setupPlayerListener()
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
                _playerState.update {
                    it.copy(
                        playbackState = playbackState,
                        isLoading = playbackState == Player.STATE_BUFFERING,
                        // Update duration here as it might become available when ready
                        durationMs = if (exoPlayer.duration > 0) exoPlayer.duration else it.durationMs
                    )
                }
                 if (playbackState == Player.STATE_ENDED) {
                    stopPositionUpdates()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _playerState.update { it.copy(error = error, isLoading = false, isPlaying = false) }
                stopPositionUpdates()
            }

            override fun onTimelineChanged(timeline: Player.Timeline, reason: Int) {
                if (!timeline.isEmpty && exoPlayer.duration > 0) { // Check if duration is positive
                    _playerState.update { it.copy(durationMs = exoPlayer.duration) }
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                val subtitleTrackGroups = mutableListOf<TrackGroup>()
                for (groupInfo in tracks.groups) {
                    if (groupInfo.type == C.TRACK_TYPE_TEXT && groupInfo.isSupported) {
                        subtitleTrackGroups.add(groupInfo.mediaTrackGroup)
                    }
                }
                _playerState.update { it.copy(availableSubtitleTracks = subtitleTrackGroups) }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                _playerState.update { it.copy(videoWidth = videoSize.width, videoHeight = videoSize.height) }
            }

            override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
                 // Check if text tracks are disabled or a specific one is selected
                _playerState.update { it.copy(selectedSubtitleTrackParameters = parameters) }
            }
        })
    }

    // MediaSourceFactory will be injected into PlayerManager, which then passes it here.
    fun prepareMedia(mediaUrl: String, playWhenReady: Boolean, mediaSourceFactory: MediaSourceFactory) {
        _playerState.update { PlayerState(isLoading = true) } // Reset to a clean loading state
        try {
            val mediaSource = mediaSourceFactory.createMediaSource(mediaUrl)
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.playWhenReady = playWhenReady
            exoPlayer.prepare()
        } catch (e: Exception) {
            _playerState.update { it.copy(error = e, isLoading = false) }
        }
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun seekTo(positionMs: Long) {
        val validPosition = positionMs.coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
        exoPlayer.seekTo(validPosition)
        // Update state immediately for responsiveness, listener will also update
        _playerState.update { it.copy(currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)) }
    }

    fun releasePlayer() {
        stopPositionUpdates()
        coroutineScope.coroutineContext.cancelChildren() // Cancel jobs in this scope
        exoPlayer.release()
    }

    fun setSubtitleTrack(trackIndex: Int, groupIndex: Int, parameters: TrackSelectionParameters.Builder? = null) {
        val trackSelectionBuilder = parameters ?: exoPlayer.trackSelectionParameters.buildUpon()

        // Find the correct TrackGroup based on available tracks
        val availableTracks = exoPlayer.currentTracks
        var actualGroupIndex = -1
        var textGroupCount = 0
        for(i in 0 until availableTracks.groups.size) {
            if(availableTracks.groups[i].type == C.TRACK_TYPE_TEXT) {
                if(textGroupCount == groupIndex) {
                    actualGroupIndex = i
                    break
                }
                textGroupCount++
            }
        }

        if (actualGroupIndex != -1) {
            val trackGroup = availableTracks.groups[actualGroupIndex].mediaTrackGroup
            trackSelectionBuilder
                .clearOverridesOfType(C.TRACK_TYPE_TEXT) // Clear previous text track overrides
                .addOverride(TrackSelectionParameters.TrackSelectionOverride(trackGroup, trackIndex))
                .setTrackSelectionDisabled(C.TRACK_TYPE_TEXT, false) // Ensure text tracks are enabled
        } else {
            // Track group not found, disable subtitles
            trackSelectionBuilder.setTrackSelectionDisabled(C.TRACK_TYPE_TEXT, true)
        }
        exoPlayer.trackSelectionParameters = trackSelectionBuilder.build()
    }

    fun clearSubtitleTrack() {
        val parametersBuilder = exoPlayer.trackSelectionParameters.buildUpon()
        parametersBuilder.clearOverridesOfType(C.TRACK_TYPE_TEXT)
        parametersBuilder.setTrackSelectionDisabled(C.TRACK_TYPE_TEXT, true)
        exoPlayer.trackSelectionParameters = parametersBuilder.build()
    }

    fun getAvailableSubtitleTracks(): List<TrackGroup> {
        val trackGroups = exoPlayer.currentTracks.groups
        val subtitleTrackGroups = mutableListOf<TrackGroup>()
        for (i in 0 until trackGroups.size) {
            if (trackGroups[i].type == C.TRACK_TYPE_TEXT && trackGroups[i].isSupported) {
                subtitleTrackGroups.add(trackGroups[i].mediaTrackGroup)
            }
        }
        return subtitleTrackGroups
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = coroutineScope.launch {
            while (isActive) { // Loop while coroutine is active
                if (exoPlayer.isPlaying) { // Only update if playing
                    _playerState.update {
                        it.copy(currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L))
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
}
