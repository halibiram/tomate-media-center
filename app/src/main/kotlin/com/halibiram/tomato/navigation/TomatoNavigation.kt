package com.halibiram.tomato.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.halibiram.tomato.ui.screens.SplashScreenContent
import com.halibiram.tomato.ui.screens.Greeting

@Composable
fun TomatoNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            // Replace with actual Splash Screen Composable if it's different from SplashScreenContent
            SplashScreenContent()
            // Example of navigating after a delay:
            // LaunchedEffect(key1 = true) {
            //    delay(2000L) // Delay for 2 seconds
            //    navController.navigate(Screen.Main.route) {
            //        popUpTo(Screen.Splash.route) { inclusive = true }
            //    }
            // }
        }
        composable(Screen.Main.route) {
            // Replace with actual Main Screen Composable
            Greeting(name = "Main Screen")
        }
        // Add other composables for other screens here
    }
}
