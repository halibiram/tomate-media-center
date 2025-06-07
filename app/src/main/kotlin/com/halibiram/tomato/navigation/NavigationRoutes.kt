package com.halibiram.tomato.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Main : Screen("main_screen")
    // Add other screen routes here
}
