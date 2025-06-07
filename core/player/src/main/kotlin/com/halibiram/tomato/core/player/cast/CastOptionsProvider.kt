package com.halibiram.tomato.core.player.cast

import android.content.Context
// import com.google.android.gms.cast.framework.CastOptions
// import com.google.android.gms.cast.framework.OptionsProvider
// import com.google.android.gms.cast.framework.SessionProvider
// import com.google.android.gms.cast.framework.media.CastMediaOptions

// CastOptionsProvider
// class CastOptionsProvider : OptionsProvider {
//     override fun getCastOptions(appContext: Context): CastOptions {
//         // val mediaOptions = CastMediaOptions.Builder()
//         //     // Configure media options if needed
//         //     .build()
//
//         return CastOptions.Builder()
//             .setReceiverApplicationId("YOUR_APP_ID") // Replace with your actual Cast App ID
//             // .setCastMediaOptions(mediaOptions)
//             .build()
//     }
//
//     override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
//         return null
//     }
// }

// Placeholder if not using full Google Cast SDK initialization via manifest
object DummyCastOptionsProvider {}
