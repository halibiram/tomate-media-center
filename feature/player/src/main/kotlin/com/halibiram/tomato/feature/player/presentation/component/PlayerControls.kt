package com.halibiram.tomato.feature.player.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.halibiram.tomato.core.player.exoplayer.PlayerState
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
    mediaTitle: String,
    isCastAvailable: Boolean, // To show/hide Cast button
    isCasting: Boolean, // To potentially change behavior or UI if casting
    onPlayPauseToggle: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onFullScreenToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    // onStartCasting: () -> Unit, // Cast button itself handles connection UI
    // onStopCasting: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        ) {
            Text(
                text = mediaTitle,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 64.dp, end = 64.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onSeekBackward, enabled = !isCasting /* Disable local seek if casting */) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    IconButton(onClick = onPlayPauseToggle, modifier = Modifier.size(56.dp)) {
                        Icon(
                            // For casting, icon might reflect remote player state. PlayerState should ideally reflect this.
                            imageVector = if (playerState.isPlaying /* && !isCasting */ || isCasting /* && remoteIsPlaying */) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    IconButton(onClick = onSeekForward, enabled = !isCasting /* Disable local seek if casting */) {
                        Icon(Icons.Default.FastForward, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (playerState.isLoading && !playerState.isPlaying && !isCasting) "Buffering..."
                               else if (isCasting) "Casting..." // Simple casting indicator
                               else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Row {
                        if (isCastAvailable) {
                            AndroidView(
                                factory = { context ->
                                    MediaRouteButton(context).apply {
                                        CastButtonFactory.setUpMediaRouteButton(context, this)
                                    }
                                },
                                modifier = Modifier.size(48.dp) // Standard IconButton size
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings (Subtitles, Quality, Speed)", tint = Color.White)
                        }
                        IconButton(onClick = onFullScreenToggle) {
                            Icon(
                                imageVector = if (false /* uiState.isFullScreen */) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
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
fun PlayerControlsPreview_Playing_CastAvailable() {
    TomatoTheme {
        PlayerControls(
            isVisible = true,
            playerState = PlayerState(isPlaying = true, currentPositionMs = 30000, durationMs = 120000, isLoading = false),
            mediaTitle = "Big Buck Bunny",
            isCastAvailable = true,
            isCasting = false,
            onPlayPauseToggle = {}, onSeekForward = {}, onSeekBackward = {}, onSeekTo = {},
            onFullScreenToggle = {}, onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerControlsPreview_Playing_Casting() {
    TomatoTheme {
        PlayerControls(
            isVisible = true,
            playerState = PlayerState(isPlaying = true), // This state might reflect remote player
            mediaTitle = "Casting Movie Title",
            isCastAvailable = true,
            isCasting = true,
            onPlayPauseToggle = {}, onSeekForward = {}, onSeekBackward = {}, onSeekTo = {},
            onFullScreenToggle = {}, onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlayerControlsPreview_Paused_NoCast() {
    TomatoTheme {
        PlayerControls(
            isVisible = true,
            playerState = PlayerState(isPlaying = false, currentPositionMs = 0, durationMs = 300000, isLoading = false),
            mediaTitle = "Elephants Dream",
            isCastAvailable = false,
            isCasting = false,
            onPlayPauseToggle = {}, onSeekForward = {}, onSeekBackward = {}, onSeekTo = {},
            onFullScreenToggle = {}, onSettingsClick = {}
        )
    }
}
