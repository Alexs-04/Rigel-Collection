package com.korebit.rigel.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.korebit.rigel.data.api.RetrofitClient
import com.korebit.rigel.screens.DashboardScreen
import com.korebit.rigel.screens.LoginScreen

sealed class Route(val route: String) {
    data object Login : Route("login")
    data object Dashboard : Route("dashboard")
}

@Composable
fun RigelNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Login.route
    ) {
        composable(Route.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Dashboard.route) {
            DashboardScreen(
                username = "Usuario",
                onLogout = {
                    // Clean token before navigating back to login
                    RetrofitClient.clearAuthToken()
                    navController.navigate(Route.Login.route) {
                        popUpTo(Route.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateTo = {
                    // Future navigation
                }
            )
        }
    }
}





