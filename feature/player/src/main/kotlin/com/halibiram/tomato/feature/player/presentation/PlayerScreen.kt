package com.halibiram.tomato.feature.player.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
// import com.google.android.exoplayer2.ExoPlayer // Assuming ExoPlayer
// import com.google.android.exoplayer2.ui.StyledPlayerView // ExoPlayer's UI component
import com.halibiram.tomato.feature.player.presentation.component.PlayerGestures
import com.halibiram.tomato.feature.player.presentation.component.PlayerOverlay
import com.halibiram.tomato.ui.theme.TomatoTheme

// Helper to find activity from context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun PlayerScreen(
    // viewModel: PlayerViewModel = hiltViewModel(), // With Hilt
    viewModel: PlayerViewModel, // Pass for preview or non-Hilt
    // exoPlayer: ExoPlayer, // Pass ExoPlayer instance if managed by DI/Activity
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isScreenVisible by remember { mutableStateOf(true) } // To manage player release

    // Manage screen orientation and system UI (immersive mode)
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        activity?.let {
            // Store original flags
            val originalSystemUiVisibility = it.window.decorView.systemUiVisibility
            val originalLayoutParams = it.window.attributes.flags

            // Enter immersive mode
            it.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
            it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            // Consider orientation lock here if needed: activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        onDispose {
            activity?.let {
                // Restore original system UI visibility and layout params
                it.window.decorView.systemUiVisibility = originalSystemUiVisibility
                it.window.attributes.flags = originalLayoutParams // This might need more care to restore properly
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                // activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    // Lifecycle observer for pausing/resuming player
    // LocalLifecycleOwner.current.lifecycle.addObserver(remember {
    //     LifecycleEventObserver { _, event ->
    //         when (event) {
    //             Lifecycle.Event.ON_PAUSE -> if (isScreenVisible) exoPlayer.pause()
    //             Lifecycle.Event.ON_RESUME -> if (isScreenVisible) exoPlayer.play()
    //             Lifecycle.Event.ON_DESTROY -> if (isScreenVisible) { /* exoPlayer.release() handled by ViewModel */ }
    //             else -> {}
    //         }
    //     }
    // })


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Player background is typically black
    ) {
        if (uiState.isLoading || uiState.mediaItem == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.error != null) {
            Text(
                text = "Error: ${uiState.error}",
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else {
            // ExoPlayer View using AndroidView
            // AndroidView(
            //     factory = { ctx ->
            //         StyledPlayerView(ctx).apply {
            //             player = exoPlayer
            //             useController = false // We use our custom controls via PlayerOverlay
            //             layoutParams = ViewGroup.LayoutParams(
            //                 ViewGroup.LayoutParams.MATCH_PARENT,
            //                 ViewGroup.LayoutParams.MATCH_PARENT
            //             )
            //         }
            //     },
            //     modifier = Modifier.fillMaxSize()
            // )
            // Placeholder for Player View:
            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
                 Text("Player View (ExoPlayer/Media3)", color = Color.White, modifier = Modifier.align(Alignment.Center))
            }


            PlayerGestures(
                onToggleControls = viewModel::toggleControlsVisibility,
                onDoubleTapSeekForward = viewModel::seekForward,
                onDoubleTapSeekBackward = viewModel::seekBackward
            ) {
                PlayerOverlay(
                    mediaItem = uiState.mediaItem,
                    isVisible = uiState.controlsVisible,
                    isPlaying = uiState.isPlaying,
                    isLocked = false, // TODO: Add isLocked to UiState and ViewModel
                    currentPositionMs = uiState.currentPositionMs,
                    totalDurationMs = uiState.totalDurationMs,
                    playbackSpeed = uiState.playbackSpeed,
                    onPlayPauseToggle = viewModel::togglePlayPause,
                    onSeekForward = { viewModel.seekForward() },
                    onSeekBackward = { viewModel.seekBackward() },
                    // onNextEpisode = { viewModel.playNextEpisode() }, // TODO
                    // onPreviousEpisode = { viewModel.playPreviousEpisode() }, // TODO
                    onPlaybackSpeedChange = viewModel::onPlaybackSpeedChange,
                    onSettingsClick = { /* TODO: Show settings dialog */ },
                    onLockToggle = { /* TODO: Implement lock toggle in ViewModel */ },
                    onNavigateBack = {
                        isScreenVisible = false // Ensure player is released if nav occurs before dispose
                        onNavigateBack()
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PlayerScreenPreview_Loading() {
    val loadingViewModel = PlayerViewModel().apply {
        // Simulate loading state if needed, though default might be loading
    }
    TomatoTheme {
        PlayerScreen(
            viewModel = loadingViewModel,
            // exoPlayer = ExoPlayer.Builder(LocalContext.current).build(), // Preview needs an instance
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerScreenPreview_Error() {
    val errorViewModel = PlayerViewModel().apply {
        // Simulate error state (PlayerViewModel needs modification to allow this for preview)
        // _uiState.value = PlayerUiState(isLoading = false, error = "Failed to load video")
    }
    TomatoTheme {
        PlayerScreen(
            viewModel = errorViewModel,
            // exoPlayer = ExoPlayer.Builder(LocalContext.current).build(),
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerScreenPreview_Playing() {
    val playingViewModel = PlayerViewModel().apply {
        // Simulate playing state (PlayerViewModel needs modification)
        // _uiState.value = PlayerUiState(
        //     mediaItem = MediaItem("id", "Big Buck Bunny", "url"),
        //     isLoading = false,
        //     isPlaying = true,
        //     controlsVisible = true
        // )
    }
    TomatoTheme {
        PlayerScreen(
            viewModel = playingViewModel,
            // exoPlayer = ExoPlayer.Builder(LocalContext.current).build(),
            onNavigateBack = {}
        )
    }
}
