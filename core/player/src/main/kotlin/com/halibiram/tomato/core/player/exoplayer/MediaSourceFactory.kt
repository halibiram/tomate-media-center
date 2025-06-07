package com.halibiram.tomato.core.player.exoplayer

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaSourceFactory @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataSourceFactory: DefaultDataSource.Factory by lazy {
        DefaultDataSource.Factory(context)
    }

    fun createMediaSource(mediaUrl: String): MediaSource {
        val mediaItem = MediaItem.fromUri(mediaUrl)

        // Infer content type from URL or use more sophisticated type detection if needed
        val type = Util.inferContentType(mediaItem.localConfiguration?.uri ?: mediaItem.mediaId)

        return when (type) {
            androidx.media3.common.C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            androidx.media3.common.C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            androidx.media3.common.C.CONTENT_TYPE_SS, // SmoothStreaming
            androidx.media3.common.C.CONTENT_TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            else -> {
                // Default to ProgressiveMediaSource if type is unknown or not explicitly handled
                // Log a warning or handle as an error if strict type matching is required
                // Log.w("MediaSourceFactory", "Unknown media type for URL: $mediaUrl. Defaulting to ProgressiveMediaSource.")
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
            }
        }
    }
}
