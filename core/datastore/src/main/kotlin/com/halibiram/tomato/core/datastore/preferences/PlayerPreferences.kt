package com.halibiram.tomato.core.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class PlayerSettings(
    val preferredSubtitleLanguage: String,
    val playbackSpeed: Float,
    val autoPlayNext: Boolean,
    val preferredQuality: Int // e.g., 0 for Auto, 480, 720, 1080
)

class PlayerPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        const val PLAYER_PREFERENCES_NAME = "player_prefs"
        private val PREFERRED_SUBTITLE_LANG_KEY = stringPreferencesKey("preferred_subtitle_language")
        private val PLAYBACK_SPEED_KEY = floatPreferencesKey("playback_speed")
        private val AUTO_PLAY_NEXT_KEY = booleanPreferencesKey("auto_play_next")
        private val PREFERRED_QUALITY_KEY = intPreferencesKey("preferred_quality")

        const val DEFAULT_SUBTITLE_LANGUAGE = "en" // English
        const val DEFAULT_PLAYBACK_SPEED = 1.0f
        const val DEFAULT_AUTO_PLAY_NEXT = true
        const val DEFAULT_QUALITY = 0 // Auto
    }

    val playerSettingsFlow: Flow<PlayerSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val subtitleLang = preferences[PREFERRED_SUBTITLE_LANG_KEY] ?: DEFAULT_SUBTITLE_LANGUAGE
            val speed = preferences[PLAYBACK_SPEED_KEY] ?: DEFAULT_PLAYBACK_SPEED
            val autoPlay = preferences[AUTO_PLAY_NEXT_KEY] ?: DEFAULT_AUTO_PLAY_NEXT
            val quality = preferences[PREFERRED_QUALITY_KEY] ?: DEFAULT_QUALITY
            PlayerSettings(subtitleLang, speed, autoPlay, quality)
        }

    suspend fun updatePreferredSubtitleLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_SUBTITLE_LANG_KEY] = language
        }
    }

    suspend fun updatePlaybackSpeed(speed: Float) {
        dataStore.edit { preferences ->
            preferences[PLAYBACK_SPEED_KEY] = speed
        }
    }

    suspend fun toggleAutoPlayNext(enable: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_PLAY_NEXT_KEY] = enable
        }
    }

    suspend fun updatePreferredQuality(quality: Int) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_QUALITY_KEY] = quality
        }
    }
}
