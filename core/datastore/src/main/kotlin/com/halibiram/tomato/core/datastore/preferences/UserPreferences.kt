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

data class UserSession(
    val authToken: String?,
    val userId: String?,
    val username: String?,
    val isLoggedIn: Boolean
)

class UserPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        const val USER_PREFERENCES_NAME = "user_prefs"
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }

    val userSessionFlow: Flow<UserSession> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val authToken = preferences[AUTH_TOKEN_KEY]
            val userId = preferences[USER_ID_KEY]
            val username = preferences[USERNAME_KEY]
            val isLoggedIn = preferences[IS_LOGGED_IN_KEY] ?: false
            UserSession(authToken, userId, username, isLoggedIn)
        }

    suspend fun saveUserSession(authToken: String, userId: String, username: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = authToken
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }

    suspend fun clearUserSession() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USERNAME_KEY)
            preferences[IS_LOGGED_IN_KEY] = false
        }
    }

    suspend fun updateAuthToken(newToken: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = newToken
        }
    }

    suspend fun isLoggedIn(): Flow<Boolean> = dataStore.data.map { it[IS_LOGGED_IN_KEY] ?: false }
}
