package com.halibiram.tomato.core.player.notification

import android.app.Notification
import android.content.Context
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager

// PlayerNotificationManager wrapper for Media3
class TomatoPlayerNotificationManager(
    private val context: Context,
    private val channelId: String,
    private val notificationId: Int
) {
    private var playerNotificationManager: PlayerNotificationManager? = null

    fun showNotificationForPlayer(player: Player) {
        if (playerNotificationManager == null) {
            playerNotificationManager = PlayerNotificationManager.Builder(
                context,
                notificationId,
                channelId
            )
            // .setMediaDescriptionAdapter(adapter) // Optional: for richer notification content
            // .setNotificationListener(listener) // Optional: for notification events
            .build()
        }
        playerNotificationManager?.setPlayer(player)
    }

    fun hideNotification() {
        playerNotificationManager?.setPlayer(null) // This hides the notification
    }

    // You might need a MediaDescriptionAdapter
    // private val adapter = object : PlayerNotificationManager.MediaDescriptionAdapter { ... }

    // And a NotificationListener
    // private val listener = object : PlayerNotificationManager.NotificationListener { ... }

    fun release() {
        playerNotificationManager?.setPlayer(null) // Important to release the player from the manager
        // Actual manager release is not explicitly available, it depends on service lifecycle or player release
    }
}
