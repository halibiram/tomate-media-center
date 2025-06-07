package com.halibiram.tomato.core.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

data class PlayerPreferencesData(
    val defaultSubtitleLanguage: String,
    val preferredResolution: String, // e.g., "Auto", "1080p", "720p", "480p"
    val autoPlayNext: Boolean,
    val seekIncrementSeconds: Int, // e.g., 5, 10, 15, 30 seconds
    val playbackSpeed: Float // Added from previous version of this file
)

class PlayerPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {

    companion object {
        internal val DEFAULT_SUBTITLE_LANGUAGE_KEY = stringPreferencesKey("default_subtitle_language")
        internal val PREFERRED_RESOLUTION_KEY = stringPreferencesKey("preferred_resolution")
        internal val AUTO_PLAY_NEXT_KEY = booleanPreferencesKey("auto_play_next")
        internal val SEEK_INCREMENT_SECONDS_KEY = intPreferencesKey("seek_increment_seconds")
        internal val PLAYBACK_SPEED_KEY = floatPreferencesKey("playback_speed") // Added

        // Default values
        const val DEFAULT_SUBTITLE_LANGUAGE = "en"
        const val DEFAULT_RESOLUTION = "Auto"
        const val DEFAULT_AUTO_PLAY_NEXT = true
        const val DEFAULT_SEEK_INCREMENT_SECONDS = 10
        const val DEFAULT_PLAYBACK_SPEED = 1.0f
    }

    val playerPreferencesFlow: Flow<PlayerPreferencesData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapPlayerPreferences(preferences)
        }

    private fun mapPlayerPreferences(preferences: Preferences): PlayerPreferencesData {
        val subtitleLang = preferences[DEFAULT_SUBTITLE_LANGUAGE_KEY] ?: DEFAULT_SUBTITLE_LANGUAGE
        val resolution = preferences[PREFERRED_RESOLUTION_KEY] ?: DEFAULT_RESOLUTION
        val autoPlay = preferences[AUTO_PLAY_NEXT_KEY] ?: DEFAULT_AUTO_PLAY_NEXT
        val seekIncrement = preferences[SEEK_INCREMENT_SECONDS_KEY] ?: DEFAULT_SEEK_INCREMENT_SECONDS
        val speed = preferences[PLAYBACK_SPEED_KEY] ?: DEFAULT_PLAYBACK_SPEED

        return PlayerPreferencesData(subtitleLang, resolution, autoPlay, seekIncrement, speed)
    }

    suspend fun updateDefaultSubtitleLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_SUBTITLE_LANGUAGE_KEY] = language
        }
    }

    suspend fun updatePreferredResolution(resolution: String) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_RESOLUTION_KEY] = resolution
        }
    }

    suspend fun updateAutoPlayNext(autoPlay: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_PLAY_NEXT_KEY] = autoPlay
        }
    }

    suspend fun updateSeekIncrementSeconds(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[SEEK_INCREMENT_SECONDS_KEY] = seconds
        }
    }

    suspend fun updatePlaybackSpeed(speed: Float) {
        dataStore.edit { preferences ->
            preferences[PLAYBACK_SPEED_KEY] = speed
        }
    }
}
