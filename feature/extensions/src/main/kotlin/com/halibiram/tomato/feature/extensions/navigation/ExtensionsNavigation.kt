package com.halibiram.tomato.feature.extensions.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.halibiram.tomato.feature.extensions.presentation.ExtensionsScreen
import com.halibiram.tomato.feature.extensions.presentation.ExtensionsViewModel

object ExtensionsRoutes {
    const val EXTENSIONS_LIST_SCREEN_ROUTE = "extensions_list_screen"
    const val ADD_EXTENSION_SCREEN_ROUTE = "add_extension_screen" // Example for adding from file/URL
    const val EXTENSION_DETAILS_ROUTE_BASE = "extension_details"
    const val EXTENSION_ID_ARG = "extensionId"
    const val EXTENSION_DETAILS_SCREEN_ROUTE = "$EXTENSION_DETAILS_ROUTE_BASE/{$EXTENSION_ID_ARG}"
}

fun NavGraphBuilder.extensionsGraph(navController: NavController) {
    composable(route = ExtensionsRoutes.EXTENSIONS_LIST_SCREEN_ROUTE) {
        val extensionsViewModel: ExtensionsViewModel = hiltViewModel()
        ExtensionsScreen(
            viewModel = extensionsViewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAddExtension = { navController.navigate(ExtensionsRoutes.ADD_EXTENSION_SCREEN_ROUTE) },
            onNavigateToExtensionDetails = { extensionId ->
                navController.navigate("${ExtensionsRoutes.EXTENSION_DETAILS_ROUTE_BASE}/$extensionId")
            }
        )
    }

    composable(route = ExtensionsRoutes.ADD_EXTENSION_SCREEN_ROUTE) {
        // Placeholder for AddExtensionScreen
        // AddExtensionScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(
        route = ExtensionsRoutes.EXTENSION_DETAILS_SCREEN_ROUTE,
        arguments = listOf(navArgument(ExtensionsRoutes.EXTENSION_ID_ARG) { type = NavType.StringType })
    ) { backStackEntry ->
        val extensionId = backStackEntry.arguments?.getString(ExtensionsRoutes.EXTENSION_ID_ARG)
        // Placeholder for ExtensionDetailsScreen
        // ExtensionDetailsScreen(
        //     extensionId = extensionId,
        //     onNavigateBack = { navController.popBackStack() }
        // )
    }
}

// Function to navigate to the main extensions management screen
fun NavController.navigateToExtensions() {
    this.navigate(ExtensionsRoutes.EXTENSIONS_LIST_SCREEN_ROUTE) {
        launchSingleTop = true
    }
}
