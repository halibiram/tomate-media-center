package com.halibiram.tomato.core.player.cast

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.common.images.WebImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class CastState {
    NO_DEVICES_AVAILABLE,
    NOT_CONNECTED,
    CONNECTING,
    CONNECTED
}

@Singleton
class CastManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _castState = MutableStateFlow(CastState.NO_DEVICES_AVAILABLE)
    val castState: StateFlow<CastState> = _castState.asStateFlow()

    private val _isCastAvailable = MutableStateFlow(false)
    val isCastAvailable: StateFlow<Boolean> = _isCastAvailable.asStateFlow()

    private var castContext: CastContext? = null
    private var castSession: CastSession? = null
    private val sessionManagerListener: SessionManagerListener<CastSession> = CastSessionManagerListener()

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    init {
        try {
            // Initialize CastContext. This can sometimes throw IllegalStateException
            // if Google Play services is not available or version is incorrect.
            // Lazy initialization or explicit init method might be safer.
            castContext = CastContext.getSharedInstance(context)
            _isCastAvailable.value = castContext?.isCastAvailable ?: false
            castContext?.sessionManager?.addSessionManagerListener(sessionManagerListener, CastSession::class.java)
            updateCastState(castContext?.castState ?: com.google.android.gms.cast.framework.CastState.NO_DEVICES_AVAILABLE)

            // Listen for Cast State changes
            castContext?.addCastStateListener { state ->
                updateCastState(state)
                _isCastAvailable.value = castContext?.isCastAvailable ?: false
            }

        } catch (e: Exception) {
            Log.e("CastManager", "Error initializing CastContext: ${e.message}", e)
            _castState.value = CastState.NO_DEVICES_AVAILABLE // Or a specific error state
            _isCastAvailable.value = false
        }
    }

    private fun updateCastState(frameworkCastState: Int) {
         when (frameworkCastState) {
            com.google.android.gms.cast.framework.CastState.NO_DEVICES_AVAILABLE ->
                _castState.value = CastState.NO_DEVICES_AVAILABLE
            com.google.android.gms.cast.framework.CastState.NOT_CONNECTED ->
                _castState.value = CastState.NOT_CONNECTED
            com.google.android.gms.cast.framework.CastState.CONNECTING ->
                _castState.value = CastState.CONNECTING
            com.google.android.gms.cast.framework.CastState.CONNECTED ->
                _castState.value = CastState.CONNECTED
            else ->
                 _castState.value = CastState.NO_DEVICES_AVAILABLE // Default or unknown
        }
    }


    fun loadRemoteMedia(
        mediaUrl: String,
        title: String,
        posterUrl: String?,
        mimeType: String = "video/mp4", // Default, should be dynamic if possible
        currentLocalPlayerPosition: Long = 0L // In milliseconds
    ) {
        coroutineScope.launch { // Ensure this is on a suitable dispatcher if any blocking calls occur
            if (castSession == null || !castSession!!.isConnected) {
                Log.w("CastManager", "Cast session not available or not connected.")
                _castState.update { if (it == CastState.CONNECTED) CastState.NOT_CONNECTED else it }
                return@launch
            }

            val remoteMediaClient = castSession!!.remoteMediaClient
            if (remoteMediaClient == null) {
                Log.w("CastManager", "RemoteMediaClient not available.")
                return@launch
            }

            val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE) // Or MEDIA_TYPE_TV_SHOW
            metadata.putString(MediaMetadata.KEY_TITLE, title)
            // metadata.putString(MediaMetadata.KEY_SUBTITLE, "Optional Subtitle or Series Title")
            posterUrl?.let {
                metadata.addImage(WebImage(Uri.parse(it)))
            }
            // Add more metadata like series title, episode number, etc. if applicable

            val mediaInfo = MediaInfo.Builder(mediaUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED) // Or STREAM_TYPE_LIVE
                .setContentType(mimeType)
                .setMetadata(metadata)
                // .setStreamDuration(durationMs) // Optional: if known
                .build()

            val requestData = MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(true)
                .setCurrentTime(currentLocalPlayerPosition) // Start casting from this position
                .build()

            remoteMediaClient.load(requestData).setResultCallback { result ->
                if (result.isSuccess) {
                    Log.d("CastManager", "Remote media loaded successfully.")
                } else {
                    Log.e("CastManager", "Error loading remote media: ${result.status.statusMessage}")
                    // Handle error: update UI, show message, etc.
                }
            }
        }
    }

    fun endCurrentSession() {
        castContext?.sessionManager?.endCurrentSession(true)
    }

    fun cleanup() {
        castContext?.sessionManager?.removeSessionManagerListener(sessionManagerListener, CastSession::class.java)
        // castContext?.removeCastStateListener { ... } // Need to store the listener instance to remove it
        castSession = null
    }

    private inner class CastSessionManagerListener : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            Log.d("CastManager", "Cast session started: $sessionId")
            castSession = session
            _castState.value = CastState.CONNECTED
            // Application should now switch playback to the Cast device.
            // The UI (ViewModel) should observe castState and trigger loadRemoteMedia.
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            Log.d("CastManager", "Cast session resumed. Was suspended: $wasSuspended")
            castSession = session
            _castState.value = CastState.CONNECTED
            // Resume playback on Cast device or update UI.
        }

        override fun onSessionSuspended(session: CastSession, reason: Int) {
            Log.d("CastManager", "Cast session suspended. Reason: $reason")
            castSession = session // Still the current session, but suspended
            _castState.value = CastState.CONNECTING // Or a new "SUSPENDED" state
            // Update UI, local player might show as paused or disconnected.
        }

        override fun onSessionStarting(session: CastSession) {
            Log.d("CastManager", "Cast session starting.")
            castSession = session
            _castState.value = CastState.CONNECTING
        }

        override fun onSessionEnding(session: CastSession) {
            Log.d("CastManager", "Cast session ending.")
            // UI should prepare to switch back to local playback if it was casting.
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            Log.d("CastManager", "Cast session ended. Error code: $error")
            if (castSession == session) {
                castSession = null
            }
            _castState.value = CastState.NOT_CONNECTED
            // Switch back to local playback.
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {
            Log.e("CastManager", "Cast session start failed. Error code: $error")
            if (castSession == session) {
                castSession = null
            }
            _castState.value = CastState.NOT_CONNECTED // Or a specific error state
        }

        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            Log.e("CastManager", "Cast session resume failed. Error code: $error")
            _castState.value = CastState.NOT_CONNECTED
        }
    }
}
