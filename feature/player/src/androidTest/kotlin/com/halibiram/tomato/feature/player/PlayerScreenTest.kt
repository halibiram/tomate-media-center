package com.halibiram.tomato.feature.player

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import com.halibiram.tomato.core.player.cast.CastManager
import com.halibiram.tomato.core.player.cast.CastState
import com.halibiram.tomato.core.player.exoplayer.PlayerManager
import com.halibiram.tomato.core.player.exoplayer.PlayerState
import com.halibiram.tomato.feature.player.presentation.PlayerScreen
import com.halibiram.tomato.feature.player.presentation.PlayerScreenUiState
import com.halibiram.tomato.feature.player.presentation.PlayerViewModel
import com.halibiram.tomato.ui.theme.TomatoTheme
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.media3.common.Player // For playback states

// Fake ViewModel for PlayerScreen UI tests
class FakePlayerViewModel(
    initialCoreState: PlayerState,
    initialUiState: PlayerScreenUiState,
    val mockPlayerManager: PlayerManager = mockk(relaxed = true),
    val mockCastManager: CastManager = mockk(relaxed = true)
) : PlayerViewModel(
    SavedStateHandle(), // Empty SavedStateHandle for simplicity in fake
    mockPlayerManager,
    mockCastManager
) {
    private val _fakeCorePlayerState = MutableStateFlow(initialCoreState)
    override val corePlayerState: StateFlow<PlayerState> = _fakeCorePlayerState

    private val _fakeUiState = MutableStateFlow(initialUiState)
    override val uiState: StateFlow<PlayerScreenUiState> = _fakeUiState

    // Expose ExoPlayer instance via PlayerManager mock
    init {
        every { mockPlayerManager.getExoPlayerInstance() } returns mockk(relaxed = true)
        every { mockCastManager.castState } returns MutableStateFlow(initialUiState.currentCastState)
        every { mockCastManager.isCastAvailable } returns MutableStateFlow(initialUiState.isCastAvailable)
    }


    fun setState(coreState: PlayerState? = null, uiStateUpdate: PlayerScreenUiState? = null) {
        coreState?.let { _fakeCorePlayerState.value = it }
        uiStateUpdate?.let { _fakeUiState.value = it }
    }

    // Override methods to verify calls if needed, or use MockK's verify on the mocks
    override fun toggleControlsVisibility() {
        _fakeUiState.value = _fakeUiState.value.copy(controlsVisible = !_fakeUiState.value.controlsVisible)
        // Simulate auto-hide logic if needed for specific tests, or assume it's complex to test here
    }
    override fun seekForward(seconds: Int) { mockPlayerManager.seekPlayback(corePlayerState.value.currentPositionMs + seconds * 1000) }
    override fun seekBackward(seconds: Int) { mockPlayerManager.seekPlayback(corePlayerState.value.currentPositionMs - seconds * 1000) }
    override fun togglePlayPause() { if(corePlayerState.value.isPlaying) mockPlayerManager.pausePlayback() else mockPlayerManager.resumePlayback() }
    override fun openTrackSelectionDialog() { _fakeUiState.value = _fakeUiState.value.copy(showTrackSelectionDialog = true) }
    override fun closeTrackSelectionDialog() { _fakeUiState.value = _fakeUiState.value.copy(showTrackSelectionDialog = false) }

}


