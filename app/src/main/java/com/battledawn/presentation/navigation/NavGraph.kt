package com.battledawn.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.battledawn.presentation.ui.screens.alliance.AllianceScreen
import com.battledawn.presentation.ui.screens.battle.BattleScreen
import com.battledawn.presentation.ui.screens.colony.ColonyScreen
import com.battledawn.presentation.ui.screens.leaderboard.LeaderboardScreen
import com.battledawn.presentation.ui.screens.login.LoginScreen
import com.battledawn.presentation.ui.screens.map.MapScreen
import com.battledawn.presentation.ui.screens.research.ResearchScreen
import com.battledawn.presentation.ui.screens.settings.SettingsScreen
import com.battledawn.presentation.ui.screens.splash.SplashScreen

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Map : Screen("map")
    object Colony : Screen("colony/{colonyId}") {
        fun createRoute(colonyId: String) = "colony/$colonyId"
    }
    object Units : Screen("units")
    object Research : Screen("research")
    object Alliance : Screen("alliance")
    object Battle : Screen("battle/{battleId}") {
        fun createRoute(battleId: String) = "battle/$battleId"
    }
    object Leaderboard : Screen("leaderboard")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}

/**
 * Main navigation host for Battle Dawn
 */
@Composable
fun BattleDawnNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onNavigateToColony = { colonyId ->
                    navController.navigate(Screen.Colony.createRoute(colonyId))
                }
            )
        }

        composable(Screen.Colony.route) { backStackEntry ->
            val colonyId = backStackEntry.arguments?.getString("colonyId")
            if (colonyId != null) {
                ColonyScreen(
                    colonyId = colonyId,
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }

        composable(Screen.Research.route) {
            ResearchScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Alliance.route) {
            AllianceScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Battle.route) { backStackEntry ->
            val battleId = backStackEntry.arguments?.getString("battleId")
            if (battleId != null) {
                BattleScreen(
                    battleId = battleId,
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }

        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}
