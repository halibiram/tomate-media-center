package com.halibiram.tomato.feature.player.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import com.halibiram.tomato.core.player.exoplayer.PlayerState // Core state
import com.halibiram.tomato.feature.player.presentation.component.PlayerControls
import com.halibiram.tomato.ui.theme.TomatoTheme

// Helper to find activity from context (already defined in previous step's PlayerScreen)
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@SuppressLint("SourceLockedOrientationActivity") // If locking orientation
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    // mediaUrl: String, // Passed via NavHost in real app, ViewModel gets from SavedStateHandle
    onNavigateBack: () -> Unit
) {
    val corePlayerState by viewModel.corePlayerState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Screen lifecycle management (fullscreen, keep screen on, orientation)
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        activity?.let {
            val originalSystemUiVisibility = it.window.decorView.systemUiVisibility
            val originalLayoutParamsFlags = it.window.attributes.flags
            val originalOrientation = it.requestedOrientation

            // Enter immersive mode & keep screen on
            it.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
            it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            // Lock to landscape for video playback
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        onDispose {
            activity?.let {
                it.window.decorView.systemUiVisibility = originalSystemUiVisibility
                // Be careful when restoring flags, ensure it's exactly what was there.
                // it.window.attributes.flags = originalLayoutParamsFlags // This might be too broad
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                it.requestedOrientation = originalOrientation // Restore original orientation
            }
        }
    }

    // Handle Android back button press for custom behavior (e.g., exit fullscreen first)
    BackHandler {
        // If in fullscreen and want to exit fullscreen first:
        // if (uiState.isFullScreen) { viewModel.toggleFullScreen() } else { onNavigateBack() }
        onNavigateBack()
    }

    // Observe lifecycle events for play/pause
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.pause()
                Lifecycle.Event.ON_RESUME -> viewModel.play() // Or based on previous state
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable( // Click on the player area to toggle controls visibility
                interactionSource = remember { MutableInteractionSource() },
                indication = null // No ripple effect on the player view itself
            ) {
                if (uiState.controlsVisible) {
                    viewModel.hideControls() // If visible, tapping screen hides them
                } else {
                    viewModel.showControlsThenAutoHide() // If hidden, tapping shows them temporarily
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.getExoPlayerInstance()
                    useController = false // Using custom Compose controls
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Adjust resizeMode as needed, e.g., RESIZE_MODE_FIT, RESIZE_MODE_ZOOM
                    // setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading Indicator centered on screen
        if (corePlayerState.isLoading && !corePlayerState.isPlaying) { // Show loading only if not already playing (e.g. initial buffer)
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // Player Controls (conditionally visible)
        // PlayerControls is now animated internally by PlayerOverlay in the original design
        // For this step, let's directly use PlayerControls if PlayerOverlay is not yet implemented
        // or if we want to simplify. The prompt focuses on PlayerControls.
        // The original PlayerScreen had PlayerGestures wrapping PlayerOverlay, which then had PlayerControls.
        // Let's assume PlayerControls is directly used for now, visibility handled by its own logic or uiState.controlsVisible

        PlayerControls( // Assuming PlayerControls handles its own visibility via AnimatedVisibility or similar
            isVisible = uiState.controlsVisible,
            playerState = corePlayerState, // Pass the core player state
            mediaTitle = uiState.mediaTitle,
            onPlayPauseToggle = { viewModel.togglePlayPause() },
            onSeekForward = { viewModel.seekTo(corePlayerState.currentPositionMs + 10000) }, // Example 10s
            onSeekBackward = { viewModel.seekTo(corePlayerState.currentPositionMs - 10000) }, // Example 10s
            onFullScreenToggle = { viewModel.toggleFullScreen() },
            onSettingsClick = { /* TODO: Show settings like subtitles/quality */ },
            onSeekTo = { newPositionMs -> viewModel.seekTo(newPositionMs) }, // For slider interaction
            // Pass subtitle related callbacks and data
            // availableSubtitleTracks = uiState.availableSubtitleTracks,
            // selectedSubtitleTrackParams = uiState.selectedSubtitleTrackParams,
            // onSubtitleTrackSelected = { group, trackIndex -> viewModel.selectSubtitleTrack(group, trackIndex) },
            // onClearSubtitles = { viewModel.clearSubtitles() },
            modifier = Modifier.fillMaxSize() // PlayerControls will align itself (e.g., to bottom)
        )


        // Display Player Errors (if any)
        corePlayerState.error?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Playback Error: ${error.message ?: "Unknown error"}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                // Optional: Add a retry button here that calls viewModel.initializeAndPlayMedia() or similar
            }
        }
    }
}

// Preview setup for PlayerScreen is complex due to ExoPlayer and SavedStateHandle.
// A simplified preview might pass a fake ViewModel or mock PlayerManager.
@Preview(showBackground = true)
@Composable
fun PlayerScreenPreview() {
    // This preview will likely not render the ExoPlayer view correctly.
    // It's for basic layout checks of controls if they were visible.
    // A FakePlayerViewModel would be needed for more meaningful previews.
    TomatoTheme {
        // PlayerScreen(onNavigateBack = {}) // This would crash without Hilt/proper ViewModel
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Text("Player Screen Preview (ExoPlayer view not available in standard preview)", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
    }
}
