package com.r3d1.interval.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.r3d1.interval.ui.main.HomeScreen
import com.r3d1.interval.ui.main.TimerScreen
import com.r3d1.interval.ui.profiles.ProfileEditorScreen
import com.r3d1.interval.ui.settings.SettingsScreen
import com.r3d1.interval.ui.statistics.StatisticsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Timer : Screen("timer/{profileId}") {
        fun createRoute(profileId: Long) = "timer/$profileId"
    }
    data object ProfileEditor : Screen("profile_editor?profileId={profileId}") {
        fun createRoute(profileId: Long? = null) =
            if (profileId != null) "profile_editor?profileId=$profileId" else "profile_editor"
    }
    data object Settings : Screen("settings")
    data object Statistics : Screen("statistics")
}

@Composable
fun IntervalNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onProfileClick = { profileId ->
                    navController.navigate(Screen.Timer.createRoute(profileId))
                },
                onAddProfileClick = {
                    navController.navigate(Screen.ProfileEditor.createRoute())
                },
                onEditProfileClick = { profileId ->
                    navController.navigate(Screen.ProfileEditor.createRoute(profileId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onStatisticsClick = {
                    navController.navigate(Screen.Statistics.route)
                }
            )
        }

        composable(
            route = Screen.Timer.route,
            arguments = listOf(
                navArgument("profileId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: return@composable
            TimerScreen(
                profileId = profileId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ProfileEditor.route,
            arguments = listOf(
                navArgument("profileId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val profileIdStr = backStackEntry.arguments?.getString("profileId")
            val profileId = profileIdStr?.toLongOrNull()
            ProfileEditorScreen(
                profileId = profileId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
