package com.halibiram.tomato.feature.player.screen

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.halibiram.tomato.feature.player.viewmodel.PlayerViewModel

@Composable
fun VideoPlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
    mediaUri: Uri? // This will be used to pass actual URIs later
) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val window = activity?.window
    val view = LocalView.current

    var isFullscreen by remember { mutableStateOf(false) }

    // HLS sample stream from Apple (tested in previous conceptual step):
    // val currentTestStreamUri = mediaUri ?: Uri.parse("https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8")
    // **TEST DASH STREAM**
    val currentTestStreamUri = mediaUri ?: Uri.parse("https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd")


    val player = remember { viewModel.player }

    fun setFullscreen(fullscreen: Boolean) {
        isFullscreen = fullscreen
        if (window != null) {
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            if (fullscreen) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    fun enterPiPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) == true) {
            val aspectRatio = Rational(16, 9)
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            activity.enterPictureInPictureMode(pipParams)
        }
    }

    BackHandler(enabled = isFullscreen) {
        setFullscreen(false)
    }

    DisposableEffect(currentTestStreamUri) { // Re-prepare if the stream URI changes
        viewModel.preparePlayer(currentTestStreamUri)
        player.playWhenReady = true // Start playback automatically for testing
        onDispose {
            if (isFullscreen) {
                setFullscreen(false)
            }
            // player.stop() // Stop player when composable disposes if not handled by ViewModel's onCleared
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            // .let { if (isFullscreen) it.systemBarsPadding() else it }
    ) {
        Box(
             modifier = if (isFullscreen) Modifier.fillMaxSize() else Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (!isFullscreen) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { viewModel.play() }) {
                    Text("Play")
                }
                Button(onClick = { viewModel.pause() }) {
                    Text("Pause")
                }
                Button(onClick = { setFullscreen(!isFullscreen) }) {
                    Text(if (isFullscreen) "Exit Fullscreen" else "Fullscreen")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) == true) {
                    Button(onClick = { enterPiPMode() }) {
                        Text("PiP")
                    }
                }
            }
            Text(text = "Playing: ${currentTestStreamUri}", modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally))
        } else {
             Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Button(onClick = { setFullscreen(false) }) {
                    Text("Exit Fullscreen")
                }
            }
        }
    }
}
