package com.halibiram.tomato.feature.player.presentation.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

// PlayerGestures Composable wrapper
@Composable
fun PlayerGestureBox(
    modifier: Modifier = Modifier,
    onDoubleTap: (() -> Unit)? = null,
    onTap: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    // Box(
    //    modifier = modifier.pointerInput(Unit) {
    //        detectTapGestures(
    //            onDoubleTap = { onDoubleTap?.invoke() },
    //            onTap = { onTap?.invoke() }
    //        )
    //    }
    // ) {
    //    content()
    // }
    content() // Simplified for now
}
