package com.halibiram.tomato.feature.player.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
// import androidx.compose.runtime.LaunchedEffect // For seek feedback later
// import androidx.compose.runtime.getValue // For seek feedback later
// import androidx.compose.runtime.mutableStateOf // For seek feedback later
// import androidx.compose.runtime.remember // For seek feedback later
// import androidx.compose.runtime.setValue // For seek feedback later
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.core.player.exoplayer.PlayerState
import androidx.media3.common.Player // For Player.STATE_BUFFERING, Player.STATE_IDLE
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun PlayerOverlay(
    modifier: Modifier = Modifier,
    playerState: PlayerState,
    controlsVisible: Boolean, // To potentially hide error/loading if controls are also handling it
    onRetry: () -> Unit,
    // Add callbacks for seek feedback if implemented here
    // showSeekForwardIndicator: Boolean,
    // showSeekBackwardIndicator: Boolean,
) {
    Box(modifier = modifier.fillMaxSize()) {

        // Loading Indicator
        // Show loading if player is buffering OR if it's in IDLE/null error state AND controls are not visible
        // (assuming controls might show their own loading/initial state).
        // Or, more simply, just rely on playerState.isLoading.
        AnimatedVisibility(
            visible = playerState.isLoading && playerState.playbackState == Player.STATE_BUFFERING,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator(color = Color.White)
        }

        // Error Message
        // Show error if an error exists AND controls are not visible (or error is critical)
        playerState.error?.let { error ->
            AnimatedVisibility(
                visible = !controlsVisible, // Show error overlay only if controls are hidden
                                         // Or make it always visible: visible = true,
                                         // and ensure it's drawn under controls if they are up.
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.7f) // Don't take full width for error message box
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Playback Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = error.message ?: "An unexpected error occurred.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Retry", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }

        // Placeholder for Seek Feedback text (e.g., "+10s")
        // This would typically be managed by state variables that are briefly set to true
        // by the gesture handler, and then a LaunchedEffect here would hide them after a delay.
        // For example:
        // if (showSeekForwardIndicator) {
        //     Text(
        //         text = "+10s",
        //         style = MaterialTheme.typography.headlineSmall,
        //         color = Color.White.copy(alpha = 0.8f),
        //         modifier = Modifier.align(Alignment.CenterEnd).padding(end = 64.dp)
        //     )
        // }
        // if (showSeekBackwardIndicator) {
        //     Text(
        //         text = "-10s",
        //         style = MaterialTheme.typography.headlineSmall,
        //         color = Color.White.copy(alpha = 0.8f),
        //         modifier = Modifier.align(Alignment.CenterStart).padding(start = 64.dp)
        //     )
        // }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerOverlayPreview_Loading() {
    TomatoTheme {
        PlayerOverlay(
            playerState = PlayerState(isLoading = true, playbackState = Player.STATE_BUFFERING),
            controlsVisible = false,
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerOverlayPreview_Error() {
    TomatoTheme {
        PlayerOverlay(
            playerState = PlayerState(error = Throwable("Network connection failed. Please try again.")),
            controlsVisible = false, // Assuming controls are hidden to see overlay error
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerOverlayPreview_NoErrorOrLoading() {
    TomatoTheme {
        PlayerOverlay(
            playerState = PlayerState(isPlaying = true),
            controlsVisible = true, // Controls are up, so overlay elements (if any) should be behind or not shown
            onRetry = {}
        )
    }
}
