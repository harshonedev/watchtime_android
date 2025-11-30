package com.app.watchtime.tv.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.auth.domain.repository.AuthRepository
import com.app.auth.tvui.screens.TvAuthScreen
import com.app.watchtime.tv.screens.TvHomeScreen
import org.koin.compose.koinInject

sealed class TvScreen(val route: String) {
    object Auth : TvScreen("tv_auth")
    object Home : TvScreen("tv_home")
}

@Composable
fun TvAppNavigation() {
    val navController = rememberNavController()
    val authRepository: AuthRepository = koinInject()

    // Check authentication status and set initial route
    val startDestination = if (authRepository.isTvAuthenticated()) {
        TvScreen.Home.route
    } else {
        TvScreen.Auth.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(TvScreen.Auth.route) {
            TvAuthScreen(
                onAuthSuccess = {
                    navController.navigate(TvScreen.Home.route) {
                        popUpTo(TvScreen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(TvScreen.Home.route) {
            TvHomeScreen(
                onSignOut = {
                    navController.navigate(TvScreen.Auth.route) {
                        popUpTo(TvScreen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

