package com.halibiram.tomato.core.player.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.MediaIntentReceiver
import com.google.android.gms.cast.framework.media.NotificationOptions

class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        // Example NotificationOptions - customize as needed
        val notificationOptions = NotificationOptions.Builder()
            .setActions(
                listOf(
                    MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                    MediaIntentReceiver.ACTION_STOP_CASTING
                ),
                intArrayOf(0, 1) // Compact view actions
            )
            .setTargetActivityClassName(context.packageName + ".ui.screens.MainActivity") // Replace with your player activity if different
            .build()

        // Example CastMediaOptions - customize as needed
        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .setImagePicker(null) // Default image picker or provide your own com.google.android.gms.cast.framework.media.ImagePicker
            .build()

        return CastOptions.Builder()
            .setReceiverApplicationId(com.google.android.gms.cast.CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
            // Or use your custom receiver ID: .setReceiverApplicationId("YOUR_CUSTOM_RECEIVER_APP_ID")
            .setCastMediaOptions(mediaOptions)
            // .setEnableReconnectionService(true) // Optional: for reconnecting to sessions
            // .setLaunchOptions(LaunchOptions.Builder().setAndroidReceiverCompatible(true).build()) // Optional
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        // Return null if you are only using the default CastSessionProvider
        return null
    }
}
