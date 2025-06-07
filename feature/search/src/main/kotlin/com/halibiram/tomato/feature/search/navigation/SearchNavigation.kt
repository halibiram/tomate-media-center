package com.halibiram.tomato.feature.search.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.halibiram.tomato.feature.search.presentation.SearchScreen
import com.halibiram.tomato.feature.search.presentation.SearchViewModel

object SearchRoutes {
    const val SEARCH_SCREEN_ROUTE = "search_screen"
    // Add other routes specific to the search feature if needed
}

fun NavGraphBuilder.searchScreen(
    navController: NavHostController,
    onNavigateToDetails: (mediaId: String, mediaType: String) -> Unit
) {
    composable(route = SearchRoutes.SEARCH_SCREEN_ROUTE) {
        // If using Hilt for ViewModel injection:
        val searchViewModel: SearchViewModel = hiltViewModel()

        // If not using Hilt, provide ViewModel differently
        // val searchViewModel = SearchViewModel() // Adjust if dependencies

        SearchScreen(
            viewModel = searchViewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToDetails = onNavigateToDetails
        )
    }
    // Define other composables within the search feature's navigation graph here
}

// Function to navigate to the search screen
fun NavHostController.navigateToSearch() {
    this.navigate(SearchRoutes.SEARCH_SCREEN_ROUTE) {
        // Configure navigation options (e.g., launchSingleTop) if needed
        launchSingleTop = true
    }
}
