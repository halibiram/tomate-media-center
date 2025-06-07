package com.halibiram.tomato.core.player.exoplayer

import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks // Needed for selected tracks comparison
import androidx.media3.common.Format // For selected track format
import androidx.media3.common.TrackSelectionParameters


// Using androidx.media3.common.Player constants
data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    @Player.State val playbackState: Int = Player.STATE_IDLE,

    // Track related states
    val availableSubtitleTracks: List<TrackGroup> = emptyList(),
    val availableAudioTracks: List<TrackGroup> = emptyList(),

    // To determine which track is selected, we can inspect TrackSelectionParameters
    // or directly store the selected Format if simpler for UI.
    // Storing the whole parameters object is more robust for ExoPlayer.
    val trackSelectionParameters: TrackSelectionParameters = TrackSelectionParameters.DEFAULT_WITHOUT_CONTEXT,

    val videoWidth: Int = 0,
    val videoHeight: Int = 0
) {
    // Helper properties to get selected track formats (can be null if no track or override)
    val selectedSubtitleTrackFormat: Format?
        get() {
            val textOverride = trackSelectionParameters.overrides
                .entries.firstOrNull { it.key.type == androidx.media3.common.C.TRACK_TYPE_TEXT }?.value
            return textOverride?.mediaTrackGroup?.getFormat(textOverride.trackIndices.firstOrNull() ?: 0)
        }

    val selectedAudioTrackFormat: Format?
        get() {
            val audioOverride = trackSelectionParameters.overrides
                .entries.firstOrNull { it.key.type == androidx.media3.common.C.TRACK_TYPE_AUDIO }?.value
            return audioOverride?.mediaTrackGroup?.getFormat(audioOverride.trackIndices.firstOrNull() ?: 0)
        }
}
