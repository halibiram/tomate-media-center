package com.halibiram.tomato.feature.downloads.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.halibiram.tomato.feature.downloads.presentation.DownloadsScreen
import com.halibiram.tomato.feature.downloads.presentation.DownloadsViewModel

object DownloadsRoutes {
    const val DOWNLOADS_SCREEN_ROUTE = "downloads_screen"
    // Add other routes specific to the downloads feature if needed
}

fun NavGraphBuilder.downloadsScreen(
    navController: NavController,
    onNavigateToPlayer: (mediaId: String, mediaType: String) -> Unit
) {
    composable(route = DownloadsRoutes.DOWNLOADS_SCREEN_ROUTE) {
        // If using Hilt for ViewModel injection:
        val downloadsViewModel: DownloadsViewModel = hiltViewModel()

        // If not using Hilt, provide ViewModel differently
        // val downloadsViewModel = DownloadsViewModel() // Adjust if dependencies

        DownloadsScreen(
            viewModel = downloadsViewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToPlayer = onNavigateToPlayer
        )
    }
    // Define other composables within the downloads feature's navigation graph here
}

// Function to navigate to the downloads screen
fun NavController.navigateToDownloads() {
    this.navigate(DownloadsRoutes.DOWNLOADS_SCREEN_ROUTE) {
        // Configure navigation options (e.g., launchSingleTop) if needed
        launchSingleTop = true
    }
}
