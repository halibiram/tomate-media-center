package com.halibiram.tomato.feature.player.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
// import com.google.android.exoplayer2.ExoPlayer // Assuming ExoPlayer, passed from Activity/DI
import com.halibiram.tomato.feature.player.presentation.PlayerScreen
import com.halibiram.tomato.feature.player.presentation.PlayerViewModel
import androidx.hilt.navigation.compose.hiltViewModel

object PlayerRoutes {
    const val PLAYER_SCREEN_BASE_ROUTE = "player_screen"
    const val MEDIA_ID_ARG = "mediaId"
    const val MEDIA_TYPE_ARG = "mediaType" // e.g., "movie", "episode"
    // Route including arguments: player_screen/{mediaId}/{mediaType}
    const val PLAYER_SCREEN_ROUTE_WITH_ARGS =
        "$PLAYER_SCREEN_BASE_ROUTE/{$MEDIA_ID_ARG}/{$MEDIA_TYPE_ARG}"
}

// Helper class for type-safe argument passing (optional but good practice)
// class PlayerArgs(val mediaId: String, val mediaType: String) {
//     constructor(savedStateHandle: androidx.lifecycle.SavedStateHandle) :
//         this(
//             checkNotNull(savedStateHandle[PlayerRoutes.MEDIA_ID_ARG]),
//             checkNotNull(savedStateHandle[PlayerRoutes.MEDIA_TYPE_ARG])
//         )
//     companion object {
//         const val MEDIA_ID_ARG = PlayerRoutes.MEDIA_ID_ARG
//         const val MEDIA_TYPE_ARG = PlayerRoutes.MEDIA_TYPE_ARG
//     }
// }


fun NavGraphBuilder.playerScreen(
    navController: NavController
    // exoPlayer: ExoPlayer // Pass ExoPlayer if it's managed at a higher level (e.g., Activity)
) {
    composable(
        route = PlayerRoutes.PLAYER_SCREEN_ROUTE_WITH_ARGS,
        arguments = listOf(
            navArgument(PlayerRoutes.MEDIA_ID_ARG) { type = NavType.StringType },
            navArgument(PlayerRoutes.MEDIA_TYPE_ARG) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        // val mediaId = backStackEntry.arguments?.getString(PlayerRoutes.MEDIA_ID_ARG)
        // val mediaType = backStackEntry.arguments?.getString(PlayerRoutes.MEDIA_TYPE_ARG)
        // Guard against null args, though NavType.StringType should ensure they are present.

        // If using Hilt for ViewModel injection and SavedStateHandle for args:
        val playerViewModel: PlayerViewModel = hiltViewModel()
        // The ViewModel should be using SavedStateHandle to retrieve mediaId and mediaType

        PlayerScreen(
            viewModel = playerViewModel,
            // exoPlayer = exoPlayer, // Pass the shared ExoPlayer instance
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

fun NavController.navigateToPlayer(mediaId: String, mediaType: String) {
    // Ensure mediaId and mediaType are URL-encoded if they can contain special characters,
    // though typically IDs are safe.
    this.navigate("${PlayerRoutes.PLAYER_SCREEN_BASE_ROUTE}/$mediaId/$mediaType") {
        // Configure navigation options if needed
        launchSingleTop = true // Avoid multiple player instances for the same media
    }
}
