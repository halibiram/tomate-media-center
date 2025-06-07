package com.halibiram.tomato.core.player.exoplayer

import android.content.Context
import androidx.media3.common.C // For C.TRACK_TYPE_TEXT, C.TRACK_TYPE_AUDIO
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
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

@Singleton
class TomatoExoPlayer @Inject constructor(@ApplicationContext private val context: Context) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerStateFlow: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var positionUpdateJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun getExoPlayerInstance(): ExoPlayer = exoPlayer

    init {
        // Initialize with default track selection parameters
        _playerState.update { it.copy(trackSelectionParameters = exoPlayer.trackSelectionParameters) }
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
                if (!timeline.isEmpty && exoPlayer.duration > 0) {
                    _playerState.update { it.copy(durationMs = exoPlayer.duration) }
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                val subtitleTrackGroups = mutableListOf<TrackGroup>()
                val audioTrackGroups = mutableListOf<TrackGroup>()

                for (groupInfo in tracks.groups) {
                    if (groupInfo.isSupported) { // Consider only supported tracks
                        when (groupInfo.type) {
                            C.TRACK_TYPE_TEXT -> subtitleTrackGroups.add(groupInfo.mediaTrackGroup)
                            C.TRACK_TYPE_AUDIO -> audioTrackGroups.add(groupInfo.mediaTrackGroup)
                        }
                    }
                }
                _playerState.update {
                    it.copy(
                        availableSubtitleTracks = subtitleTrackGroups,
                        availableAudioTracks = audioTrackGroups,
                        // Update selected tracks based on current parameters if needed,
                        // or rely on onTrackSelectionParametersChanged
                        trackSelectionParameters = exoPlayer.trackSelectionParameters
                    )
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                _playerState.update { it.copy(videoWidth = videoSize.width, videoHeight = videoSize.height) }
            }

            override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
                _playerState.update { it.copy(trackSelectionParameters = parameters) }
            }
        })
    }

    fun prepareMedia(mediaUrl: String, playWhenReady: Boolean, mediaSourceFactory: MediaSourceFactory) {
        _playerState.update { PlayerState(isLoading = true, trackSelectionParameters = exoPlayer.trackSelectionParameters) }
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
        _playerState.update { it.copy(currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)) }
    }

    fun releasePlayer() {
        stopPositionUpdates()
        coroutineScope.coroutineContext.cancelChildren()
        exoPlayer.release()
    }

    fun selectSubtitleTrack(trackGroup: TrackGroup, trackIndex: Int) {
        val override = TrackSelectionOverride(trackGroup, listOf(trackIndex))
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setOverrideForType(override)
            .setTrackSelectionDisabled(trackGroup.type, false) // Ensure text tracks are enabled
            .build()
    }

    fun clearSubtitleTrack() { // Keep this specific version for clarity from ViewModel
        // To disable subtitles, we clear overrides for text tracks and disable the type.
        val parametersBuilder = exoPlayer.trackSelectionParameters.buildUpon()
        parametersBuilder.clearOverridesOfType(C.TRACK_TYPE_TEXT)
        parametersBuilder.setTrackSelectionDisabled(C.TRACK_TYPE_TEXT, true)
        exoPlayer.trackSelectionParameters = parametersBuilder.build()
    }

    fun selectAudioTrack(trackGroup: TrackGroup, trackIndex: Int) {
        val override = TrackSelectionOverride(trackGroup, listOf(trackIndex))
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setOverrideForType(override)
            .setTrackSelectionDisabled(trackGroup.type, false) // Ensure audio tracks are enabled (usually by default)
            .build()
    }

    // This might not be needed if selecting another audio track implicitly clears the previous one,
    // or if default behavior is desired. Typically, one audio track is always active.
    // fun clearAudioTrack(trackGroup: TrackGroup) {
    //     exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
    //         .buildUpon()
    //         .clearTrackSelectionOverrides(trackGroup.mediaTrackGroup)
    //         .build()
    // }

    // getAvailableSubtitleTracks and getAvailableAudioTracks are now implicitly handled by
    // playerStateFlow.value.availableSubtitleTracks and playerStateFlow.value.availableAudioTracks

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = coroutineScope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
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
