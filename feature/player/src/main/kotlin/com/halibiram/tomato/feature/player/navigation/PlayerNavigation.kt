package com.halibiram.tomato.feature.player.navigation

// import androidx.navigation.NavController
// import androidx.navigation.NavGraphBuilder
// import androidx.navigation.NavType
// import androidx.navigation.compose.composable
// import androidx.navigation.navArgument
// import com.halibiram.tomato.feature.player.presentation.PlayerScreen

// PlayerNavigation
const val playerRoute = "player/{mediaUrl}" // Example with a media URL argument

// fun NavGraphBuilder.playerScreen(navController: NavController) {
//    composable(
//        route = playerRoute,
//        arguments = listOf(navArgument("mediaUrl") { type = NavType.StringType; nullable = true })
//    ) { backStackEntry ->
//        PlayerScreen(mediaUrl = backStackEntry.arguments?.getString("mediaUrl"))
//    }
// }

// fun NavController.navigateToPlayer(mediaUrl: String) {
//    this.navigate("player/$mediaUrl")
// }
