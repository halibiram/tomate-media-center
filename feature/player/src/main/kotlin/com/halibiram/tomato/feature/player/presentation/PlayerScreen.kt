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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.ui.PlayerView
import com.halibiram.tomato.core.player.exoplayer.PlayerState
import com.halibiram.tomato.feature.player.presentation.component.PlayerControls
import com.halibiram.tomato.feature.player.presentation.component.PlayerGestures
import com.halibiram.tomato.feature.player.presentation.component.PlayerOverlay
import com.halibiram.tomato.ui.theme.TomatoTheme

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// Helper function to get a displayable name for a track
fun getTrackDisplayName(format: Format, index: Int): String {
    val language = format.language?.let { java.util.Locale(it).displayLanguage } ?: "Unknown"
    val label = format.label ?: "Track $index"
    return if (format.label != null) "$label ($language)" else "$language - $label"
}


@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val corePlayerState by viewModel.corePlayerState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context.findActivity()
        activity?.let {
            val originalSystemUiVisibility = it.window.decorView.systemUiVisibility
            val originalLayoutParamsFlags = it.window.attributes.flags
            val originalOrientation = it.requestedOrientation

            it.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
            it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        onDispose {
            activity?.let {
                it.window.decorView.systemUiVisibility = originalSystemUiVisibility
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                it.requestedOrientation = originalOrientation
            }
        }
    }

    BackHandler { onNavigateBack() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.pause()
                Lifecycle.Event.ON_RESUME -> viewModel.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        PlayerGestures(
            onToggleControls = { viewModel.toggleControlsVisibility() },
            onSeekForward = { viewModel.seekForward() },
            onSeekBackward = { viewModel.seekBackward() }
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.getExoPlayerInstance()
                        useController = false
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            PlayerControls(
                isVisible = uiState.controlsVisible,
                playerState = corePlayerState,
                mediaTitle = uiState.mediaTitle,
                isCastAvailable = uiState.isCastAvailable, // Pass cast state
                isCasting = uiState.isCasting,       // Pass casting status
                onPlayPauseToggle = { viewModel.togglePlayPause() },
                onSeekForward = { viewModel.seekForward() },
                onSeekBackward = { viewModel.seekBackward() },
                onSeekTo = { newPositionMs -> viewModel.seekTo(newPositionMs) },
                onFullScreenToggle = { viewModel.toggleFullScreen() },
                onSettingsClick = { viewModel.openTrackSelectionDialog() }, // Open dialog
                modifier = Modifier.fillMaxSize()
            )
        }

        PlayerOverlay(
            playerState = corePlayerState,
            controlsVisible = uiState.controlsVisible,
            onRetry = { viewModel.retryPlayback() },
            modifier = Modifier.fillMaxSize()
        )

        if (uiState.showTrackSelectionDialog) {
            TrackSelectionDialog(
                playerState = corePlayerState,
                onDismissRequest = { viewModel.closeTrackSelectionDialog() },
                onSubtitleSelected = { group, index -> viewModel.selectSubtitleTrack(group, index) },
                onAudioSelected = { group, index -> viewModel.selectAudioTrack(group, index) },
                onDisableSubtitles = { viewModel.disableSubtitles() }
            )
        }
    }
}

@Composable
fun TrackSelectionDialog(
    playerState: PlayerState,
    onDismissRequest: () -> Unit,
    onSubtitleSelected: (TrackGroup, Int) -> Unit,
    onAudioSelected: (TrackGroup, Int) -> Unit,
    onDisableSubtitles: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = LocalContext.current.resources.displayMetrics.heightPixels.dp * 0.8f )) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Track Selection", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

                // Subtitles Section
                Text("Subtitles", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                if (playerState.availableSubtitleTracks.isEmpty()) {
                    Text("No subtitle tracks available.", fontStyle = FontStyle.Italic, modifier = Modifier.padding(bottom = 8.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) { // Limit height for scrolling
                        itemsIndexed(playerState.availableSubtitleTracks) { groupIndex, trackGroup ->
                            for (i in 0 until trackGroup.length) {
                                val format = trackGroup.getFormat(i)
                                val isSelected = playerState.trackSelectionParameters.overrides[trackGroup]?.trackIndices?.contains(i) == true &&
                                                 !playerState.trackSelectionParameters.isTrackSelectionDisabled(trackGroup.type, trackGroup)


                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { onSubtitleSelected(trackGroup, i) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSelected, onClick = { onSubtitleSelected(trackGroup, i) })
                                    Text(getTrackDisplayName(format, i), modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                        // "Disable Subtitles" option
                        item {
                             val noSubtitlesSelected = playerState.trackSelectionParameters.isTrackSelectionDisabled(C.TRACK_TYPE_TEXT, playerState.availableSubtitleTracks.firstOrNull()?.getFormat(0)?.let { playerState.availableSubtitleTracks.first() } ) ||
                                                     playerState.trackSelectionParameters.overrides.none { it.key.type == C.TRACK_TYPE_TEXT }


                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onDisableSubtitles() }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = noSubtitlesSelected, onClick = { onDisableSubtitles() })
                                Text("Disable Subtitles", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Audio Tracks Section
                Text("Audio", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                 if (playerState.availableAudioTracks.isEmpty()) {
                    Text("No audio tracks available.", fontStyle = FontStyle.Italic, modifier = Modifier.padding(bottom = 8.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) { // Limit height
                        itemsIndexed(playerState.availableAudioTracks) { groupIndex, trackGroup ->
                             for (i in 0 until trackGroup.length) {
                                val format = trackGroup.getFormat(i)
                                val isSelected = playerState.trackSelectionParameters.overrides[trackGroup]?.trackIndices?.contains(i) == true &&
                                                 !playerState.trackSelectionParameters.isTrackSelectionDisabled(trackGroup.type, trackGroup)


                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { onAudioSelected(trackGroup, i) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSelected, onClick = { onAudioSelected(trackGroup, i) })
                                    Text(getTrackDisplayName(format, i), modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismissRequest, modifier = Modifier.align(Alignment.End)) {
                    Text("Close")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PlayerScreenPreview() {
    TomatoTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            androidx.compose.material3.Text("Player Screen Preview (ExoPlayer view not available)", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
fun TrackSelectionDialogPreview() {
    val format1 = Format.Builder().setId("sub1").setLabel("English").setLanguage("en").setSampleMimeType(androidx.media3.common.MimeTypes.TEXT_VTT).build()
    val format2 = Format.Builder().setId("sub2").setLabel("Spanish").setLanguage("es").setSampleMimeType(androidx.media3.common.MimeTypes.TEXT_VTT).build()
    val subtitleGroup = TrackGroup(format1, format2)

    val audioFormat1 = Format.Builder().setId("aud1").setLabel("Stereo").setLanguage("en").setSampleMimeType(androidx.media3.common.MimeTypes.AUDIO_AAC).build()
    val audioGroup = TrackGroup(audioFormat1)

    val params = TrackSelectionParameters.Builder().build()
        // .setOverrideForType(TrackSelectionOverride(subtitleGroup, listOf(0))).build() // Select English

    TomatoTheme(darkTheme = true) {
        TrackSelectionDialog(
            playerState = PlayerState(
                availableSubtitleTracks = listOf(subtitleGroup),
                availableAudioTracks = listOf(audioGroup),
                trackSelectionParameters = params
            ),
            onDismissRequest = {},
            onSubtitleSelected = {_,_ ->},
            onAudioSelected = {_,_ ->},
            onDisableSubtitles = {}
        )
    }
}
