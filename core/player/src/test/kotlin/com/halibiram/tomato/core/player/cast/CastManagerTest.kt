package com.halibiram.tomato.core.player.cast

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [30])
class CastManagerTest {

    private lateinit var context: Context
    private lateinit var mockCastContext: CastContext
    private lateinit var mockSessionManager: SessionManager
    private lateinit var mockCastSession: CastSession
    private lateinit var mockRemoteMediaClient: RemoteMediaClient

    // Listener that CastManager will add
    private lateinit var sessionManagerListenerSlot: CapturingSlot<SessionManagerListener<CastSession>>


    // This setup is complex due to static getInstance and final classes in CastSDK.
    // PowerMock or more involved Robolectric shadows might be needed for full testability.
    // Here, we mock what CastContext.getSharedInstance would return.
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Mock static CastContext.getSharedInstance(context)
        mockkStatic(CastContext::class)

        mockCastContext = mockk(relaxed = true)
        mockSessionManager = mockk(relaxed = true)
        mockCastSession = mockk(relaxed = true)
        mockRemoteMediaClient = mockk(relaxed = true)

        every { CastContext.getSharedInstance(any<Context>()) } returns mockCastContext
        every { mockCastContext.sessionManager } returns mockSessionManager
        every { mockCastContext.isCastAvailable } returns true // Assume cast is available initially
        every { mockCastContext.castState } returns com.google.android.gms.cast.framework.CastState.NOT_CONNECTED

        // Capture the listener
        sessionManagerListenerSlot = slot()
        every { mockSessionManager.addSessionManagerListener(capture(sessionManagerListenerSlot), CastSession::class.java) } just runs
        every { mockSessionManager.removeSessionManagerListener(any(), CastSession::class.java) } just runs

        every { mockCastSession.isConnected } returns true // Assume session connects successfully
        every { mockCastSession.remoteMediaClient } returns mockRemoteMediaClient

        // Mock RemoteMediaClient.load to return a successful result
        val mockPendingResult = mockk<PendingResult<RemoteMediaClient.MediaChannelResult>>(relaxed = true)
        val mockStatus = mockk<Status>(relaxed = true)
        every { mockStatus.isSuccess } returns true
        val mockMediaChannelResult = mockk<RemoteMediaClient.MediaChannelResult>(relaxed = true)
        every { mockMediaChannelResult.status } returns mockStatus
        every { mockPendingResult.setResultCallback(any()) } answers {
            // Immediately invoke callback with success for testing loadRemoteMedia
            val callback = firstArg<RemoteMediaClient.ResultCallback<RemoteMediaClient.MediaChannelResult>>()
            callback.onResult(mockMediaChannelResult)
        }
        every { mockRemoteMediaClient.load(any<MediaLoadRequestData>()) } returns mockPendingResult
    }

    @After
    fun tearDown() {
        unmockkStatic(CastContext::class)
    }

    @Test
    fun `initial castState is NO_DEVICES_AVAILABLE if CastContext init fails or no devices`() = runTest {
        // Simulate CastContext.getSharedInstance throwing an error or returning null/unavailable state
        every { CastContext.getSharedInstance(any<Context>()) } throws IllegalStateException("Play services error")

        val castManager = CastManager(context) // Init will run

        assertEquals(CastState.NO_DEVICES_AVAILABLE, castManager.castState.first())
        assertFalse(castManager.isCastAvailable.first())
    }

    @Test
    fun `castState updates when CastContext state changes`() = runTest {
        val castManager = CastManager(context) // Init adds listener
        val castStateListenerSlot = slot<(Int) -> Unit>()
        every { mockCastContext.addCastStateListener(capture(castStateListenerSlot)) } just runs

        // Re-initialize to capture the listener added in init
        val freshCastManager = CastManager(context)
        assertTrue(castStateListenerSlot.isCaptured)

        // Simulate state changes from Cast SDK
        castStateListenerSlot.captured.invoke(com.google.android.gms.cast.framework.CastState.CONNECTING)
        assertEquals(CastState.CONNECTING, freshCastManager.castState.value)

        castStateListenerSlot.captured.invoke(com.google.android.gms.cast.framework.CastState.CONNECTED)
        assertEquals(CastState.CONNECTED, freshCastManager.castState.value)

        castStateListenerSlot.captured.invoke(com.google.android.gms.cast.framework.CastState.NOT_CONNECTED)
        assertEquals(CastState.NOT_CONNECTED, freshCastManager.castState.value)
    }


    @Test
    fun `sessionManagerListener onSessionStarted updates castState to CONNECTED`() = runTest {
        val castManager = CastManager(context) // Listener is added in init
        assertTrue(sessionManagerListenerSlot.isCaptured)

        sessionManagerListenerSlot.captured.onSessionStarted(mockCastSession, "test_session_id")
        assertEquals(CastState.CONNECTED, castManager.castState.value)
    }

    @Test
    fun `sessionManagerListener onSessionEnded updates castState to NOT_CONNECTED`() = runTest {
        val castManager = CastManager(context)
        assertTrue(sessionManagerListenerSlot.isCaptured)

        // Simulate being connected first
        sessionManagerListenerSlot.captured.onSessionStarted(mockCastSession, "test_session_id")
        assertEquals(CastState.CONNECTED, castManager.castState.value)

        // Then session ends
        sessionManagerListenerSlot.captured.onSessionEnded(mockCastSession, 0)
        assertEquals(CastState.NOT_CONNECTED, castManager.castState.value)
    }


    @Test
    fun `loadRemoteMedia calls remoteMediaClient load with correct MediaInfo`() = runTest {
        val castManager = CastManager(context)
        // Simulate session started to have a castSession instance
        sessionManagerListenerSlot.captured.onSessionStarted(mockCastSession, "test_session_id")

        val mediaUrl = "http://example.com/movie.mp4"
        val title = "Test Movie"
        val posterUrl = "http://example.com/poster.jpg"
        val mimeType = "video/mp4"
        val position = 12345L

        castManager.loadRemoteMedia(mediaUrl, title, posterUrl, mimeType, position)

        val mediaLoadRequestDataSlot = slot<MediaLoadRequestData>()
        coVerify { mockRemoteMediaClient.load(capture(mediaLoadRequestDataSlot)) }

        val capturedRequest = mediaLoadRequestDataSlot.captured
        assertNotNull(capturedRequest)
        assertEquals(mediaUrl, capturedRequest.mediaInfo?.contentId ?: capturedRequest.mediaInfo?.contentUrl)
        assertEquals(title, capturedRequest.mediaInfo?.metadata?.getString(MediaMetadata.KEY_TITLE))
        assertEquals(posterUrl, capturedRequest.mediaInfo?.metadata?.images?.firstOrNull()?.url?.toString())
        assertEquals(mimeType, capturedRequest.mediaInfo?.contentType)
        assertEquals(position, capturedRequest.currentTime)
        assertTrue(capturedRequest.autoplay)
    }

    @Test
    fun `loadRemoteMedia does nothing if session is null or not connected`() = runTest {
        val castManager = CastManager(context)
        // Do NOT start a session, so castSession remains null or not connected
        every { mockCastSession.isConnected } returns false

        castManager.loadRemoteMedia("url", "title", null)

        coVerify(exactly = 0) { mockRemoteMediaClient.load(any()) }
    }

    @Test
    fun `cleanup removes session manager listener`() {
        val castManager = CastManager(context)
        castManager.cleanup()
        verify { mockSessionManager.removeSessionManagerListener(sessionManagerListenerSlot.captured, CastSession::class.java) }
    }
}
