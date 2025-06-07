package com.halibiram.tomato.feature.settings.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.halibiram.tomato.feature.settings.presentation.SettingsScreen
import com.halibiram.tomato.feature.settings.presentation.SettingsViewModel

object SettingsRoutes {
    const val SETTINGS_SCREEN_ROUTE = "settings_screen"
    // Example sub-routes if settings screen navigates to other specific settings pages
    const val ABOUT_SCREEN_ROUTE = "settings_about_screen"
    const val ACCOUNT_SCREEN_ROUTE = "settings_account_screen"
}

fun NavGraphBuilder.settingsScreen(
    navController: NavController
    // Add other navigation actions as parameters if SettingsScreen leads to more specific screens
    // e.g. onNavigateToAbout: () -> Unit
) {
    composable(route = SettingsRoutes.SETTINGS_SCREEN_ROUTE) {
        // If using Hilt for ViewModel injection:
        val settingsViewModel: SettingsViewModel = hiltViewModel()

        // If not using Hilt, provide ViewModel differently
        // val settingsViewModel = SettingsViewModel(/* provide dependencies */)

        SettingsScreen(
            viewModel = settingsViewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAbout = { navController.navigate(SettingsRoutes.ABOUT_SCREEN_ROUTE) },
            onNavigateToAccount = { navController.navigate(SettingsRoutes.ACCOUNT_SCREEN_ROUTE) }
        )
    }

    // Define composables for sub-screens if any, e.g.:
    // composable(route = SettingsRoutes.ABOUT_SCREEN_ROUTE) {
    //     AboutScreen(onNavigateBack = { navController.popBackStack() })
    // }
    // composable(route = SettingsRoutes.ACCOUNT_SCREEN_ROUTE) {
    //     AccountScreen(onNavigateBack = { navController.popBackStack() })
    // }
}

// Function to navigate to the main settings screen
fun NavController.navigateToSettings() {
    this.navigate(SettingsRoutes.SETTINGS_SCREEN_ROUTE) {
        launchSingleTop = true
    }
}
