package com.halibiram.tomato.feature.player.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.feature.player.presentation.MediaItem // Assuming MediaItem data class exists
import com.halibiram.tomato.ui.theme.TomatoTheme

/**
 * Overlay for the player, containing controls, title, back button, etc.
 * Its visibility is typically toggled by gestures.
 *
 * @param mediaItem Information about the content being played.
 * @param isVisible Whether the overlay (and controls) should be visible.
 * @param isPlaying Current playback state.
 * @param isLocked Whether the player controls are locked (to prevent accidental touches).
 * @param currentPositionMs Current playback position.
 * @param totalDurationMs Total duration of the media.
 * @param playbackSpeed Current playback speed.
 * @param onPlayPauseToggle Callback to toggle play/pause.
 * @param onSeekForward Callback to seek forward.
 * @param onSeekBackward Callback to seek backward.
 * @param onNextEpisode Callback for next episode button (if applicable).
 * @param onPreviousEpisode Callback for previous episode button (if applicable).
 * @param onPlaybackSpeedChange Callback when playback speed is changed.
 * @param onSettingsClick Callback when settings (e.g., quality, subtitles) are clicked.
 * @param onLockToggle Callback to toggle the lock state of controls.
 * @param onNavigateBack Callback for the back button.
 */
@Composable
fun PlayerOverlay(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem?,
    isVisible: Boolean,
    isPlaying: Boolean,
    isLocked: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    playbackSpeed: Float,
    onPlayPauseToggle: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onNextEpisode: (() -> Unit)? = null,
    onPreviousEpisode: (() -> Unit)? = null,
    onPlaybackSpeedChange: (Float) -> Unit,
    onSettingsClick: () -> Unit,
    onLockToggle: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Animate visibility of the entire overlay
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Top Bar: Back Button, Title, Lock Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    mediaItem?.let {
                        Text(it.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1)
                        if (it.seriesTitle != null && it.episodeTitle != null) {
                            Text(
                                "${it.seriesTitle} - ${it.episodeTitle}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                    }
                }
                IconButton(onClick = onLockToggle) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (isLocked) "Unlock Controls" else "Lock Controls",
                        tint = Color.White
                    )
                }
            }

            // Center: Buffering indicator (placeholder)
            // if (isBuffering) {
            //     CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            // }

            // Bottom: Player Controls (only if not locked)
            if (!isLocked) {
                PlayerControls(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    isVisible = true, // Controls visibility is part of PlayerControls itself
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    totalDurationMs = totalDurationMs,
                    playbackSpeed = playbackSpeed,
                    onPlayPauseToggle = onPlayPauseToggle,
                    onSeekForward = onSeekForward,
                    onSeekBackward = onSeekBackward,
                    onNextEpisode = onNextEpisode,
                    onPreviousEpisode = onPreviousEpisode,
                    onPlaybackSpeedChange = onPlaybackSpeedChange,
                    onSettingsClick = onSettingsClick
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
fun PlayerOverlayPreview_Visible_Playing_Unlocked() {
    val sampleMediaItem = MediaItem("1", "Big Buck Bunny", "url", seriesTitle = "Short Films", episodeTitle = "The Bunny Movie")
    TomatoTheme {
        PlayerOverlay(
            mediaItem = sampleMediaItem,
            isVisible = true,
            isPlaying = true,
            isLocked = false,
            currentPositionMs = 60000, // 1 min
            totalDurationMs = 600000, // 10 min
            playbackSpeed = 1.0f,
            onPlayPauseToggle = {},
            onSeekForward = {},
            onSeekBackward = {},
            onNextEpisode = {},
            onPlaybackSpeedChange = {},
            onSettingsClick = {},
            onLockToggle = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
fun PlayerOverlayPreview_Visible_Paused_Locked() {
    val sampleMediaItem = MediaItem("2", "Another Movie Title", "url")
    TomatoTheme {
        PlayerOverlay(
            mediaItem = sampleMediaItem,
            isVisible = true,
            isPlaying = false,
            isLocked = true, // Controls should be hidden by PlayerControls logic, lock icon visible
            currentPositionMs = 120000, // 2 min
            totalDurationMs = 3600000, // 1 hour
            playbackSpeed = 1.25f,
            onPlayPauseToggle = {},
            onSeekForward = {},
            onSeekBackward = {},
            onPlaybackSpeedChange = {},
            onSettingsClick = {},
            onLockToggle = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
fun PlayerOverlayPreview_NotVisible() {
    TomatoTheme {
        PlayerOverlay(
            mediaItem = null,
            isVisible = false, // Main test here
            isPlaying = true,
            isLocked = false,
            currentPositionMs = 0,
            totalDurationMs = 0,
            playbackSpeed = 1.0f,
            onPlayPauseToggle = {},
            onSeekForward = {},
            onSeekBackward = {},
            onPlaybackSpeedChange = {},
            onSettingsClick = {},
            onLockToggle = {},
            onNavigateBack = {}
        )
    }
}
