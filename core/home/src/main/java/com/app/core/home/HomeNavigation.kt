package com.app.core.home

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.core.ui.theme.ThemeViewModel
import com.app.discover.ui.screens.DiscoverScreen
import com.app.popular.ui.screens.PopularScreen
import com.app.profile.ui.screens.ProfileScreen
import com.collections.ui.screens.CollectionsScreen
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeNavigation(
    navController: NavHostController,
    themeViewModel: ThemeViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    navigateToAuth: () -> Unit,
    navigateToQrScan: () -> Unit = {},
    navigateToMediaDetails: (tmdbId: Int, name: String, posterUrl: String?, key: String) -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = HomeDestination.Popular,
        enterTransition = { defaultEnterForTabs() },
        exitTransition = { defaultExitForTabs() },
        popEnterTransition = { defaultEnterForTabs() },
        popExitTransition = { defaultExitForTabs() }
    ) {
        composable<HomeDestination.Popular> {
            PopularScreen(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onNavigateToMediaDetails = navigateToMediaDetails,
                onNavigateToDiscover = {
                    navController.navigate(HomeDestination.Discover) {
                        popUpTo(HomeDestination.Popular) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable<HomeDestination.Discover> {
            DiscoverScreen(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onNavigateToMediaDetails = navigateToMediaDetails,
            )
        }

        composable<HomeDestination.Collections> {
            CollectionsScreen(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onNavigateToMediaDetails = navigateToMediaDetails
            )
        }

        composable<HomeDestination.Profile> {
            ProfileScreen(
                modifier = modifier,
                themeViewModel = themeViewModel,
                onNavigateToAuth = {
                    navigateToAuth()
                },
                onNavigateToQrScan = {
                    navigateToQrScan()
                }
            )
        }
    }

}

private fun NavBackStackEntry.tabIndex(): Int = when {
    destination.hasRoute<HomeDestination.Popular>() -> HomeTabs.indexOf(HomeDestination.Popular)
    destination.hasRoute<HomeDestination.Discover>() -> HomeTabs.indexOf(HomeDestination.Discover)
    destination.hasRoute<HomeDestination.Collections>() -> HomeTabs.indexOf(HomeDestination.Collections)
    destination.hasRoute<HomeDestination.Profile>() -> HomeTabs.indexOf(HomeDestination.Profile)
    else -> Int.MAX_VALUE
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.direction(): AnimatedContentTransitionScope.SlideDirection {
    val from = initialState.tabIndex()
    val to = targetState.tabIndex()
    return if (to > from) AnimatedContentTransitionScope.SlideDirection.Left
    else AnimatedContentTransitionScope.SlideDirection.Right
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultEnterForTabs(
): EnterTransition = slideIntoContainer(
    towards = direction(),
    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
) + fadeIn(animationSpec = tween(durationMillis = 500))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultExitForTabs(
): ExitTransition = slideOutOfContainer(
    towards = direction(),
    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
) + fadeOut(animationSpec = tween(durationMillis = 500))


@Serializable
sealed class HomeDestination {
    @Serializable
    data object Popular : HomeDestination()

    @Serializable
    data object Discover : HomeDestination()

    @Serializable
    data object Collections : HomeDestination()

    @Serializable
    data object Profile : HomeDestination()
}

// A fixed order for bottom tabs (used to compute animation direction)
val HomeTabs = listOf(
    HomeDestination.Popular,
    HomeDestination.Discover,
    HomeDestination.Collections,
    HomeDestination.Profile
)

@Composable
fun DemoScreen(
    modifier: Modifier = Modifier,
    name: String = "Demo Screen", // Default parameter for demonstration purposes
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name)
    }
}