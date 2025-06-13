package com.halibiram.tomato.feature.player.screen

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Rational
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.alpha // Not directly used, AnimatedVisibility handles fade
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.platform.LocalDensity // Not explicitly used
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.halibiram.tomato.feature.player.viewmodel.PlayerViewModel
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope // Already available via rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Helper function to format milliseconds (already defined)
fun Long.formatMinSec(): String {
    return if (this < 0 || this == Long.MAX_VALUE) {
        "--:--"
    } else {
        val hours = TimeUnit.MILLISECONDS.toHours(this)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
        if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}

// Enum for gesture type
enum class GestureType { NONE, BRIGHTNESS, VOLUME }

@Composable
fun VideoPlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
    mediaUri: Uri?
) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val window = activity?.window
    val view = LocalView.current

    var isFullscreen by remember { mutableStateOf(false) }
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val totalDuration by viewModel.totalDuration.collectAsState()

    val currentTestStreamUri = mediaUri ?: Uri.parse("https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd")
    val player = remember { viewModel.player }

    var isSeeking by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }

    var controlsVisible by remember { mutableStateOf(true) }
    var controlsTimeoutJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    var currentGestureType by remember { mutableStateOf(GestureType.NONE) }
    var gestureIndicatorVisible by remember { mutableStateOf(false) }
    var gestureIndicatorValue by remember { mutableFloatStateOf(0f) }
    var gestureIndicatorJob by remember { mutableStateOf<Job?>(null) }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    fun showControlsTemporarily(hideAfterMillis: Long = 3000) {
        controlsVisible = true
        controlsTimeoutJob?.cancel()
        controlsTimeoutJob = scope.launch {
            delay(hideAfterMillis)
            if(!isSeeking) controlsVisible = false
        }
    }

    fun showGestureIndicator(type: GestureType, value: Float) {
        currentGestureType = type
        gestureIndicatorValue = value.coerceIn(0f, 1f) // Ensure value is between 0 and 1 for progress
        gestureIndicatorVisible = true
        gestureIndicatorJob?.cancel()
        gestureIndicatorJob = scope.launch {
            delay(1500)
            gestureIndicatorVisible = false
        }
    }

    LaunchedEffect(Unit) { showControlsTemporarily() }
    LaunchedEffect(isSeeking) {
        if(isSeeking) {
            controlsTimeoutJob?.cancel()
            controlsVisible = true
        } else {
            showControlsTemporarily()
        }
    }

    fun setFullscreenImpl(fullscreen: Boolean) { // Renamed to avoid conflict if passed as lambda
        isFullscreen = fullscreen
        if (window != null) {
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            if (fullscreen) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controlsVisible = false
            } else {
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                showControlsTemporarily()
            }
        }
    }

    fun enterPiPModeImpl() { // Renamed to avoid conflict
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) == true) {
            val aspectRatio = Rational(16, 9)
            val pipParams = PictureInPictureParams.Builder().setAspectRatio(aspectRatio).build()
            activity.enterPictureInPictureMode(pipParams)
            controlsVisible = false
        }
    }

    BackHandler(enabled = isFullscreen) { setFullscreenImpl(false) }

    DisposableEffect(currentTestStreamUri) {
        viewModel.preparePlayer(currentTestStreamUri)
        onDispose {
            if (isFullscreen) setFullscreenImpl(false) // Ensure fullscreen is exited
            controlsTimeoutJob?.cancel()
            gestureIndicatorJob?.cancel()
        }
    }

    LaunchedEffect(currentPosition, isPlaying) {
        if (!isSeeking) {
            sliderValue = currentPosition.toFloat()
        }
    }
    LaunchedEffect(totalDuration) {
        if (totalDuration > 0 && currentPosition <= totalDuration) {
             sliderValue = currentPosition.toFloat()
        } else if (totalDuration == 0L) {
             sliderValue = 0f
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = (if (isFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth().aspectRatio(16 / 9f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (controlsVisible) controlsVisible = false else showControlsTemporarily()
                        },
                        onDoubleTap = { offset ->
                            val screenWidth = size.width
                            if (offset.x < screenWidth / 3) viewModel.seekRewind()
                            else if (offset.x > screenWidth * 2 / 3) viewModel.seekForward()
                            else { if (viewModel.isPlaying.value) viewModel.pause() else viewModel.play() }
                            showControlsTemporarily()
                        }
                    )
                }
                .pointerInput(Unit) {
                    var dragConsumed: Boolean
                    var initialGestureType: GestureType

                    detectDragGestures(
                        onDragStart = { offset ->
                            dragConsumed = false
                            initialGestureType = if (offset.x < size.width / 3f ) GestureType.BRIGHTNESS
                                               else if (offset.x > size.width * 2f / 3f) GestureType.VOLUME
                                               else GestureType.NONE
                            if(initialGestureType != GestureType.NONE) showControlsTemporarily(5000)
                        },
                        onDrag = { change, dragAmount ->
                            if (initialGestureType == GestureType.NONE) return@detectDragGestures

                            val verticalDrag = dragAmount.y
                            if (abs(verticalDrag) > abs(dragAmount.x) * 1.2f) { // Prioritize vertical
                                change.consume()
                                dragConsumed = true

                                if (initialGestureType == GestureType.BRIGHTNESS && activity != null && window != null) {
                                    val currentSysBrightness = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, -1)
                                    val currentWindowBrightness = window.attributes.screenBrightness
                                    val effectiveCurrentBrightness = if(currentWindowBrightness > 0) currentWindowBrightness else (currentSysBrightness / 255f)

                                    val newBrightness = (effectiveCurrentBrightness - verticalDrag / (size.height * 0.7f)).coerceIn(0.05f, 1f)
                                    val lp = window.attributes
                                    lp.screenBrightness = newBrightness
                                    window.attributes = lp
                                    showGestureIndicator(GestureType.BRIGHTNESS, newBrightness)
                                } else if (initialGestureType == GestureType.VOLUME) {
                                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                    // More sensitive drag: smaller screen fraction for full volume range
                                    val delta = -(verticalDrag / (size.height * 0.5f)) * maxVolume
                                    val newVolume = (currentVolume + delta).coerceIn(0f, maxVolume.toFloat()).toInt()
                                    try {
                                       audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                                    } catch (e: SecurityException) {
                                       // Log or inform user about restriction
                                    }
                                    showGestureIndicator(GestureType.VOLUME, newVolume.toFloat() / maxVolume)
                                }
                            }
                        },
                        onDragEnd = {
                            if (dragConsumed) showControlsTemporarily()
                            initialGestureType = GestureType.NONE // Reset
                        }
                    )
                }
        ) {
            AndroidView(
                factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = false } },
                modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
                visible = gestureIndicatorVisible,
                enter = fadeIn(initialAlpha = 0.3f), exit = fadeOut(targetAlpha = 0.3f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = if (currentGestureType == GestureType.BRIGHTNESS) Icons.Filled.BrightnessMedium else Icons.Filled.VolumeUp,
                        contentDescription = currentGestureType.name,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { gestureIndicatorValue }, // Updated for Compose 1.6+
                        modifier = Modifier.width(120.dp).height(8.dp), // Make thicker
                        color = Color.White,
                        trackColor = Color.Gray.copy(alpha = 0.5f),
                    )
                }
            }

            AnimatedVisibility(
                visible = controlsVisible && !isFullscreen,
                enter = fadeIn(), exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Slider(
                            value = sliderValue,
                            onValueChange = { newValue -> isSeeking = true; sliderValue = newValue },
                            valueRange = 0f..(totalDuration.toFloat().coerceAtLeast(0f).takeIf { it > 0f } ?: 0f),
                            onValueChangeFinished = { viewModel.seekTo(sliderValue.toLong()); isSeeking = false; showControlsTemporarily() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = if(isSeeking) sliderValue.toLong().formatMinSec() else currentPosition.formatMinSec(), color = Color.White)
                            Text(text = totalDuration.formatMinSec(), color = Color.White)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousTrack(); showControlsTemporarily() }) { Icon(Icons.Filled.SkipPrevious, "Previous", tint = Color.White) }
                        IconButton(onClick = { viewModel.seekRewind(); showControlsTemporarily() }) { Icon(Icons.Filled.FastRewind, "Rewind", tint = Color.White) }
                        IconButton(onClick = { if (isPlaying) viewModel.pause() else viewModel.play(); showControlsTemporarily() }) {
                            Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (isPlaying) "Pause" else "Play", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.seekForward(); showControlsTemporarily() }) { Icon(Icons.Filled.FastForward, "Forward", tint = Color.White) }
                        IconButton(onClick = { viewModel.nextTrack(); showControlsTemporarily() }) { Icon(Icons.Filled.SkipNext, "Next", tint = Color.White) }
                    }
                }
            }

            if (isFullscreen && controlsVisible) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    IconButton(onClick = { setFullscreenImpl(false); showControlsTemporarily() }, modifier = Modifier.align(Alignment.TopStart)) {
                        Icon(Icons.Filled.FullscreenExit, "Exit Fullscreen", tint = Color.White)
                    }
                }
            }
        }

        if (!isFullscreen) {
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { setFullscreenImpl(!isFullscreen) }) {
                    Icon(if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen, "Fullscreen")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) == true) {
                    IconButton(onClick = { enterPiPModeImpl() }) { Icon(Icons.Filled.PictureInPictureAlt, "PiP") }
                }
            }
        }
    }
}
