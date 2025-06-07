package com.halibiram.tomato.feature.player.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private var _player: ExoPlayer? = null
    val player: ExoPlayer get() = _player ?: ExoPlayer.Builder(application).build().also { _player = it }

    fun preparePlayer(mediaUri: Uri) {
        val mediaItem = MediaItem.fromUri(mediaUri)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
        _player = null
    }
}
