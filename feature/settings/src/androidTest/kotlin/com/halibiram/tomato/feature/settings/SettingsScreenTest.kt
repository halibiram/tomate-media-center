package com.halibiram.tomato.feature.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.halibiram.tomato.core.datastore.preferences.*
import com.halibiram.tomato.feature.settings.presentation.SettingsScreen
import com.halibiram.tomato.feature.settings.presentation.SettingsUiState
import com.halibiram.tomato.feature.settings.presentation.SettingsViewModel
import com.halibiram.tomato.ui.theme.TomatoTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

// Fake ViewModel for SettingsScreen UI tests
class FakeSettingsViewModel(
    initialState: SettingsUiState,
    // Mocks for preference classes if direct interaction needs verification
    val mockAppPrefs: AppPreferences = mockk(relaxed = true),
    val mockUserPrefs: UserPreferences = mockk(relaxed = true),
    val mockPlayerPrefs: PlayerPreferences = mockk(relaxed = true)
) : SettingsViewModel(
    mockAppPrefs,
    mockUserPrefs,
    mockPlayerPrefs
) {
    private val _fakeUiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<SettingsUiState> = _fakeUiState

    // Track calls for verification
    var updatedTheme: AppThemePreference? = null
    var updatedDataSaver: Boolean? = null
    var updatedSubLang: String? = null
    var updatedAutoPlayNext: Boolean? = null


    fun setState(newState: SettingsUiState) {
        _fakeUiState.value = newState
    }

    override fun updateAppTheme(theme: AppThemePreference) {
        // super.updateAppTheme(theme) // Calls mock, which is fine
        updatedTheme = theme
        // Simulate state update as if flow from mockAppPrefs updated
        _fakeUiState.value = _fakeUiState.value.copy(appPrefs = _fakeUiState.value.appPrefs.copy(appTheme = theme))
    }

    override fun updateDataSaverMode(enabled: Boolean) {
        updatedDataSaver = enabled
        _fakeUiState.value = _fakeUiState.value.copy(appPrefs = _fakeUiState.value.appPrefs.copy(dataSaverMode = enabled))
    }

    override fun updateDefaultSubtitleLanguage(language: String) {
        updatedSubLang = language
        _fakeUiState.value = _fakeUiState.value.copy(playerPrefs = _fakeUiState.value.playerPrefs.copy(defaultSubtitleLanguage = language))
    }

    override fun updateAutoPlayNext(enabled: Boolean) {
        updatedAutoPlayNext = enabled
        _fakeUiState.value = _fakeUiState.value.copy(playerPrefs = _fakeUiState.value.playerPrefs.copy(autoPlayNext = enabled))
    }
}

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeSettingsViewModel

    private fun setupViewModel(initialState: SettingsUiState) {
        fakeViewModel = FakeSettingsViewModel(initialState)
    }

    @Test
    fun settingsScreen_loadingState_showsLoadingIndicator() {
        setupViewModel(SettingsUiState(isLoading = true))
        composeTestRule.setContent {
            TomatoTheme {
                SettingsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToAbout = {}, onNavigateToAccount = {})
            }
        }
        // Check for CircularProgressIndicator (needs testTag or check other elements absent)
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed() // TopBar title
        // Check that no actual settings items are shown
        composeTestRule.onNodeWithText("Theme", substring = true).assertDoesNotExist()
    }

    @Test
    fun settingsScreen_errorState_showsErrorMessage() {
        val errorMsg = "Failed to load settings"
        setupViewModel(SettingsUiState(isLoading = false, error = errorMsg))
        composeTestRule.setContent {
            TomatoTheme {
                SettingsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToAbout = {}, onNavigateToAccount = {})
            }
        }
        composeTestRule.onNodeWithText("Error: $errorMsg").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dismiss").assertIsDisplayed() // Retry/Dismiss button
    }

    @Test
    fun settingsScreen_displaysPreferenceValuesCorrectly() {
        val initialState = SettingsUiState(
            isLoading = false,
            appPrefs = AppPreferencesData(appTheme = AppThemePreference.DARK, dataSaverMode = true, selectedThemeColor = null, lastSyncTimestamp = null),
            playerPrefs = PlayerPreferencesData(defaultSubtitleLanguage = "es", preferredResolution = "1080p", autoPlayNext = false, seekIncrementSeconds = 10, playbackSpeed = 1.0f),
            userPrefs = UserPreferencesData(username = "TestUser", isLoggedIn = true, userId = "id", authToken = "token")
        )
        setupViewModel(initialState)

        composeTestRule.setContent {
            TomatoTheme {
                SettingsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToAbout = {}, onNavigateToAccount = {})
            }
        }

        // Appearance
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark", substring = true).assertIsDisplayed() // Summary for theme

        // Player
        composeTestRule.onNodeWithText("Auto-play next episode").assertIsDisplayed()
        // For SwitchSettingItem, the Switch itself can be found by its checked state if unique, or by a testTag.
        // To verify checked state, find the Switch associated with "Auto-play next episode".
        composeTestRule.onNode(hasParent(hasText("Auto-play next episode")) and hasSet쳥Properties(mapOf(SemanticsProperties.ToggleableState to androidx.compose.ui.state.ToggleableState.Off)))
            .assertIsDisplayed() // Switch is Off (autoplayNext = false)

        composeTestRule.onNodeWithText("Default Subtitle Language").assertIsDisplayed()
        composeTestRule.onNodeWithText(Locale("es").displayName, substring = true).assertIsDisplayed() // Summary for language

        // Data & Sync
        composeTestRule.onNodeWithText("Data Saver Mode").assertIsDisplayed()
        composeTestRule.onNode(hasParent(hasText("Data Saver Mode")) and hasSet쳥Properties(mapOf(SemanticsProperties.ToggleableState to androidx.compose.ui.state.ToggleableState.On)))
            .assertIsDisplayed() // Switch is On (dataSaverMode = true)

        // Account
        composeTestRule.onNodeWithText("TestUser", substring = true).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_switchInteraction_callsViewModelUpdate() {
        val initialState = SettingsUiState(isLoading = false, appPrefs = AppPreferencesData(dataSaverMode = false, appTheme = AppPreferences.DEFAULT_APP_THEME, selectedThemeColor = null, lastSyncTimestamp = null))
        setupViewModel(initialState)

        composeTestRule.setContent {
            TomatoTheme {
                SettingsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToAbout = {}, onNavigateToAccount = {})
            }
        }

        // Find the Data Saver Mode switch (it's currently Off) and click it
        composeTestRule.onNode(hasParent(hasText("Data Saver Mode")) and isToggleable())
            .performClick()

        composeTestRule.waitForIdle() // Ensure recomposition and state update

        // Verify ViewModel method was called and state updated in Fake VM
        assertEquals(true, fakeViewModel.updatedDataSaver)
        // Verify UI reflects the change (Switch is now On)
         composeTestRule.onNode(hasParent(hasText("Data Saver Mode")) and hasSet쳥Properties(mapOf(SemanticsProperties.ToggleableState to androidx.compose.ui.state.ToggleableState.On)))
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_dialogSettingClick_opensDialog() {
        // This test focuses on opening the dialog. Dialog content tests would be separate.
        val initialState = SettingsUiState(isLoading = false)
        setupViewModel(initialState)
        var themeDialogShownInFake = false
        fakeViewModel.mockAppPrefs = mockk(relaxed = true) {
            // Simulate what happens when updateAppTheme is called by dialog
            coJustRun { updateAppTheme(any()) }
        }
        // Override openThemeDialog in fake if it involves more logic, or check a flag.
        // For this, we'll check if the dialog title appears.

        composeTestRule.setContent {
            TomatoTheme {
                SettingsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToAbout = {}, onNavigateToAccount = {})
            }
        }

        composeTestRule.onNodeWithText("Theme").performClick() // Click the "Theme" DialogSettingItem
        composeTestRule.waitForIdle()

        // Verify the ThemeSelectionDialog is shown by checking its title
        composeTestRule.onNodeWithText("Select Theme").assertIsDisplayed()
        // Example: Click an option in the dialog
        composeTestRule.onNodeWithText(AppThemePreference.DARK.name.replace('_', ' ').capitalize()).performClick()
        composeTestRule.waitForIdle()
        // Verify dialog is dismissed and ViewModel method was called
        composeTestRule.onNodeWithText("Select Theme").assertDoesNotExist()
        assertEquals(AppThemePreference.DARK, fakeViewModel.updatedTheme)
    }
}
