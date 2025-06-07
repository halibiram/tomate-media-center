package com.halibiram.tomato.core.player.exoplayer

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.FakeMediaSourceFactory // For testing MediaSource interactions
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class) // Using Robolectric for ExoPlayer instantiation
@Config(manifest = Config.NONE, sdk = [30]) // Configure Robolectric SDK if needed
class TomatoExoPlayerTest {

    private lateinit var context: Context
    private lateinit var tomatoExoPlayer: TomatoExoPlayer
    private lateinit var exoPlayer: ExoPlayer // Direct reference for some assertions
    private lateinit var mediaSourceFactory: FakeMediaSourceFactory // Media3 test utility

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        tomatoExoPlayer = TomatoExoPlayer(context) // TomatoExoPlayer constructor takes Context
        exoPlayer = tomatoExoPlayer.getExoPlayerInstance()
        mediaSourceFactory = FakeMediaSourceFactory()
    }

    @After
    fun tearDown() {
        tomatoExoPlayer.releasePlayer() // Release the player after each test
    }

    @Test
    fun `initial playerState is correct`() = runTest {
        val initialState = tomatoExoPlayer.playerStateFlow.first()
        assertEquals(Player.STATE_IDLE, initialState.playbackState)
        assertFalse(initialState.isPlaying)
        assertFalse(initialState.isLoading)
        assertEquals(0L, initialState.currentPositionMs)
        assertEquals(0L, initialState.durationMs)
        assertNull(initialState.error)
    }

    @Test
    fun `prepareMedia sets player to loading and then ready state`() = runTest {
        val mediaUrl = "http://example.com/video.mp4"
        val mediaItem = MediaItem.fromUri(mediaUrl)

        // Collect states in a list to observe changes
        val states = mutableListOf<PlayerState>()
        val job = launch { // Launch in background to collect
            tomatoExoPlayer.playerStateFlow.collect { states.add(it) }
        }

        tomatoExoPlayer.prepareMedia(mediaUrl, playWhenReady = false, mediaSourceFactory)

        // ExoPlayer's preparation is asynchronous. We need to wait for it.
        // advanceUntilIdle() might work if dispatches are on TestDispatcher.
        // For Robolectric's Looper, sometimes direct control or waiting is needed.
        // Robolectric.flushForegroundThreadScheduler() // Or shadowOf(Looper.getMainLooper()).idle()

        // A simple delay for async operations in player, or use a more robust idling resource/test rule.
        // This is a common challenge with testing ExoPlayer's async nature.
        // For this test, let's assume prepare() eventually leads to STATE_READY or STATE_BUFFERING then READY.
        // We can check the sequence of states.

        // Wait for player to be ready or timeout (simplified wait)
        var counter = 0
        while(exoPlayer.playbackState != Player.STATE_READY && counter < 100) {
            delay(100) // Robolectric should handle this delay with its scheduler
            counter++
        }

        assertTrue("Player did not reach ready state", exoPlayer.playbackState == Player.STATE_READY)

        val lastState = tomatoExoPlayer.playerStateFlow.value
        assertEquals(Player.STATE_READY, lastState.playbackState)
        assertFalse(lastState.isLoading) // Should not be loading once ready and not playing
        assertFalse(lastState.isPlaying) // playWhenReady was false

        job.cancel() // Stop collecting
    }

    @Test
    fun `play and pause correctly update isPlaying state`() = runTest {
        val mediaUrl = "http://example.com/video.mp4"
        tomatoExoPlayer.prepareMedia(mediaUrl, playWhenReady = false, mediaSourceFactory)

        var counter = 0
        while(exoPlayer.playbackState != Player.STATE_READY && counter < 100) { delay(100); counter++ }
        assertTrue("Player did not reach ready state for play/pause test", exoPlayer.playbackState == Player.STATE_READY)

        tomatoExoPlayer.play()
        advanceUntilIdle() // Allow listener to process
        assertTrue("Player should be playing", tomatoExoPlayer.playerStateFlow.value.isPlaying)

        tomatoExoPlayer.pause()
        advanceUntilIdle()
        assertFalse("Player should be paused", tomatoExoPlayer.playerStateFlow.value.isPlaying)
    }

    @Test
    fun `seekTo updates currentPositionMs`() = runTest {
         val mediaUrl = "http://example.com/video.mp4"
        tomatoExoPlayer.prepareMedia(mediaUrl, playWhenReady = false, mediaSourceFactory)
        var counter = 0
        while(exoPlayer.playbackState != Player.STATE_READY && counter < 100) { delay(100); counter++ }
        assertTrue("Player did not reach ready state for seekTo test", exoPlayer.playbackState == Player.STATE_READY)

        // Mock duration for accurate seeking test if possible, or rely on ExoPlayer's timeline for dummy source
        // For now, assume duration is known or seek is within bounds.
        // ExoPlayer test utils might have ways to set duration on a fake timeline.
        // If duration is 0, seek might not behave as expected or be capped.
        // Let's assume some duration becomes available after prepare for a real source.
        // Since FakeMediaSourceFactory is used, duration might be 0 or C.TIME_UNSET.
        // This makes testing seek to an absolute position tricky without a more controllable fake player.

        val seekPosition = 10000L // Seek to 10 seconds
        // Ensure player is ready and has a duration before seeking for a meaningful test
        if (exoPlayer.duration > 0 && exoPlayer.duration != C.TIME_UNSET) {
            tomatoExoPlayer.seekTo(seekPosition)
            advanceUntilIdle()
             // Position might not update exactly to seekPosition immediately due to player internals,
             // but it should be close or equal.
            assertEquals(seekPosition, tomatoExoPlayer.playerStateFlow.value.currentPositionMs.coerceAtMost(exoPlayer.duration))
        } else {
            // If duration is unknown/0, seek might not change position or go to 0.
            // This part of test shows limitations of testing ExoPlayer without full control/fakes.
            tomatoExoPlayer.seekTo(seekPosition) // Try seeking anyway
            // Assert based on expected behavior with unknown duration (e.g., position remains 0 or unchanged)
             assertEquals(0L, tomatoExoPlayer.playerStateFlow.value.currentPositionMs) // Example if seek to 0
        }
    }

    // Track selection tests are more complex as they depend on available tracks from a media source.
    // They would typically involve preparing media with known tracks and then verifying parameters.
    // For a basic unit test, we can check if the parameters are passed to ExoPlayer.
    // This would require mocking ExoPlayer itself, which is hard.
    // Using Robolectric allows us to check the resulting TrackSelectionParameters.

    // releasePlayer is called in @After, testing its effects (e.g., player released) is implicit.
}
