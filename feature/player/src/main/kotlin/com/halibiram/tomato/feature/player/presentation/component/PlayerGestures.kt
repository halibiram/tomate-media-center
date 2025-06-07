package com.halibiram.tomato.feature.player.presentation.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.halibiram.tomato.ui.theme.TomatoTheme

/**
 * A composable that handles gestures for the player.
 * This typically wraps the player view and the controls overlay.
 *
 * @param onToggleControls Callback for single tap to toggle controls visibility.
 * @param onDoubleTapSeekForward Callback for double tap on the right side to seek forward.
 * @param onDoubleTapSeekBackward Callback for double tap on the left side to seek backward.
 * @param onHorizontalDrag Callback for horizontal drag to seek (optional, can be complex).
 * @param onVerticalDrag Callback for vertical drag to adjust volume/brightness (optional).
 */
@Composable
fun PlayerGestures(
    modifier: Modifier = Modifier,
    onToggleControls: () -> Unit,
    onDoubleTapSeekForward: (() -> Unit)? = null,
    onDoubleTapSeekBackward: (() -> Unit)? = null,
    // Add more gesture callbacks as needed:
    // onHorizontalDragStart: () -> Unit = {},
    // onHorizontalDragEnd: () -> Unit = {},
    // onHorizontalDragProgress: (Float) -> Unit = {}, // progress -1f to 1f
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onToggleControls()
                    },
                    onDoubleTap = { offset ->
                        // Determine if double tap is on left or right half of the screen
                        val screenWidth = size.width
                        if (offset.x > screenWidth / 2) {
                            onDoubleTapSeekForward?.invoke()
                        } else {
                            onDoubleTapSeekBackward?.invoke()
                        }
                    }
                    // onLongPress = { /* Optional: Handle long press */ }
                )
            }
            // Add other gesture detectors like detectDragGestures for seek/volume/brightness
            // .pointerInput(Unit) {
            //     detectDragGestures(
            //         onDragStart = { onHorizontalDragStart() },
            //         onDragEnd = { onHorizontalDragEnd() },
            //         onDrag = { change, dragAmount ->
            //             change.consume()
            //             // Calculate horizontal drag percentage for seeking
            //             // Or vertical drag for volume/brightness
            //         }
            //     )
            // }
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerGesturesPreview() {
    TomatoTheme {
        PlayerGestures(
            onToggleControls = { /* Log tap */ },
            onDoubleTapSeekForward = { /* Log double tap right */ },
            onDoubleTapSeekBackward = { /* Log double tap left */ }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // This would be the player view and overlay content
            }
        }
    }
}
