package com.halibiram.tomato.core.player.service

import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.halibiram.tomato.core.player.exoplayer.PlayerManager // Assuming PlayerManager provides ExoPlayer
import com.halibiram.tomato.core.player.exoplayer.TomatoExoPlayer
import com.halibiram.tomato.core.player.notification.PlayerNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : MediaSessionService() {

    @Inject
    lateinit var playerManager: PlayerManager // Injected PlayerManager

    // Or inject TomatoExoPlayer directly if PlayerManager doesn't expose ExoPlayer instance easily for MediaSession
    @Inject
    lateinit var tomatoExoPlayer: TomatoExoPlayer // Assuming this holds the actual ExoPlayer instance

    @Inject
    lateinit var notificationManager: PlayerNotificationManager

    private var mediaSession: MediaSession? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    // MediaSession.Callback implementation
    private inner class TomatoMediaSessionCallback : MediaSession.Callback {
        override fun onPlay(mediaSession: MediaSession, controller: MediaSession.ControllerInfo): Int {
            playerManager.resumePlayback()
            return MediaSession.Callback.RESULT_SUCCESS
        }

        override fun onPause(mediaSession: MediaSession, controller: MediaSession.ControllerInfo): Int {
            playerManager.pausePlayback()
            return MediaSession.Callback.RESULT_SUCCESS
        }

        override fun onSeekTo(mediaSession: MediaSession, controller: MediaSession.ControllerInfo, positionMs: Long): Int {
            playerManager.seekPlayback(positionMs)
            return MediaSession.Callback.RESULT_SUCCESS
        }

        // Implement other callbacks as needed: onStop, onSetMediaItem, onFastForward, onRewind, etc.
        // For example, to handle media items sent from a MediaController:
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): com.google.common.util.concurrent.ListenableFuture<MutableList<MediaItem>> {
            // Here you would typically convert these MediaItems to your app's format or
            // directly prepare them for playback using PlayerManager/TomatoExoPlayer.
            // For now, just add them to the player's playlist.
            val updatedMediaItems = mediaItems.map { mediaItem ->
                // If your MediaItem needs specific LocalConfiguration (e.g., custom headers for URL)
                // you might need to rebuild it here.
                mediaItem
            }.toMutableList()

            tomatoExoPlayer.exoPlayer.addMediaItems(updatedMediaItems)
            // playerManager.playMedia(updatedMediaItems.first().mediaId ?: "", playWhenReady = false) // Example
            return com.google.common.util.concurrent.Futures.immediateFuture(updatedMediaItems)
        }

        override fun onSetMediaItem(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItem: MediaItem,
            startPositionMs: Long
        ): Int {
            // playerManager.playMedia(mediaItem.mediaId ?: mediaItem.requestMetadata.mediaUri.toString(), startPositionMs > 0)
            // This needs more robust handling of mediaItem to URL or ID.
            mediaItem.requestMetadata.mediaUri?.toString()?.let {
                playerManager.playMedia(it, playWhenReady = false) // playWhenReady might be true
                if (startPositionMs > 0) {
                    playerManager.seekPlayback(startPositionMs)
                }
            }
            return MediaSession.Callback.RESULT_SUCCESS
        }
    }

    override fun onCreate() {
        super.onCreate()
        val exoPlayerInstance = tomatoExoPlayer.exoPlayer // Get the ExoPlayer instance

        // Set audio attributes for ExoPlayer (important for background play, focus handling)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE) // Or MUSIC, SPEECH
            .setUsage(C.USAGE_MEDIA)
            .build()
        exoPlayerInstance.setAudioAttributes(audioAttributes, true /* handleAudioFocus */)


        mediaSession = MediaSession.Builder(this, exoPlayerInstance)
            .setCallback(TomatoMediaSessionCallback())
            // .setId("TomatoMediaSession") // Optional: Unique ID for the session
            .build()

        // Observe player state to show/hide notification
        serviceScope.launch {
            tomatoExoPlayer.playerStateFlow.collectLatest { playerState ->
                if (playerState.isPlaying || playerState.isLoading) { // Or more specific conditions
                    mediaSession?.let { session ->
                        notificationManager.showNotification(exoPlayerInstance, session, this@PlayerService)
                    }
                } else {
                    // If playback is paused and not loading, notification might stay or be dismissible
                    // If playback stopped/errored and notification is dismissed, service should stop.
                    // This logic is partially in PlayerNotificationManager's listener.
                    // Potentially stop foreground if paused for a long time or explicitly stopped.
                }
            }
        }
    }

    // This is called when a MediaBrowser (like Android Auto or Google Assistant) connects.
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId) // Important for MediaSessionService
        // Handle custom intents, e.g., to start playback with a specific media URL
        // intent?.getStringExtra("MEDIA_URL_EXTRA")?.let { mediaUrl ->
        //     playerManager.playMedia(mediaUrl)
        // }
        return START_STICKY // Or START_NOT_STICKY / START_REDELIVER_INTENT based on needs
    }


    override fun onDestroy() {
        serviceJob.cancel() // Cancel coroutines
        mediaSession?.run {
            player.release() // Release player associated with session
            release()        // Release the session itself
            mediaSession = null
        }
        notificationManager.release() // Clean up notification manager resources
        super.onDestroy()
    }
}
