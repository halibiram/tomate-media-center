package com.halibiram.tomato.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halibiram.tomato.core.datastore.preferences.AppTheme // Assuming from core
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// Data class to hold all relevant settings for the UI
data class AllSettings(
    val appTheme: AppTheme = AppTheme.SYSTEM_DEFAULT,
    val notificationsEnabled: Boolean = true,
    val dataSaverMode: Boolean = false,
    val preferredSubtitleLanguage: String = "en",
    val playbackSpeed: Float = 1.0f,
    val autoPlayNext: Boolean = true,
    val email: String? = null // Example user-specific setting
)

data class SettingsUiState(
    val settings: AllSettings = AllSettings(),
    val isLoading: Boolean = true,
    val error: String? = null
)

// @HiltViewModel
class SettingsViewModel /*@Inject constructor(
    // private val appPreferences: com.halibiram.tomato.core.datastore.preferences.AppPreferences,
    // private val playerPreferences: com.halibiram.tomato.core.datastore.preferences.PlayerPreferences,
    // private val userPreferences: com.halibiram.tomato.core.datastore.preferences.UserPreferences,
    // private val authRepository: AuthRepository // For logout
)*/ : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadAllSettings()
    }

    private fun loadAllSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Combine flows from different preference sources
                // combine(
                //     appPreferences.applicationSettingsFlow,
                //     playerPreferences.playerSettingsFlow,
                //     userPreferences.userSessionFlow // Assuming this has email or other user info
                // ) { appSettings, playerSettings, userSession ->
                //     AllSettings(
                //         appTheme = appSettings.appTheme,
                //         notificationsEnabled = appSettings.notificationsEnabled,
                //         dataSaverMode = appSettings.dataSaverMode,
                //         preferredSubtitleLanguage = playerSettings.preferredSubtitleLanguage,
                //         playbackSpeed = playerSettings.playbackSpeed,
                //         autoPlayNext = playerSettings.autoPlayNext,
                //         email = userSession.email // or username, etc.
                //     )
                // }.collect { combinedSettings ->
                //     _uiState.value = SettingsUiState(settings = combinedSettings, isLoading = false)
                // }

                // Simulate loading
                kotlinx.coroutines.delay(300)
                _uiState.value = SettingsUiState(
                    settings = AllSettings(email = "user@example.com"), // Default/simulated settings
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load settings: ${e.message}")
            }
        }
    }

    fun updateAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            // appPreferences.updateAppTheme(theme)
            // Refresh UI, typically flow would auto-update, or reloadAllSettings() if not directly observing combined flow
             _uiState.value = _uiState.value.copy(settings = _uiState.value.settings.copy(appTheme = theme))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            // appPreferences.toggleNotifications(enabled)
            _uiState.value = _uiState.value.copy(settings = _uiState.value.settings.copy(notificationsEnabled = enabled))
        }
    }

    fun toggleDataSaver(enabled: Boolean) {
        viewModelScope.launch {
            // appPreferences.toggleDataSaverMode(enabled)
            _uiState.value = _uiState.value.copy(settings = _uiState.value.settings.copy(dataSaverMode = enabled))
        }
    }

    fun updateSubtitleLanguage(languageCode: String) {
        viewModelScope.launch {
            // playerPreferences.updatePreferredSubtitleLanguage(languageCode)
            _uiState.value = _uiState.value.copy(settings = _uiState.value.settings.copy(preferredSubtitleLanguage = languageCode))
        }
    }

    fun updatePlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            // playerPreferences.updatePlaybackSpeed(speed)
            _uiState.value = _uiState.value.copy(settings = _uiState.value.settings.copy(playbackSpeed = speed))
        }
    }

    fun toggleAutoPlayNext(enabled: Boolean) {
        viewModelScope.launch {
            // playerPreferences.toggleAutoPlayNext(enabled)
            _uiState.value = _uiState.value.copy(settings = _uiState.value.settings.copy(autoPlayNext = enabled))
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            // authRepository.logout()
            // userPreferences.clearUserSession()
            // Navigate to login screen or update UI accordingly
            // This might involve setting an event/flag for the UI to observe and navigate
        }
    }
}
