package com.halibiram.tomato.feature.player.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import all filled icons for convenience
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.core.player.exoplayer.PlayerState // Core PlayerState
import com.halibiram.tomato.ui.theme.TomatoTheme
import java.util.concurrent.TimeUnit

fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    playerState: PlayerState,
    mediaTitle: String, // Added for displaying title
    onPlayPauseToggle: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekTo: (Long) -> Unit, // For slider interaction
    onFullScreenToggle: () -> Unit,
    onSettingsClick: () -> Unit, // For subtitles, quality, speed
    // Add more specific callbacks if needed, e.g., for next/prev episode
    // onNextEpisode: (() -> Unit)? = null,
    // onPreviousEpisode: (() -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize() // Takes full size to overlay correctly
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)) // Semi-transparent background for the whole controls area
        ) {
            // Top Controls (e.g., Title, Back Button - if not handled by PlayerOverlay)
            // For this component, let's assume a top bar part of PlayerScreen itself or PlayerOverlay handles title/back.
            // We can add a simple title display here if needed.
            Text(
                text = mediaTitle,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 64.dp, end = 64.dp) // Padding to avoid overlapping with potential system icons or back buttons
            )


            // Main Playback Controls (Centered or Bottom)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Seek Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatDuration(playerState.currentPositionMs),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Slider(
                        value = if (playerState.durationMs > 0) playerState.currentPositionMs.toFloat() / playerState.durationMs else 0f,
                        onValueChange = { sliderValue ->
                            if (playerState.durationMs > 0) {
                                onSeekTo((sliderValue * playerState.durationMs).toLong())
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = formatDuration(playerState.durationMs),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placeholder for Previous Track/Episode if applicable
                    // IconButton(onClick = { /* onPreviousEpisode?.invoke() */ }) {
                    //     Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                    // }

                    IconButton(onClick = onSeekBackward) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    IconButton(onClick = onPlayPauseToggle, modifier = Modifier.size(56.dp)) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize() // Make icon fill the IconButton
                        )
                    }

                    IconButton(onClick = onSeekForward) {
                        Icon(Icons.Default.FastForward, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    // Placeholder for Next Track/Episode
                    // IconButton(onClick = { /* onNextEpisode?.invoke() */ }) {
                    //     Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                    // }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Row for Fullscreen and Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placeholder for current playback speed display or other info
                    Text(
                        text = if (playerState.isLoading && !playerState.isPlaying) "Buffering..." else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Row {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings (Subtitles, Quality, Speed)", tint = Color.White)
                        }
                        IconButton(onClick = onFullScreenToggle) {
                            Icon(
                                imageVector = if (false /* Replace with actual isFullScreen state from ViewModel */) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Toggle Fullscreen",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerControlsPreview_Playing() {
    TomatoTheme {
        PlayerControls(
            isVisible = true,
            playerState = PlayerState(isPlaying = true, currentPositionMs = 30000, durationMs = 120000, isLoading = false),
            mediaTitle = "Big Buck Bunny",
            onPlayPauseToggle = {}, onSeekForward = {}, onSeekBackward = {}, onSeekTo = {},
            onFullScreenToggle = {}, onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerControlsPreview_Paused() {
    TomatoTheme {
        PlayerControls(
            isVisible = true,
            playerState = PlayerState(isPlaying = false, currentPositionMs = 0, durationMs = 300000, isLoading = false),
            mediaTitle = "Elephants Dream",
            onPlayPauseToggle = {}, onSeekForward = {}, onSeekBackward = {}, onSeekTo = {},
            onFullScreenToggle = {}, onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerControlsPreview_Buffering() {
    TomatoTheme {
        PlayerControls(
            isVisible = true,
            playerState = PlayerState(isPlaying = false, currentPositionMs = 10000, durationMs = 240000, isLoading = true),
            mediaTitle = "Sintel",
            onPlayPauseToggle = {}, onSeekForward = {}, onSeekBackward = {}, onSeekTo = {},
            onFullScreenToggle = {}, onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerControlsPreview_NotVisible() {
    TomatoTheme {
         PlayerControls(
            isVisible = false, // Main test here
            playerState = PlayerState(isPlaying = true),
            mediaTitle = "Should not be visible",
            onPlayPauseToggle = {}, onSeekForward = {}, onSeekBackward = {}, onSeekTo = {},
            onFullScreenToggle = {}, onSettingsClick = {}
        )
    }
}
