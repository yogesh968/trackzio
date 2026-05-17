package com.weathersnap.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    data object Weather : Screen("weather")
    data object CreateReport : Screen("create_report")
    data object Camera : Screen("camera")
    data object SavedReports : Screen("saved_reports")
}
