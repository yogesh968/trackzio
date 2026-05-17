package com.weathersnap.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weathersnap.ui.SharedWeatherViewModel
import com.weathersnap.ui.camera.CameraScreen
import com.weathersnap.ui.report.CreateReportScreen
import com.weathersnap.ui.savedreports.SavedReportsScreen
import com.weathersnap.ui.weather.WeatherScreen

@Composable
fun WeatherSnapNavHost(navController: NavHostController = rememberNavController()) {
    // Single shared VM instance for the entire nav graph
    val sharedViewModel: SharedWeatherViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Weather.route,
        enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
        exitTransition = { slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)) },
        popEnterTransition = { slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) }
    ) {
        composable(Screen.Weather.route) {
            WeatherScreen(
                sharedViewModel = sharedViewModel,
                onNavigateToCreateReport = { navController.navigate(Screen.CreateReport.route) },
                onNavigateToSavedReports = { navController.navigate(Screen.SavedReports.route) }
            )
        }
        composable(Screen.CreateReport.route) {
            CreateReportScreen(
                sharedViewModel = sharedViewModel,
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToSavedReports = {
                    navController.navigate(Screen.SavedReports.route) {
                        popUpTo(Screen.Weather.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Camera.route) {
            CameraScreen(
                sharedViewModel = sharedViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SavedReports.route) {
            SavedReportsScreen(onBack = { navController.popBackStack() })
        }
    }
}
