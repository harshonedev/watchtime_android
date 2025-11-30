package com.app.core.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.app.auth.ui.screens.AuthScreen
import com.app.auth.ui.screens.QrScanScreen
import com.app.auth.ui.states.AuthState
import com.app.auth.ui.viewmodels.AuthViewModel
import com.app.core.home.HomeScreen
import com.app.core.ui.theme.ThemeViewModel
import com.app.media.ui.screens.MediaDetailsScreen
import com.app.media.ui.screens.SeasonsScreen
import com.app.media.ui.viewmodel.MediaDetailsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel = koinViewModel(),
) {
    val navController = rememberNavController()
    val userAuthenticated = when (authViewModel.authState.value) {
        is AuthState.Authenticated -> true
        is AuthState.Unauthenticated -> false
        else -> false // Handle other states if necessary
    }
    val startDestination = if (userAuthenticated) Screen.Home else Screen.Auth

    SharedTransitionLayout {
        NavHost(navController = navController, startDestination = startDestination) {

            composable<Screen.Home> {
                HomeScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    navigateToAuth = {
                        navController.navigate(Screen.Auth) {
                            popUpTo(Screen.Home) {
                                inclusive = true
                            }
                        }
                    },
                    navigateToQrScan = {
                        navController.navigate(Screen.QrScan)
                    },
                    themeViewModel = themeViewModel,
                    navigateToMediaDetails = { mediaId, mediaType, posterUrl, posterKey ->
                        navController.navigate(
                            Screen.MediaDetails(
                                mediaId,
                                mediaType,
                                posterUrl,
                                posterKey
                            )
                        )
                    }
                )
            }

            composable<Screen.Auth> {
                AuthScreen {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Auth) {
                            inclusive = true
                        }
                    }
                }
            }

            composable<Screen.QrScan> {
                QrScanScreen(
                    onScanComplete = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.MediaDetails> { backStackEntry ->
                val screen = backStackEntry.toRoute<Screen.MediaDetails>()
                MediaDetailsScreen(
                    viewModel = koinViewModel<MediaDetailsViewModel>(
                        parameters = { parametersOf(screen.id, screen.type) }
                    ),
                    mediaId = screen.id,
                    mediaType = screen.type,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    posterUrl = screen.posterUrl,
                    posterKey = screen.posterKey,
                    onNavigateToSeason = { posterPath, tvId, seasonNumber, seasonName, tvName ->
                        navController.navigate(
                            Screen.Season(
                                tvId,
                                seasonNumber,
                                tvName,
                                seasonName,
                                posterPath

                            )
                        )
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    })
            }

            composable<Screen.Season> { backStackEntry ->
                val screen = backStackEntry.toRoute<Screen.Season>()
                SeasonsScreen(
                    tvId = screen.tvId,
                    seasonNumber = screen.seasonNumber,
                    seasonName = screen.seasonName,
                    tvName = screen.tvName,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    posterPath = screen.posterPath,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
