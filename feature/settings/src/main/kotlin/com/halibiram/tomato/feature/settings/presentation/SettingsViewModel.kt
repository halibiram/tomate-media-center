package com.halibiram.tomato.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.core.datastore.preferences.AppPreferences
import com.halibiram.tomato.core.datastore.preferences.AppPreferencesData
import com.halibiram.tomato.core.datastore.preferences.AppThemePreference // Assuming defined in AppPreferences.kt
import com.halibiram.tomato.core.datastore.preferences.PlayerPreferences
import com.halibiram.tomato.core.datastore.preferences.PlayerPreferencesData
import com.halibiram.tomato.core.datastore.preferences.UserPreferences
import com.halibiram.tomato.core.datastore.preferences.UserPreferencesData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    // Using default constructors of preference data classes
    val appPrefs: AppPreferencesData = AppPreferencesData( // Provide default values matching those in AppPreferences
        appTheme = AppPreferences.DEFAULT_APP_THEME,
        selectedThemeColor = AppPreferences.DEFAULT_SELECTED_THEME_COLOR,
        dataSaverMode = AppPreferences.DEFAULT_DATA_SAVER_MODE,
        lastSyncTimestamp = AppPreferences.DEFAULT_LAST_SYNC_TIMESTAMP
    ),
    val userPrefs: UserPreferencesData = UserPreferencesData( // Provide default values
        userId = null,
        isLoggedIn = UserPreferences.DEFAULT_IS_LOGGED_IN,
        authToken = null,
        username = null
    ),
    val playerPrefs: PlayerPreferencesData = PlayerPreferencesData( // Provide default values
        defaultSubtitleLanguage = PlayerPreferences.DEFAULT_SUBTITLE_LANGUAGE,
        preferredResolution = PlayerPreferences.DEFAULT_RESOLUTION,
        autoPlayNext = PlayerPreferences.DEFAULT_AUTO_PLAY_NEXT,
        seekIncrementSeconds = PlayerPreferences.DEFAULT_SEEK_INCREMENT_SECONDS,
        playbackSpeed = PlayerPreferences.DEFAULT_PLAYBACK_SPEED
    ),
    val isLoading: Boolean = true,
    val error: String? = null // For any error messages during loading or saving
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val userPreferences: UserPreferences,
    private val playerPreferences: PlayerPreferences
    // private val authRepository: AuthRepository // For logout if needed
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadAllSettings()
    }

    private fun loadAllSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // Start with loading true
            combine(
                appPreferences.appPreferencesFlow,
                userPreferences.userPreferencesFlow,
                playerPreferences.playerPreferencesFlow
            ) { app, user, player ->
                // Directly use the data classes from the flows
                SettingsUiState(appPrefs = app, userPrefs = user, playerPrefs = player, isLoading = false)
            }.catch { e ->
                // Handle error loading preferences
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load settings: ${e.message ?: "Unknown error"}"
                    )
                }
            }.collectLatest { newState -> // Use collectLatest if there are rapid updates
                _uiState.update { newState }
            }
        }
    }

    // --- App Preferences Methods ---
    fun updateAppTheme(theme: AppThemePreference) {
        viewModelScope.launch {
            appPreferences.updateAppTheme(theme)
            // UI will recompose due to appPreferencesFlow update
        }
    }

    fun updateSelectedThemeColor(themeColor: String?) { // Allow nullable for clearing
        viewModelScope.launch {
            appPreferences.updateSelectedThemeColor(themeColor)
        }
    }

    fun updateDataSaverMode(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.updateDataSaverMode(enabled)
        }
    }

    fun updateLastSyncTimestamp(timestamp: Long?) {
        viewModelScope.launch {
            appPreferences.updateLastSyncTimestamp(timestamp)
        }
    }

    // --- Player Preferences Methods ---
    fun updateDefaultSubtitleLanguage(language: String) {
        viewModelScope.launch {
            playerPreferences.updateDefaultSubtitleLanguage(language)
        }
    }

    fun updatePreferredResolution(resolution: String) {
        viewModelScope.launch {
            playerPreferences.updatePreferredResolution(resolution)
        }
    }

    fun updateAutoPlayNext(enabled: Boolean) {
        viewModelScope.launch {
            playerPreferences.updateAutoPlayNext(enabled)
        }
    }

    fun updateSeekIncrementSeconds(seconds: Int) {
        viewModelScope.launch {
            playerPreferences.updateSeekIncrementSeconds(seconds)
        }
    }

    fun updatePlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            playerPreferences.updatePlaybackSpeed(speed)
        }
    }

    // --- User Preferences Methods (Examples) ---
    fun updateUsername(username: String?) { // Example
        viewModelScope.launch {
            userPreferences.updateUsername(username)
        }
    }

    fun logoutUser() { // Example
        viewModelScope.launch {
            // authRepository.logout() // If you have an auth repository
            userPreferences.clearUserSession()
            // Could also emit a one-time event for navigation to login screen
        }
    }

    fun clearSettingsError() {
        _uiState.update { it.copy(error = null) }
    }
}