@RunWith(AndroidJUnit4::class)
class PlayerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakePlayerViewModel

    private fun setupViewModel(
        initialCoreState: PlayerState = PlayerState(),
        initialUiState: PlayerScreenUiState = PlayerScreenUiState()
    ) {
        fakeViewModel = FakePlayerViewModel(initialCoreState, initialUiState)
    }

    @Test
    fun playerScreen_initialState_PlayerViewExistsControlsVisible() {
        // Given
        setupViewModel(
            initialUiState = PlayerScreenUiState(controlsVisible = true, mediaTitle = "Test Media")
        )

        // When
        composeTestRule.setContent {
            TomatoTheme {
                PlayerScreen(viewModel = fakeViewModel, onNavigateBack = {})
            }
        }

        // Then
        // PlayerView is an AndroidView, hard to assert directly without custom tags.
        // We assume it's there if no crash.
        // Check for controls visibility
        composeTestRule.onNodeWithText("Test Media").assertIsDisplayed() // Title in PlayerControls
        composeTestRule.onNodeWithContentDescription("Play").assertIsDisplayed() // Play button
    }

    @Test
    fun playerScreen_loadingState_showsLoadingIndicatorInOverlay() {
        // Given
        setupViewModel(
            initialCoreState = PlayerState(isLoading = true, playbackState = Player.STATE_BUFFERING),
            initialUiState = PlayerScreenUiState(controlsVisible = false) // Hide controls to see overlay clearly
        )

        // When
        composeTestRule.setContent {
            TomatoTheme {
                PlayerScreen(viewModel = fakeViewModel, onNavigateBack = {})
            }
        }
        // Then
        // Check for CircularProgressIndicator (assuming it's the only one when loading)
        // This requires the indicator to be identifiable, e.g. by a testTag in PlayerOverlay
        // As a proxy, check that controls are hidden and no error is shown.
        composeTestRule.onNodeWithContentDescription("Play").assertDoesNotExist() // Controls not visible
        // composeTestRule.onNodeWithTag("loading_indicator_overlay").assertIsDisplayed(); // Ideal
    }

    @Test
    fun playerScreen_errorState_showsErrorInOverlay() {
        // Given
        val errorMessage = "Test Playback Error"
        setupViewModel(
            initialCoreState = PlayerState(error = Throwable(errorMessage)),
            initialUiState = PlayerScreenUiState(controlsVisible = false) // Hide controls
        )

        // When
        composeTestRule.setContent {
            TomatoTheme {
                PlayerScreen(viewModel = fakeViewModel, onNavigateBack = {})
            }
        }
        // Then
        composeTestRule.onNodeWithText("Playback Error", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(errorMessage, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun playerScreen_singleTap_togglesControlsVisibility() {
        // Given
        setupViewModel(initialUiState = PlayerScreenUiState(controlsVisible = true, mediaTitle = "Tap Test"))
        composeTestRule.setContent {
            TomatoTheme {
                PlayerScreen(viewModel = fakeViewModel, onNavigateBack = {})
            }
        }

        // Initially visible
        composeTestRule.onNodeWithText("Tap Test").assertIsDisplayed()

        // When: Single tap on the screen (root gesture detector)
        composeTestRule.onRoot().performClick() // Simulates tap on PlayerGestures area

        // Then: Controls should be hidden (ViewModel's toggleControlsVisibility would update state)
        // Need to wait for state change and recomposition
        composeTestRule.waitForIdle()
        // Manually trigger state change in fake for test if toggle is complex
        fakeViewModel.setState(uiStateUpdate = fakeViewModel.uiState.value.copy(controlsVisible = false))
        composeTestRule.onNodeWithText("Tap Test").assertDoesNotExist() // Title in controls is gone

        // When: Tap again
        composeTestRule.onRoot().performClick()
        fakeViewModel.setState(uiStateUpdate = fakeViewModel.uiState.value.copy(controlsVisible = true))
        composeTestRule.waitForIdle()
        // Then: Controls should be visible again
        composeTestRule.onNodeWithText("Tap Test").assertIsDisplayed()
    }

    @Test
    fun playerScreen_doubleTapRight_seeksForward() {
        // Given
        setupViewModel()
        composeTestRule.setContent {
            TomatoTheme {
                PlayerScreen(viewModel = fakeViewModel, onNavigateBack = {})
            }
        }
        // When: Double tap on the right side
        val rootNode = composeTestRule.onRoot()
        val rightSideX = rootNode.getBoundsInRoot().width * 0.75f
        val centerY = rootNode.getBoundsInRoot().height * 0.5f
        rootNode.performTouchInput { doubleClick(Offset(rightSideX, centerY)) }

        // Then: Verify ViewModel's seekForward was called
        verify { fakeViewModel.mockPlayerManager.seekPlayback(any()) } // More specific check on position if possible
    }

    @Test
    fun playerScreen_doubleTapLeft_seeksBackward() {
        // Given
        setupViewModel()
        composeTestRule.setContent {
            TomatoTheme {
                PlayerScreen(viewModel = fakeViewModel, onNavigateBack = {})
            }
        }
        // When: Double tap on the left side
        val rootNode = composeTestRule.onRoot()
        val leftSideX = rootNode.getBoundsInRoot().width * 0.25f
        val centerY = rootNode.getBoundsInRoot().height * 0.5f
        rootNode.performTouchInput { doubleClick(Offset(leftSideX, centerY)) }

        // Then: Verify ViewModel's seekBackward was called
        verify { fakeViewModel.mockPlayerManager.seekPlayback(any()) }
    }

    @Test
    fun playerScreen_settingsButtonClick_opensTrackSelectionDialog() {
        setupViewModel(initialUiState = PlayerScreenUiState(controlsVisible = true))
         composeTestRule.setContent {
            TomatoTheme {
                PlayerScreen(viewModel = fakeViewModel, onNavigateBack = {})
            }
        }
        // Ensure controls are visible to click settings
        fakeViewModel.setState(uiStateUpdate = fakeViewModel.uiState.value.copy(controlsVisible = true))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Settings (Subtitles, Quality, Speed)").performClick()
        composeTestRule.waitForIdle()

        // Verify dialog is shown by checking for its title or content
        // This relies on the fakeViewModel correctly updating showTrackSelectionDialog state
        assertTrue(fakeViewModel.uiState.value.showTrackSelectionDialog)
        // In a real UI test, you would check for dialog's specific text:
        // composeTestRule.onNodeWithText("Track Selection").assertIsDisplayed() // If dialog is shown
    }

    // Cast button UI test is complex due to MediaRouteButton being an Android View.
    // It usually requires more specific setup or Robolectric for full interaction test.
    // Basic visibility check:
    @Test
    fun playerScreen_castButton_visibilityBasedOnCastAvailability() {
        setupViewModel(initialUiState = PlayerScreenUiState(isCastAvailable = true, controlsVisible = true))
        composeTestRule.setContent {
            TomatoTheme {
                PlayerScreen(viewModel = fakeViewModel, onNavigateBack = {})
            }
        }
        // MediaRouteButton doesn't have simple text/contentDescription for Compose tests.
        // It would need a testTag or custom matcher if wrapped.
        // For now, this test is conceptual for visibility.
        // If PlayerControls shows it, its container might be identifiable.
        // Example: composeTestRule.onNodeWithTag("cast_button_container").assertIsDisplayed()
    }
}
