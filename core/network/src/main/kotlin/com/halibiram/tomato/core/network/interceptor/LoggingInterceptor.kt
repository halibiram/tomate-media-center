package com.halibiram.tomato.core.network.interceptor

import android.util.Log

// Simple logger for Ktor. Ktor's `Logging` plugin can be configured with a custom `Logger`.
object LoggingInterceptor {
    private const val TAG = "KtorHttpClient"

    fun log(message: String) {
        // In a real app, you might use a more sophisticated logging library like Timber.
        Log.d(TAG, message)
    }
}
