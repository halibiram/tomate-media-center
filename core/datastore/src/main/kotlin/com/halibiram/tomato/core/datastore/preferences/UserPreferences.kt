package com.halibiram.tomato.core.datastore.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.Serializable

// UserPreferences keys and data structure
object UserPreferencesKeys {
    val USER_ID = stringPreferencesKey("user_id")
    val USER_NAME = stringPreferencesKey("user_name")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    // Add other user-specific keys
}

@Serializable
data class UserPreferences(
    val userId: String? = null,
    val userName: String? = null,
    val isLoggedIn: Boolean = false
    // Add other fields
)
