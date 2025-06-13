package com.halibiram.tomato.core.player.exoplayer

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

// MediaSourceFactory for ExoPlayer
class MediaSourceFactory(private val context: Context) {

    fun createProgressiveMediaSource(url: String): MediaSource {
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaItem = MediaItem.fromUri(url)
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
    }

    // Add methods for other media source types like HLS, Dash, etc.
    // fun createHlsMediaSource(url: String): MediaSource { ... }
    // fun createDashMediaSource(url: String): MediaSource { ... }
}
