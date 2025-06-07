package com.halibiram.tomato.core.player.exoplayer

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class PlayerManagerTest {

    private lateinit var tomatoExoPlayer: TomatoExoPlayer
    private lateinit var mediaSourceFactory: MediaSourceFactory
    private lateinit var playerManager: PlayerManager

    @BeforeEach
    fun setUp() {
        tomatoExoPlayer = mockk(relaxed = true) // Relaxed mock for TomatoExoPlayer
        mediaSourceFactory = mockk() // Standard mock for MediaSourceFactory
        playerManager = PlayerManager(tomatoExoPlayer, mediaSourceFactory)

        // Stubbing for getExoPlayerInstance if it's called by methods under test, though not directly by these.
        // every { tomatoExoPlayer.getExoPlayerInstance() } returns mockk(relaxed = true)
    }

    @Test
    fun `playMedia calls TomatoExoPlayer prepareMedia with correct parameters`() {
        val mediaUrl = "http://example.com/video.mp4"
        val playWhenReady = true // Default for playMedia

        // Capture the MediaSourceFactory instance passed to prepareMedia
        val mediaSourceFactorySlot = slot<MediaSourceFactory>()
        coJustRun { tomatoExoPlayer.prepareMedia(any(), any(), capture(mediaSourceFactorySlot)) }

        playerManager.playMedia(mediaUrl, playWhenReady)

        coVerify { tomatoExoPlayer.prepareMedia(mediaUrl, playWhenReady, mediaSourceFactory) }
        // Assert that the captured factory is the one we provided (optional, but good for sanity)
        // assertEquals(mediaSourceFactory, mediaSourceFactorySlot.captured) // This might fail if they are different instances but behaviorally same.
                                                                        // For this test, verifying the call is enough.
    }

    @Test
    fun `pausePlayback calls TomatoExoPlayer pause`() {
        coJustRun { tomatoExoPlayer.pause() }
        playerManager.pausePlayback()
        coVerify { tomatoExoPlayer.pause() }
    }

    @Test
    fun `resumePlayback calls TomatoExoPlayer play`() {
        coJustRun { tomatoExoPlayer.play() }
        playerManager.resumePlayback()
        coVerify { tomatoExoPlayer.play() }
    }

    @Test
    fun `seekPlayback calls TomatoExoPlayer seekTo`() {
        val positionMs = 15000L
        coJustRun { tomatoExoPlayer.seekTo(any()) }
        playerManager.seekPlayback(positionMs)
        coVerify { tomatoExoPlayer.seekTo(positionMs) }
    }

    @Test
    fun `release calls TomatoExoPlayer releasePlayer`() {
        coJustRun { tomatoExoPlayer.releasePlayer() }
        playerManager.release()
        coVerify { tomatoExoPlayer.releasePlayer() }
    }

    @Test
    fun `getExoPlayerInstance calls corresponding method on TomatoExoPlayer`() {
        val mockExo = mockk<androidx.media3.exoplayer.ExoPlayer>()
        every { tomatoExoPlayer.getExoPlayerInstance() } returns mockExo

        val actualExo = playerManager.getExoPlayerInstance()

        verify { tomatoExoPlayer.getExoPlayerInstance() }
        assertEquals(mockExo, actualExo)
    }

    // Tests for track selection delegation
    @Test
    fun `setSubtitleTrack calls corresponding method on TomatoExoPlayer`() {
        val trackGroup = mockk<androidx.media3.common.TrackGroup>()
        val trackIndex = 0
        val builder = mockk<androidx.media3.common.TrackSelectionParameters.Builder>()
        coJustRun { tomatoExoPlayer.setSubtitleTrack(trackIndex, any(), builder) }

        playerManager.setSubtitleTrack(trackIndex, 0, builder) // groupIndex passed as 0 for simplicity

        coVerify { tomatoExoPlayer.setSubtitleTrack(trackIndex, 0, builder) }
    }

    @Test
    fun `clearSubtitleTrack calls corresponding method on TomatoExoPlayer`() {
        coJustRun { tomatoExoPlayer.clearSubtitleTrack() }
        playerManager.clearSubtitleTrack()
        coVerify { tomatoExoPlayer.clearSubtitleTrack() }
    }

    @Test
    fun `getAvailableSubtitleTracks calls corresponding method on TomatoExoPlayer`() {
        val expectedTracks = listOf(mockk<androidx.media3.common.TrackGroup>())
        every { tomatoExoPlayer.getAvailableSubtitleTracks() } returns expectedTracks

        val actualTracks = playerManager.getAvailableSubtitleTracks()

        verify { tomatoExoPlayer.getAvailableSubtitleTracks() }
        assertEquals(expectedTracks, actualTracks)
    }
}
