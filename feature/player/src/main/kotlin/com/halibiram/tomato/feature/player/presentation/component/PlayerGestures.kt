package com.halibiram.tomato.feature.player.presentation.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope // Required for content lambda
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
// import androidx.compose.runtime.rememberCoroutineScope // Not strictly needed in this version
// import kotlinx.coroutines.Job // Not strictly needed in this version
// import kotlinx.coroutines.delay // Not strictly needed in this version
// import kotlinx.coroutines.launch // Not strictly needed in this version
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

// private const val TAP_TIMEOUT_MS = 200L // Not used in this simplified version

@Composable
fun PlayerGestures(
    modifier: Modifier = Modifier,
    onToggleControls: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    // Optional: Callbacks for visual feedback if desired
    // onShowSeekForwardIndicator: () -> Unit,
    // onShowSeekBackwardIndicator: () -> Unit,
    content: @Composable BoxScope.() -> Unit // Allow content to be placed inside, like the PlayerView
) {
    // val scope = rememberCoroutineScope() // Needed if launching coroutines for feedback
    // var tapJob: Job? = null // Needed for more complex single/double tap distinction

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) { // Key is Unit so this doesn't restart on recompositions from other changes
                detectTapGestures(
                    onTap = {
                        // Default detectTapGestures: If onDoubleTap is provided, onTap is only called
                        // if it's determined that the tap is not part of a double-tap sequence.
                        // So, this should work correctly for toggling controls on a clear single tap.
                        onToggleControls()
                    },
                    onDoubleTap = { offset ->
                        // tapJob?.cancel() // Not needed with standard detectTapGestures behavior for onDoubleTap
                        if (size.width == 0) return@detectTapGestures // Avoid division by zero if size not ready
                        if (offset.x > size.width / 2) {
                            onSeekForward()
                            // scope.launch { onShowSeekForwardIndicator() } // Optional feedback
                        } else {
                            onSeekBackward()
                            // scope.launch { onShowSeekBackwardIndicator() } // Optional feedback
                        }
                    }
                )
            }
    ) {
        content() // The content (e.g., PlayerView) will be overlaid with these gestures
    }
}
