package com.halibiram.tomato.core.player.exoplayer

import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionParameters

// Using androidx.media3.common.Player constants
data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isLoading: Boolean = false, // True when player is in STATE_BUFFERING or preparing
    val error: Throwable? = null, // Holds player errors
    @Player.State val playbackState: Int = Player.STATE_IDLE,
    val availableSubtitleTracks: List<TrackGroup> = emptyList(),
    val selectedSubtitleTrackParameters: TrackSelectionParameters? = null, // Store the parameters used to select a track
    val videoWidth: Int = 0,
    val videoHeight: Int = 0
)
