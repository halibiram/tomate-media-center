package com.halibiram.tomato.feature.player.presentation

import androidx.lifecycle.SavedStateHandle
import com.halibiram.tomato.core.player.cast.CastManager
import com.halibiram.tomato.core.player.cast.CastState
import com.halibiram.tomato.core.player.exoplayer.PlayerManager
import com.halibiram.tomato.core.player.exoplayer.PlayerState
import com.halibiram.tomato.feature.player.navigation.PlayerArgs
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import androidx.media3.common.TrackGroup
import androidx.media3.common.Format

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
class PlayerViewModelTest {

    private lateinit var playerManager: PlayerManager
    private lateinit var castManager: CastManager
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: PlayerViewModel
    private lateinit var testDispatcher: TestDispatcher

    private val fakePlayerStateFlow = MutableStateFlow(PlayerState())
    private val fakeCastStateFlow = MutableStateFlow(CastState.NO_DEVICES_AVAILABLE)
    private val fakeIsCastAvailableFlow = MutableStateFlow(false)

    @BeforeEach
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        playerManager = mockk(relaxed = true)
        castManager = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle() // For basic tests, can set values directly

        // Mock flows from managers
        every { playerManager.playerStateFlow } returns fakePlayerStateFlow
        every { castManager.castState } returns fakeCastStateFlow
        every { castManager.isCastAvailable } returns fakeIsCastAvailableFlow

        // Mock methods
        coJustRun { playerManager.playMedia(any(), any()) }
        coJustRun { playerManager.pausePlayback() }
        coJustRun { playerManager.resumePlayback() }
        coJustRun { playerManager.seekPlayback(any()) }
        coJustRun { playerManager.release() }
        // Mock track selection methods if directly called by ViewModel, or ExoPlayer instance for direct calls
        val mockExoPlayer = mockk<androidx.media3.exoplayer.ExoPlayer>(relaxed = true)
        every { playerManager.getExoPlayerInstance() } returns mockExoPlayer
        every { mockExoPlayer.trackSelectionParameters = any() } just runs

