package com.halibiram.tomato.core.datastore.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.serialization.Serializable

// PlayerPreferences keys and data structure
object PlayerPreferencesKeys {
    val PLAYER_MUTED = booleanPreferencesKey("player_muted")
    val PLAYER_VOLUME = floatPreferencesKey("player_volume")
    val PLAYER_SUBTITLE_SIZE = intPreferencesKey("player_subtitle_size")
    // Add other player-specific keys
}

@Serializable
data class PlayerPreferences(
    val isMuted: Boolean = false,
    val volume: Float = 1.0f,
    val subtitleSize: Int = 100 // As a percentage
    // Add other fields
)
