package com.halibiram.tomato.core.player.exoplayer

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

// PlayerManager class
class PlayerManager(context: Context) {
    private var exoPlayer: ExoPlayer? = TomatoExoPlayer.build(context)

    fun getPlayer(): ExoPlayer? = exoPlayer

    fun playUrl(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    fun addListener(listener: Player.Listener) {
        exoPlayer?.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        exoPlayer?.removeListener(listener)
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
