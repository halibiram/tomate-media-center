package com.halibiram.tomato.core.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class AppTheme {
    LIGHT, DARK, SYSTEM_DEFAULT
}

data class ApplicationSettings(
    val appTheme: AppTheme,
    val notificationsEnabled: Boolean,
    val dataSaverMode: Boolean
)

class AppPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        const val APP_PREFERENCES_NAME = "app_settings_prefs"
        private val APP_THEME_KEY = stringPreferencesKey("app_theme")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val DATA_SAVER_MODE_KEY = booleanPreferencesKey("data_saver_mode")

        val DEFAULT_THEME = AppTheme.SYSTEM_DEFAULT
        const val DEFAULT_NOTIFICATIONS_ENABLED = true
        const val DEFAULT_DATA_SAVER_MODE = false
    }

    val applicationSettingsFlow: Flow<ApplicationSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val themeName = preferences[APP_THEME_KEY] ?: DEFAULT_THEME.name
            val theme = AppTheme.valueOf(themeName)
            val notifications = preferences[NOTIFICATIONS_ENABLED_KEY] ?: DEFAULT_NOTIFICATIONS_ENABLED
            val dataSaver = preferences[DATA_SAVER_MODE_KEY] ?: DEFAULT_DATA_SAVER_MODE
            ApplicationSettings(theme, notifications, dataSaver)
        }

    suspend fun updateAppTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[APP_THEME_KEY] = theme.name
        }
    }

    suspend fun toggleNotifications(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun toggleDataSaverMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DATA_SAVER_MODE_KEY] = enabled
        }
    }
}
