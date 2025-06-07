package com.halibiram.tomato.feature.settings.presentation

import com.halibiram.tomato.core.datastore.preferences.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

// Assume MainCoroutineExtension is in a shared test utility module
@ExperimentalCoroutinesApi
class MainCoroutineExtension(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : org.junit.jupiter.api.extension.BeforeEachCallback, org.junit.jupiter.api.extension.AfterEachCallback {
    override fun beforeEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun afterEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
@ExtendWith(MainCoroutineExtension::class)
class SettingsViewModelTest {

    private lateinit var appPreferences: AppPreferences
    private lateinit var userPreferences: UserPreferences
    private lateinit var playerPreferences: PlayerPreferences
    private lateinit var viewModel: SettingsViewModel
    private lateinit var testDispatcher: TestDispatcher

    // Flows to control mock emissions
    private val appPrefsFlow = MutableStateFlow(AppPreferencesData(appTheme = AppPreferences.DEFAULT_APP_THEME, selectedThemeColor = null, dataSaverMode = false, lastSyncTimestamp = null))
    private val userPrefsFlow = MutableStateFlow(UserPreferencesData(userId=null, isLoggedIn = false, authToken = null, username = null))
    private val playerPrefsFlow = MutableStateFlow(PlayerPreferencesData(defaultSubtitleLanguage = "en", preferredResolution = "Auto", autoPlayNext = true, seekIncrementSeconds = 10, playbackSpeed = 1.0f))

    @BeforeEach
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        appPreferences = mockk()
        userPreferences = mockk()
        playerPreferences = mockk()

        every { appPreferences.appPreferencesFlow } returns appPrefsFlow
        every { userPreferences.userPreferencesFlow } returns userPrefsFlow
        every { playerPreferences.playerPreferencesFlow } returns playerPrefsFlow

        // Mock update functions
        coJustRun { appPreferences.updateAppTheme(any()) }
        coJustRun { appPreferences.updateSelectedThemeColor(any()) }
        coJustRun { appPreferences.updateDataSaverMode(any()) }
        coJustRun { appPreferences.updateLastSyncTimestamp(any()) }
        coJustRun { playerPreferences.updateDefaultSubtitleLanguage(any()) }
        coJustRun { playerPreferences.updatePreferredResolution(any()) }
        coJustRun { playerPreferences.updateAutoPlayNext(any()) }
        coJustRun { playerPreferences.updateSeekIncrementSeconds(any()) }
        coJustRun { playerPreferences.updatePlaybackSpeed(any()) }
        coJustRun { userPreferences.updateUsername(any()) }
        coJustRun { userPreferences.clearUserSession() }


        viewModel = SettingsViewModel(appPreferences, userPreferences, playerPreferences)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading then updates with combined preferences`() = runTest(testDispatcher.scheduler) {
        // Initial state check (isLoading is true before flows emit fully)
        var uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading, "Should be loading initially after VM creation")

        // Advance time to allow combine and initial collection to process
        advanceUntilIdle()

        uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading, "Should not be loading after preferences are collected")
        assertEquals(appPrefsFlow.value, uiState.appPrefs)
        assertEquals(userPrefsFlow.value, uiState.userPrefs)
        assertEquals(playerPrefsFlow.value, uiState.playerPrefs)
        assertNull(uiState.error)
    }

    @Test
    fun `preference flows emitting new data updates uiState`() = runTest(testDispatcher.scheduler) {
        advanceUntilIdle() // Initial load

        val newAppPrefs = AppPreferencesData(appTheme = AppThemePreference.DARK, dataSaverMode = true, selectedThemeColor = "Blue", lastSyncTimestamp = 123L)
        appPrefsFlow.value = newAppPrefs
        advanceUntilIdle()
        assertEquals(newAppPrefs, viewModel.uiState.value.appPrefs)

        val newUserPrefs = UserPreferencesData(userId = "testUser", isLoggedIn = true, authToken = "token", username = "TomatoUser")
        userPrefsFlow.value = newUserPrefs
        advanceUntilIdle()
        assertEquals(newUserPrefs, viewModel.uiState.value.userPrefs)

        val newPlayerPrefs = PlayerPreferencesData(defaultSubtitleLanguage = "es", preferredResolution = "1080p", autoPlayNext = false, seekIncrementSeconds = 15, playbackSpeed = 1.5f)
        playerPrefsFlow.value = newPlayerPrefs
        advanceUntilIdle()
        assertEquals(newPlayerPrefs, viewModel.uiState.value.playerPrefs)
    }


    @Test
    fun `updateAppTheme calls appPreferences updateAppTheme`() = runTest(testDispatcher.scheduler) {
        val newTheme = AppThemePreference.DARK
        viewModel.updateAppTheme(newTheme)
        advanceUntilIdle()
        coVerify { appPreferences.updateAppTheme(newTheme) }
    }

    @Test
    fun `updateDataSaverMode calls appPreferences updateDataSaverMode`() = runTest(testDispatcher.scheduler) {
        viewModel.updateDataSaverMode(true)
        advanceUntilIdle()
        coVerify { appPreferences.updateDataSaverMode(true) }
    }

    @Test
    fun `updateDefaultSubtitleLanguage calls playerPreferences updateDefaultSubtitleLanguage`() = runTest(testDispatcher.scheduler) {
        val lang = "es"
        viewModel.updateDefaultSubtitleLanguage(lang)
        advanceUntilIdle()
        coVerify { playerPreferences.updateDefaultSubtitleLanguage(lang) }
    }

    @Test
    fun `updateAutoPlayNext calls playerPreferences updateAutoPlayNext`() = runTest(testDispatcher.scheduler) {
        viewModel.updateAutoPlayNext(false)
        advanceUntilIdle()
        coVerify { playerPreferences.updateAutoPlayNext(false) }
    }

    @Test
    fun `logoutUser calls userPreferences clearUserSession`() = runTest(testDispatcher.scheduler) {
        viewModel.logoutUser()
        advanceUntilIdle()
        coVerify { userPreferences.clearUserSession() }
    }


    @Test
    fun `error in one preference flow updates uiState error`() = runTest(testDispatcher.scheduler) {
        val errorMessage = "AppPrefs Error"
        // Make one of the flows emit an error
        every { appPreferences.appPreferencesFlow } returns flow { throw RuntimeException(errorMessage) }

        // Re-initialize ViewModel to trigger collection of the erroring flow
        viewModel = SettingsViewModel(appPreferences, userPreferences, playerPreferences)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNotNull(uiState.error)
        assertTrue(uiState.error!!.contains(errorMessage))
    }

    @Test
    fun `clearSettingsError sets error to null`() = runTest(testDispatcher.scheduler) {
        // Manually set an error state for testing
        _uiState.update { it.copy(error = "Initial Error", isLoading = false) }
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearSettingsError()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }
}
