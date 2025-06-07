package com.halibiram.tomato.core.player.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager as SystemNotificationManager // Alias to avoid conflict
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.common.MediaMetadata // For MediaDescriptionAdapter
import androidx.media3.session.MediaSession // For MediaSession Token
import androidx.media3.ui.PlayerNotificationManager as Media3PlayerNotificationManager // Alias
import com.halibiram.tomato.core.player.R // Assuming R class is generated in core.player for resources
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "tomato_playback_channel"
        const val CHANNEL_NAME = "Tomato Playback"
    }

    private var m3PlayerNotificationManager: Media3PlayerNotificationManager? = null

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                SystemNotificationManager.IMPORTANCE_LOW // Low importance for ongoing playback
            ).apply {
                description = "Notifications for media playback"
                // Set other channel properties if needed (e.g., setShowBadge(false))
            }
            val notificationManager = ContextCompat.getSystemService(context, SystemNotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        player: Player,
        mediaSession: MediaSession, // Pass the MediaSession to link notification with it
        service: androidx.media3.session.MediaSessionService // Needed for startForeground
    ) {
        if (m3PlayerNotificationManager == null) {
            val mediaDescriptionAdapter = object : Media3PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return player.mediaMetadata.title ?: player.mediaMetadata.albumTitle ?: "Unknown Title"
                }

                override fun createCurrentContentIntent(player: Player): android.app.PendingIntent? {
                    // Intent to open the app's player UI when notification is clicked
                    val packageManager = context.packageManager
                    val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)
                    // Make this more specific to navigate to the player screen if possible
                    return launchIntent?.let {
                        android.app.PendingIntent.getActivity(context, 0, it, android.app.PendingIntent.FLAG_IMMUTABLE)
                    }
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return player.mediaMetadata.artist ?: player.mediaMetadata.albumArtist
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: Media3PlayerNotificationManager.BitmapCallback
                ): android.graphics.Bitmap? {
                    // Load artwork bitmap here asynchronously if needed, then call callback.onBitmap(bitmap)
                    // For synchronous, return it directly. Placeholder:
                    // player.mediaMetadata.artworkUri?.let { ... load bitmap ... }
                    return null // Placeholder for now
                }
            }

            m3PlayerNotificationManager = Media3PlayerNotificationManager.Builder(
                context,
                NOTIFICATION_ID,
                CHANNEL_ID
            )
            .setMediaDescriptionAdapter(mediaDescriptionAdapter)
            .setNotificationListener(object : Media3PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    if (ongoing) {
                        // Start service in foreground
                        ContextCompat.startForegroundService(context, android.content.Intent(context, service::class.java))
                        service.startForeground(notificationId, notification)
                    } else {
                        // Notification is dismissible, stop foreground
                        service.stopForeground(false) // Don't remove notification if it's just paused
                    }
                }

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    // Stop service and player if dismissed by user
                    service.stopForeground(true) // Remove notification
                    service.stopSelf()
                    player.stop() // Or player.pause() depending on desired behavior
                    player.clearMediaItems() // Or just player.stop()
                }
            })
            // .setChannelNameResourceId(R.string.playback_channel_name) // From strings.xml
            // .setChannelDescriptionResourceId(R.string.playback_channel_description) // From strings.xml
            .setSmallIconResourceId(R.drawable.ic_stat_player_notification) // Ensure this drawable exists
            // Add actions like Next, Previous, Play/Pause etc.
            // .setUseNextAction(true)
            // .setUsePreviousAction(true)
            .build().apply {
                setPlayer(player)
                setMediaSessionToken(mediaSession.sessionCompatToken) // Link to MediaSession
                setUseChronometer(true)
            }
        }
        // If already created, just ensure player and session token are set (might be redundant if not changing)
        m3PlayerNotificationManager?.setPlayer(player)
        m3PlayerNotificationManager?.setMediaSessionToken(mediaSession.sessionCompatToken)
    }

    fun hideNotification() {
        m3PlayerNotificationManager?.setPlayer(null) // Detach player to hide notification
        // Or context.getSystemService(SystemNotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    // Call this when the service is destroyed to release the notification manager
    fun release() {
        hideNotification()
        m3PlayerNotificationManager = null
    }
}
