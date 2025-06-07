package com.halibiram.tomato.feature.bookmarks.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.halibiram.tomato.feature.bookmarks.presentation.BookmarksScreen
import com.halibiram.tomato.feature.bookmarks.presentation.BookmarksViewModel

object BookmarksRoutes {
    const val BOOKMARKS_SCREEN_ROUTE = "bookmarks_screen"
    // Add other routes specific to the bookmarks feature if needed
}

fun NavGraphBuilder.bookmarksScreen(
    navController: NavController,
    onNavigateToDetails: (mediaId: String, mediaType: String) -> Unit
) {
    composable(route = BookmarksRoutes.BOOKMARKS_SCREEN_ROUTE) {
        // If using Hilt for ViewModel injection:
        val bookmarksViewModel: BookmarksViewModel = hiltViewModel()

        // If not using Hilt, provide ViewModel differently
        // val bookmarksViewModel = BookmarksViewModel() // Adjust if dependencies

        BookmarksScreen(
            viewModel = bookmarksViewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToDetails = onNavigateToDetails
        )
    }
    // Define other composables within the bookmarks feature's navigation graph here
}

// Function to navigate to the bookmarks screen
fun NavController.navigateToBookmarks() {
    this.navigate(BookmarksRoutes.BOOKMARKS_SCREEN_ROUTE) {
        // Configure navigation options (e.g., launchSingleTop) if needed
        launchSingleTop = true
    }
}
