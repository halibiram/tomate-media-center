package com.halibiram.tomato.core.datastore.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.Serializable

// AppPreferences keys and data structure
object AppPreferencesKeys {
    val APP_THEME = stringPreferencesKey("app_theme") // "light", "dark", "system"
    val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    // Add other app-wide keys
}

@Serializable
data class AppPreferences(
    val theme: String = "system",
    val isFirstLaunch: Boolean = true
    // Add other fields
)
