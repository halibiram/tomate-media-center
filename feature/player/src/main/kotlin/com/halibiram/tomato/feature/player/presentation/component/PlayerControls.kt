package com.halibiram.tomato.feature.player.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.ui.theme.TomatoTheme
import java.util.concurrent.TimeUnit

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    isPlaying: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    playbackSpeed: Float,
    onPlayPauseToggle: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onNextEpisode: (() -> Unit)? = null, // Nullable if not applicable (e.g., for movies)
    onPreviousEpisode: (() -> Unit)? = null, // Nullable
    onPlaybackSpeedChange: (Float) -> Unit,
    onSettingsClick: () -> Unit
) {
    if (!isVisible) {
        return // Don't render if not visible
    }

    val currentFormattedTime = formatDuration(currentPositionMs)
    val totalFormattedTime = formatDuration(totalDurationMs)

    // Dropdown menu for playback speed
    var speedMenuExpanded by remember { mutableStateOf(false) }
    val playbackSpeeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.6f)) // Semi-transparent background
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Title / Episode Info (Placeholder)
            // Text("Video Title Here", style = MaterialTheme.typography.titleMedium, color = Color.White)
            // Spacer(modifier = Modifier.height(8.dp))

            // Middle Row: Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onPreviousEpisode != null) {
                    IconButton(onClick = onPreviousEpisode) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Episode", tint = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp)) // Maintain layout balance
                }

                IconButton(onClick = onSeekBackward) {
                    Icon(Icons.Default.FastRewind, contentDescription = "Rewind 10s", tint = Color.White)
                }

                IconButton(onClick = onPlayPauseToggle, modifier = Modifier.size(64.dp)) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                IconButton(onClick = onSeekForward) {
                    Icon(Icons.Default.FastForward, contentDescription = "Forward 10s", tint = Color.White)
                }

                if (onNextEpisode != null) {
                    IconButton(onClick = onNextEpisode) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next Episode", tint = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp)) // Maintain layout balance
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row: Seek Bar, Time, Speed, Settings
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (totalDurationMs > 0) currentPositionMs.toFloat() / totalDurationMs else 0f,
                    onValueChange = { /* TODO: Implement seek bar drag: onSeek(it * totalDurationMs) */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.Gray
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$currentFormattedTime / $totalFormattedTime", style = MaterialTheme.typography.labelMedium, color = Color.White)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { speedMenuExpanded = true }) {
                            Text("${playbackSpeed}x", color = Color.White)
                        }
                        DropdownMenu(
                            expanded = speedMenuExpanded,
                            onDismissRequest = { speedMenuExpanded = false }
                        ) {
                            playbackSpeeds.forEach { speed ->
                                DropdownMenuItem(
                                    text = { Text("${speed}x") },
                                    onClick = {
                                        onPlaybackSpeedChange(speed)
                                        speedMenuExpanded = false
                                    }
                                )
                            }
                        }

                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

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

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerControlsPreview_Playing() {
    TomatoTheme {
        PlayerControls(
            isVisible = true,
            isPlaying = true,
            currentPositionMs = 30 * 1000L, // 30 seconds
            totalDurationMs = 60 * 5 * 1000L, // 5 minutes
            playbackSpeed = 1.0f,
            onPlayPauseToggle = {},
            onSeekForward = {},
            onSeekBackward = {},
            onNextEpisode = {},
            onPreviousEpisode = {},
            onPlaybackSpeedChange = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerControlsPreview_Paused_NoNextPrev() {
    TomatoTheme {
        PlayerControls(
            isVisible = true,
            isPlaying = false,
            currentPositionMs = 120 * 1000L, // 2 minutes
            totalDurationMs = 60 * 10 * 1000L, // 10 minutes
            playbackSpeed = 1.5f,
            onPlayPauseToggle = {},
            onSeekForward = {},
            onSeekBackward = {},
            onNextEpisode = null, // For movies
            onPreviousEpisode = null, // For movies
            onPlaybackSpeedChange = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerControlsPreview_NotVisible() {
    TomatoTheme {
        PlayerControls(
            isVisible = false, // Main test here
            isPlaying = true,
            currentPositionMs = 0L,
            totalDurationMs = 0L,
            playbackSpeed = 1.0f,
            onPlayPauseToggle = {},
            onSeekForward = {},
            onSeekBackward = {},
            onPlaybackSpeedChange = {},
            onSettingsClick = {}
        )
    }
}
