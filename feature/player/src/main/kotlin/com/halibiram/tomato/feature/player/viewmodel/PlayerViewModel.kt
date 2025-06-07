package com.halibiram.tomato.feature.player.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private var _player: ExoPlayer? = null
    val player: ExoPlayer
        get() = _player ?: ExoPlayer.Builder(application).build().also {
            _player = it
            it.addListener(playerListener)
            // Don't auto-start position updates here, player might not be ready
            // startPositionUpdates()
        }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _bufferedPosition = MutableStateFlow(0L)
    val bufferedPosition: StateFlow<Long> = _bufferedPosition.asStateFlow()

    private var positionUpdateJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlayingValue: Boolean) {
            _isPlaying.value = isPlayingValue
            if (isPlayingValue) {
                startPositionUpdates()
            } else {
                stopPositionUpdates()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _totalDuration.value = player.duration.coerceAtLeast(0L)
                // If player becomes ready and is playing, start position updates
                if (player.isPlaying) {
                    startPositionUpdates()
                }
            } else if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                stopPositionUpdates()
                 _currentPosition.value = 0 // Reset position or handle as per app logic
                if(playbackState == Player.STATE_ENDED) _currentPosition.value = _totalDuration.value // Show end
            }
        }

        override fun onTimelineChanged(timeline: Player.Timeline, reason: Int) {
            if (!timeline.isEmpty) {
                 _totalDuration.value = player.duration.coerceAtLeast(0L)
            } else {
                // Timeline is empty, perhaps media source is invalid or cleared
                _totalDuration.value = 0L
                _currentPosition.value = 0L
                _bufferedPosition.value = 0L
                stopPositionUpdates()
            }
        }
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = viewModelScope.launch {
            while (isActive) {
                _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                _bufferedPosition.value = player.bufferedPosition.coerceAtLeast(0L)
                delay(1000)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    fun preparePlayer(mediaUri: Uri) {
        val mediaItem = MediaItem.fromUri(mediaUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        // Initial states after player is prepared
        _isPlaying.value = player.isPlaying
        _totalDuration.value = player.duration.coerceAtLeast(0L) // May be 0 if duration is unknown yet
        _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
        _bufferedPosition.value = player.bufferedPosition.coerceAtLeast(0L)

        if (player.isPlaying) {
            startPositionUpdates()
        }
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun seekForward() {
        player.seekForward()
        _currentPosition.value = player.currentPosition.coerceAtLeast(0L) // Update immediately
    }

    fun seekRewind() {
        player.seekBack()
        _currentPosition.value = player.currentPosition.coerceAtLeast(0L) // Update immediately
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceIn(0, _totalDuration.value)) // Ensure seek is within bounds
        _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
    }

    fun nextTrack() { /* TODO */ }
    fun previousTrack() { /* TODO */ }

    override fun onCleared() {
        super.onCleared()
        stopPositionUpdates()
        // It's important to remove the listener before releasing the player
        // to avoid callbacks on a released player instance.
        _player?.removeListener(playerListener)
        _player?.release()
        _player = null
    }
}
