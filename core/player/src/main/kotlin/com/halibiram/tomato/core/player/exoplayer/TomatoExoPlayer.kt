package com.halibiram.tomato.core.player.exoplayer

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

// TomatoExoPlayer custom wrapper or utility
object TomatoExoPlayer {
    fun build(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }
}
