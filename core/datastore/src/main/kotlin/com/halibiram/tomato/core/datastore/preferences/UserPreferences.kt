package com.halibiram.tomato.core.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

data class UserPreferencesData(
    val userId: String?,
    val isLoggedIn: Boolean,
    val authToken: String?,
    val username: String?
    // Add other user-specific preferences like email if needed
)

class UserPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {

    // Define Preference Keys
    companion object {
        // Made internal to encapsulate them within this class if not needed publicly by key name
        internal val USER_ID_KEY = stringPreferencesKey("user_id")
        internal val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        internal val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        internal val USERNAME_KEY = stringPreferencesKey("username")

        // Default values
        const val DEFAULT_IS_LOGGED_IN = false
    }

    // Read all user preferences as a Flow
    val userPreferencesFlow: Flow<UserPreferencesData> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences()) // Emit empty preferences on error
            } else {
                throw exception
            }
        }.map { preferences ->
            mapUserPreferences(preferences)
        }

    private fun mapUserPreferences(preferences: Preferences): UserPreferencesData {
        val userId = preferences[USER_ID_KEY]
        val isLoggedIn = preferences[IS_LOGGED_IN_KEY] ?: DEFAULT_IS_LOGGED_IN
        val authToken = preferences[AUTH_TOKEN_KEY]
        val username = preferences[USERNAME_KEY]
        return UserPreferencesData(userId, isLoggedIn, authToken, username)
    }

    // Update functions for individual preferences
    suspend fun updateUserId(userId: String?) {
        dataStore.edit { preferences ->
            if (userId == null) preferences.remove(USER_ID_KEY) else preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun updateIsLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn
        }
    }

    suspend fun updateAuthToken(authToken: String?) {
        dataStore.edit { preferences ->
            if (authToken == null) preferences.remove(AUTH_TOKEN_KEY) else preferences[AUTH_TOKEN_KEY] = authToken
        }
    }

    suspend fun updateUsername(username: String?) {
        dataStore.edit { preferences ->
            if (username == null) preferences.remove(USERNAME_KEY) else preferences[USERNAME_KEY] = username
        }
    }

    // Convenience function to update all login related info
    suspend fun updateUserLoginInfo(userId: String, authToken: String, username: String?) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[AUTH_TOKEN_KEY] = authToken
            if (username != null) preferences[USERNAME_KEY] = username else preferences.remove(USERNAME_KEY)
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }

    suspend fun clearUserSession() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(USERNAME_KEY)
            preferences[IS_LOGGED_IN_KEY] = false
        }
    }
}