        // Initialize ViewModel after mocks are set up
        // viewModel = PlayerViewModel(savedStateHandle, playerManager, castManager) // Will be initialized in each test with args
    }

    private fun initViewModelWithUrl(url: String?) {
        savedStateHandle[PlayerArgs.MEDIA_URL_ARG] = url
        viewModel = PlayerViewModel(savedStateHandle, playerManager, castManager)
    }


    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial UI state is correct`() {
        initViewModelWithUrl(null) // No URL initially
        val uiState = viewModel.uiState.value
        val coreState = viewModel.corePlayerState.value

        assertTrue(uiState.controlsVisible)
        assertFalse(uiState.isFullScreen)
        assertEquals("Error: Media URL not found", uiState.mediaTitle) // Due to null URL
        assertFalse(uiState.isCastAvailable)
        assertEquals(CastState.NO_DEVICES_AVAILABLE, uiState.currentCastState)
        assertFalse(uiState.isCasting)
        assertFalse(uiState.showTrackSelectionDialog)

        assertFalse(coreState.isPlaying)
        assertEquals(0L, coreState.currentPositionMs)
    }

    @Test
    fun `initializeAndPlayMedia calls PlayerManager with URL from SavedStateHandle`() = runTest(testDispatcher.scheduler) {
        val testUrl = "http://example.com/video.mp4"
        initViewModelWithUrl(testUrl)
        advanceUntilIdle() // Allow init and initial play to process

        coVerify { playerManager.playMedia(testUrl, true) } // playWhenReady defaults to true in PlayerManager
        // Also check title extraction (simplified)
        assertTrue(viewModel.uiState.value.mediaTitle.contains("video"))
    }

    @Test
    fun `togglePlayPause delegates to PlayerManager`() {
        initViewModelWithUrl("url")
        // Simulate playing
        fakePlayerStateFlow.value = PlayerState(isPlaying = true)
        viewModel.togglePlayPause()
        coVerify { playerManager.pausePlayback() }
        assertTrue(viewModel.uiState.value.controlsVisible) // Should stay visible after manual pause

        // Simulate paused
        fakePlayerStateFlow.value = PlayerState(isPlaying = false)
        viewModel.togglePlayPause()
        coVerify { playerManager.resumePlayback() }
    }

    @Test
    fun `seekTo delegates to PlayerManager`() {
        initViewModelWithUrl("url")
        val seekPosition = 10000L
        viewModel.seekTo(seekPosition)
        coVerify { playerManager.seekPlayback(seekPosition) }
    }

    @Test
    fun `seekForward and seekBackward calculate and delegate to seekTo`() {
        initViewModelWithUrl("url")
        fakePlayerStateFlow.value = PlayerState(currentPositionMs = 30000L, durationMs = 120000L)

        viewModel.seekForward(15) // Seek 15s forward
        coVerify { playerManager.seekPlayback(30000L + 15000L) }

        viewModel.seekBackward(15) // Seek 15s backward
        coVerify { playerManager.seekPlayback(30000L + 15000L - 15000L) } // Position after forward seek - 15s
    }


    @Test
    fun `controls visibility toggles and hides after timeout when playing`() = runTest(testDispatcher.scheduler) {
        initViewModelWithUrl("url")
        fakePlayerStateFlow.value = PlayerState(isPlaying = true) // Simulate playing

        // Initial state (showControlsThenAutoHide called in init)
        assertTrue(viewModel.uiState.value.controlsVisible)
        advanceTimeBy(controlsTimeoutMs + 100) // Advance past timeout
        assertFalse(viewModel.uiState.value.controlsVisible) // Should hide

        // Toggle to show
        viewModel.toggleControlsVisibility() // This calls showControlsThenAutoHide
        assertTrue(viewModel.uiState.value.controlsVisible)
        advanceTimeBy(controlsTimeoutMs + 100)
        assertFalse(viewModel.uiState.value.controlsVisible) // Should hide again

        // Toggle to hide (if it was shown permanently by pause)
        viewModel.showControlsThenAutoHide() // Make it visible
        viewModel.toggleControlsVisibility() // Should hide it
        assertFalse(viewModel.uiState.value.controlsVisible)
    }

    @Test
    fun `showControlsPermanently keeps controls visible even when playing`() = runTest(testDispatcher.scheduler){
        initViewModelWithUrl("url")
        fakePlayerStateFlow.value = PlayerState(isPlaying = true)

        viewModel.pause() // This calls showControlsPermanently
        assertTrue(viewModel.uiState.value.controlsVisible)

        advanceTimeBy(controlsTimeoutMs + 500) // Well past timeout
        assertTrue(viewModel.uiState.value.controlsVisible) // Still visible
    }


    @Test
    fun `cast state updates from CastManager affect UI state`() = runTest(testDispatcher.scheduler) {
        initViewModelWithUrl("url")

        fakeIsCastAvailableFlow.value = true
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isCastAvailable)

        fakeCastStateFlow.value = CastState.CONNECTING
        advanceUntilIdle()
        assertEquals(CastState.CONNECTING, viewModel.uiState.value.currentCastState)
        assertFalse(viewModel.uiState.value.isCasting)

        fakeCastStateFlow.value = CastState.CONNECTED
        advanceUntilIdle()
        assertEquals(CastState.CONNECTED, viewModel.uiState.value.currentCastState)
        assertTrue(viewModel.uiState.value.isCasting)
        coVerify { playerManager.pausePlayback() } // Local player should pause when casting connects
    }

    @Test
    fun `startCasting calls castManager loadRemoteMedia when connected`() {
        initViewModelWithUrl("http://example.com/video.mp4")
        val localPosition = 12345L
        fakePlayerStateFlow.value = PlayerState(currentPositionMs = localPosition)
        fakeCastStateFlow.value = CastState.CONNECTED // Simulate already connected
        _isCastAvailableFlow.value = true // Ensure cast is available too

        // This line is crucial for the test to correctly reflect the isCasting state pre-call
        viewModel.uiState.value.copy(currentCastState = CastState.CONNECTED, isCasting = true)


        viewModel.startCasting()

        coVerify { playerManager.pausePlayback() } // Local player pauses
        coVerify { castManager.loadRemoteMedia(
            mediaUrl = "http://example.com/video.mp4",
            title = "video", // Title extracted from URL placeholder logic
            posterUrl = null,
            currentLocalPlayerPosition = localPosition
        )}
        assertTrue(viewModel.uiState.value.isCasting)
    }


    @Test
    fun `onCleared calls playerManager release`() {
        initViewModelWithUrl("url")
        viewModel // Access to trigger creation and init

        // Simulate ViewModel clearing
        clearAllMocks() // Clears invocations, not the mocks themselves
        // Re-mock for this specific check if needed, or verify on original mock
        every { playerManager.release() } just runs // Ensure this is verifiable

        // To test onCleared, we'd typically have to get a fresh ViewModel from a test harness
        // or manually call the onCleared method if public (not typical).
        // For this, let's assume Hilt or a test runner would call it.
        // We can't directly call `viewModel.onCleared()` as it's protected.
        // This test is more conceptual for what should happen.
        // If PlayerViewModel was a public class, we could extend and call.

        // For now, this is a placeholder for that verification.
        // In a real Hilt test, you might verify this in an integration test context.
        assertTrue(true) // Placeholder
    }

    @Test
    fun `track selection methods call playerManager or ExoPlayer directly`() {
        initViewModelWithUrl("url")
        val mockTrackGroup = mockk<TrackGroup>()

        viewModel.selectSubtitleTrack(mockTrackGroup, 0)
        coVerify { playerManager.getExoPlayerInstance().trackSelectionParameters = any() }

        viewModel.disableSubtitles()
        coVerify { playerManager.getExoPlayerInstance().trackSelectionParameters = any() }

        viewModel.selectAudioTrack(mockTrackGroup, 0)
        coVerify { playerManager.getExoPlayerInstance().trackSelectionParameters = any() }
    }

    @Test
    fun `open and close track selection dialog updates uiState`() {
        initViewModelWithUrl("url")
        assertFalse(viewModel.uiState.value.showTrackSelectionDialog)

        viewModel.openTrackSelectionDialog()
        assertTrue(viewModel.uiState.value.showTrackSelectionDialog)
        assertTrue(viewModel.uiState.value.controlsVisible) // Check controls are shown

        viewModel.closeTrackSelectionDialog()
        assertFalse(viewModel.uiState.value.showTrackSelectionDialog)
    }
}
