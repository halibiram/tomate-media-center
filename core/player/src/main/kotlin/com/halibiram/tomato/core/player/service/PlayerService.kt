package com.halibiram.tomato.core.player.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.halibiram.tomato.core.player.exoplayer.PlayerManager
import com.halibiram.tomato.core.player.notification.TomatoPlayerNotificationManager

// PlayerService for background playback using Media3
class PlayerService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var playerManager: PlayerManager
    private lateinit var notificationManager: TomatoPlayerNotificationManager

    // Binder for clients
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    override fun onCreate() {
        super.onCreate()
        playerManager = PlayerManager(this)
        notificationManager = TomatoPlayerNotificationManager(
            this,
            "tomato_player_channel", // Ensure this channel is created
            123
        )

        val player = playerManager.getPlayer() ?: ExoPlayer.Builder(this).build().also {
            // If playerManager didn't init one, create a default one
            // This scenario needs careful handling of player instance across classes
        }

        mediaSession = MediaSession.Builder(this, player).build()
        notificationManager.showNotificationForPlayer(player)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    fun getPlayerInstance(): ExoPlayer? = playerManager.getPlayer()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // Handle intents, e.g., play specific media
        return START_STICKY
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        notificationManager.hideNotification()
        playerManager.releasePlayer() // Ensure player manager also cleans up
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent) // Important for MediaSessionService
        return binder
    }
}
