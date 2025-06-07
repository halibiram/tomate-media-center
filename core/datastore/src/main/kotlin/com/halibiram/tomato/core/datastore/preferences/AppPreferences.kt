package com.halibiram.tomato.core.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

// Re-defining AppTheme here if it's not accessible or to avoid module dependency issues.
// Ideally, this enum would be in a common/core module if shared across features and core.
// For now, defining it here for self-containment of AppPreferences.
enum class AppThemePreference {
    LIGHT, DARK, SYSTEM_DEFAULT
}

data class AppPreferencesData(
    val appTheme: AppThemePreference, // Changed from isDarkMode to a more versatile enum
    val selectedThemeColor: String?, // e.g., hex code or name of a color scheme variant, nullable
    val dataSaverMode: Boolean,
    val lastSyncTimestamp: Long? // Nullable if never synced
)

class AppPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {

    companion object {
        internal val APP_THEME_KEY = stringPreferencesKey("app_theme") // Storing enum name as string
        internal val SELECTED_THEME_COLOR_KEY = stringPreferencesKey("selected_theme_color")
        internal val DATA_SAVER_MODE_KEY = booleanPreferencesKey("data_saver_mode")
        internal val LAST_SYNC_TIMESTAMP_KEY = longPreferencesKey("last_sync_timestamp")

        // Default values
        val DEFAULT_APP_THEME = AppThemePreference.SYSTEM_DEFAULT
        val DEFAULT_SELECTED_THEME_COLOR: String? = null // No specific color selected by default
        const val DEFAULT_DATA_SAVER_MODE = false
        val DEFAULT_LAST_SYNC_TIMESTAMP: Long? = null
    }

    val appPreferencesFlow: Flow<AppPreferencesData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapAppPreferences(preferences)
        }

    private fun mapAppPreferences(preferences: Preferences): AppPreferencesData {
        val themeName = preferences[APP_THEME_KEY] ?: DEFAULT_APP_THEME.name
        val appTheme = try { AppThemePreference.valueOf(themeName) } catch (e: IllegalArgumentException) { DEFAULT_APP_THEME }
        val selectedColor = preferences[SELECTED_THEME_COLOR_KEY] ?: DEFAULT_SELECTED_THEME_COLOR
        val dataSaver = preferences[DATA_SAVER_MODE_KEY] ?: DEFAULT_DATA_SAVER_MODE
        val lastSync = preferences[LAST_SYNC_TIMESTAMP_KEY] ?: DEFAULT_LAST_SYNC_TIMESTAMP

        return AppPreferencesData(appTheme, selectedColor, dataSaver, lastSync)
    }

    suspend fun updateAppTheme(theme: AppThemePreference) {
        dataStore.edit { preferences ->
            preferences[APP_THEME_KEY] = theme.name
        }
    }

    suspend fun updateSelectedThemeColor(color: String?) {
        dataStore.edit { preferences ->
            if (color == null) preferences.remove(SELECTED_THEME_COLOR_KEY) else preferences[SELECTED_THEME_COLOR_KEY] = color
        }
    }

    suspend fun updateDataSaverMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DATA_SAVER_MODE_KEY] = enabled
        }
    }

    suspend fun updateLastSyncTimestamp(timestamp: Long?) {
        dataStore.edit { preferences ->
            if (timestamp == null) preferences.remove(LAST_SYNC_TIMESTAMP_KEY) else preferences[LAST_SYNC_TIMESTAMP_KEY] = timestamp
        }
    }
}
