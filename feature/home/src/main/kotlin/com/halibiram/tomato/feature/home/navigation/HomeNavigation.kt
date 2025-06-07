package com.halibiram.tomato.feature.home.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.halibiram.tomato.feature.home.presentation.HomeScreen
import com.halibiram.tomato.feature.home.presentation.HomeViewModel

// Define unique route names for the home feature
object HomeRoutes {
    const val HOME_SCREEN_ROUTE = "home_screen"
    // Add other specific routes within the home feature if any
    // e.g., const val FEATURED_LIST_ROUTE = "home/featured_all"
}

// Extension function on NavGraphBuilder to encapsulate Home feature navigation
fun NavGraphBuilder.homeScreen(
    navController: NavHostController,
    // Define navigation actions for interactions within the home screen
    onNavigateToDetails: (mediaId: String, mediaType: String) -> Unit,
    onNavigateToCategoryList: (categoryId: String) -> Unit
    // Add other navigation callbacks as needed, e.g.,
    // onNavigateToSearch: () -> Unit
) {
    composable(route = HomeRoutes.HOME_SCREEN_ROUTE) {
        // If using Hilt for ViewModel injection:
        val homeViewModel: HomeViewModel = hiltViewModel()

        // If not using Hilt, ViewModel would be provided differently (e.g., factory, or passed directly)
        // For preview or non-Hilt, ensure HomeViewModel can be instantiated without Hilt.
        // val homeViewModel = HomeViewModel() // Simple instantiation (adjust if it has dependencies)

        HomeScreen(
            viewModel = homeViewModel,
            onNavigateToDetails = onNavigateToDetails,
            onNavigateToCategoryList = onNavigateToCategoryList
        )
    }

    // Add other composables for the home feature here, e.g.:
    // composable(route = HomeRoutes.FEATURED_LIST_ROUTE) {
    //     FeaturedListScreen(onNavigateBack = { navController.popBackStack() }, /* ... */)
    // }
}

// Optional: Functions to navigate to specific home feature screens
fun NavHostController.navigateToHome() {
    this.navigate(HomeRoutes.HOME_SCREEN_ROUTE) {
        // Configure navigation options if needed (e.g., popUpTo, launchSingleTop)
        // launchSingleTop = true
        // popUpTo(graph.startDestinationId) { saveState = true }
    }
}

// fun NavHostController.navigateToFeaturedList() {
//    this.navigate(HomeRoutes.FEATURED_LIST_ROUTE)
// }
