package com.sona.feature.navigation.impl.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sona.core.ui.component.MiniPlayer
import com.sona.core.ui.component.SonaBackground
import com.sona.core.ui.theme.SonaTheme
import com.sona.feature.detailmusic.api.DetailMusicDestination
import com.sona.feature.detailmusic.impl.presentation.DetailMusicRoute
import com.sona.feature.favorites.api.FavoritesDestination
import com.sona.feature.favorites.impl.presentation.FavoritesRoute
import com.sona.feature.home.api.HomeDestination
import com.sona.feature.home.impl.presentation.HomeRoute
import com.sona.feature.musiclist.api.MusicListDestination
import com.sona.feature.musiclist.impl.presentation.MusicListRoute
import com.sona.feature.player.api.PlayerDestination
import com.sona.feature.player.impl.presentation.PlayerIntent
import com.sona.feature.player.impl.presentation.PlayerScreen
import com.sona.feature.player.impl.presentation.PlayerViewModel
import com.sona.feature.profile.api.ProfileDestination
import com.sona.feature.profile.impl.presentation.ProfileRoute
import com.sona.feature.searchmusic.api.SearchDestination
import com.sona.feature.searchmusic.impl.presentation.SearchRoute
import com.sona.feature.settings.api.SettingsDestination
import com.sona.feature.settings.impl.presentation.SettingsRoute
import com.sona.feature.splashscreen.api.SplashDestination
import com.sona.feature.splashscreen.impl.presentation.SplashScreen
import com.sona.feature.navigation.impl.presentation.components.SonaBottomBar

private val topLevelRoutes = setOf(
    HomeDestination.ROUTE,
    SearchDestination.ROUTE,
    FavoritesDestination.ROUTE,
    ProfileDestination.ROUTE,
)

@Composable
fun SonaApp(
    viewModelFactory: ViewModelProvider.Factory,
    modifier: Modifier = Modifier,
) {
    val appViewModel: AppViewModel = viewModel(factory = viewModelFactory)
    val appState by appViewModel.state.collectAsStateWithLifecycle()

    SonaTheme(themeMode = appState.themeMode) {
        val navController = rememberNavController()
        val playerViewModel: PlayerViewModel = viewModel(factory = viewModelFactory)
        val playerState by playerViewModel.state.collectAsStateWithLifecycle()

        val currentEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentEntry?.destination?.route
        val showBottomBar = currentRoute in topLevelRoutes
        val showMiniPlayer = playerState.track != null &&
            currentRoute != PlayerDestination.ROUTE &&
            currentRoute != SplashDestination.ROUTE

        fun navigateTopLevel(route: String) {
            if (route == currentRoute) return
            navController.navigate(route) {
                popUpTo(HomeDestination.ROUTE) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        SonaBackground(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = SplashDestination.ROUTE,
                    modifier = Modifier.weight(1f),
                    enterTransition = {
                        fadeIn(animationSpec = tween(260)) +
                            slideInHorizontally(animationSpec = tween(260)) { it / 14 }
                    },
                    exitTransition = { fadeOut(animationSpec = tween(180)) },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(220)) +
                            slideInHorizontally(animationSpec = tween(220)) { -it / 14 }
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(180)) +
                            slideOutHorizontally(animationSpec = tween(220)) { it / 14 }
                    },
                ) {
                    composable(SplashDestination.ROUTE) {
                        SplashScreen(
                            onFinished = {
                                navController.navigate(HomeDestination.ROUTE) {
                                    popUpTo(SplashDestination.ROUTE) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }

                    composable(HomeDestination.ROUTE) {
                        HomeRoute(
                            viewModelFactory = viewModelFactory,
                            onOpenTrack = { trackId ->
                                navController.navigate(DetailMusicDestination.createRoute(trackId))
                            },
                            onOpenList = { source, id, title ->
                                navController.navigate(
                                    MusicListDestination.createRoute(source, id, title),
                                )
                            },
                            onOpenSearch = { navigateTopLevel(SearchDestination.ROUTE) },
                            onOpenSettings = { navController.navigate(SettingsDestination.ROUTE) },
                        )
                    }

                    composable(SearchDestination.ROUTE) {
                        SearchRoute(
                            viewModelFactory = viewModelFactory,
                            onOpenTrack = { trackId ->
                                navController.navigate(DetailMusicDestination.createRoute(trackId))
                            },
                            onOpenArtist = { id, name ->
                                navController.navigate(
                                    MusicListDestination.createRoute("artist", id, name),
                                )
                            },
                            onOpenPlaylist = { id, name ->
                                navController.navigate(
                                    MusicListDestination.createRoute("playlist", id, name),
                                )
                            },
                        )
                    }

                    composable(FavoritesDestination.ROUTE) {
                        FavoritesRoute(
                            viewModelFactory = viewModelFactory,
                            onOpenTrack = { trackId ->
                                navController.navigate(DetailMusicDestination.createRoute(trackId))
                            },
                        )
                    }

                    composable(ProfileDestination.ROUTE) {
                        ProfileRoute(
                            viewModelFactory = viewModelFactory,
                            onOpenTrack = { trackId ->
                                navController.navigate(DetailMusicDestination.createRoute(trackId))
                            },
                            onOpenSettings = { navController.navigate(SettingsDestination.ROUTE) },
                            onOpenFavorites = { navigateTopLevel(FavoritesDestination.ROUTE) },
                        )
                    }

                    composable(SettingsDestination.ROUTE) {
                        SettingsRoute(
                            viewModelFactory = viewModelFactory,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(
                        route = DetailMusicDestination.ROUTE,
                        arguments = listOf(
                            navArgument(DetailMusicDestination.ARG_TRACK_ID) {
                                type = NavType.StringType
                            },
                        ),
                    ) { entry ->
                        val trackId = entry.arguments
                            ?.getString(DetailMusicDestination.ARG_TRACK_ID)
                            .orEmpty()
                        DetailMusicRoute(
                            viewModelFactory = viewModelFactory,
                            trackId = trackId,
                            onBack = { navController.popBackStack() },
                            onOpenTrack = { id ->
                                navController.navigate(DetailMusicDestination.createRoute(id))
                            },
                            onOpenArtist = { id, name ->
                                navController.navigate(
                                    MusicListDestination.createRoute("artist", id, name),
                                )
                            },
                        )
                    }

                    composable(
                        route = MusicListDestination.ROUTE,
                        arguments = listOf(
                            navArgument(MusicListDestination.ARG_SOURCE) {
                                type = NavType.StringType
                                defaultValue = "trending"
                            },
                            navArgument(MusicListDestination.ARG_ID) {
                                type = NavType.StringType
                                defaultValue = "all"
                            },
                            navArgument(MusicListDestination.ARG_TITLE) {
                                type = NavType.StringType
                                defaultValue = "Collection"
                            },
                        ),
                    ) { entry ->
                        val arguments = entry.arguments
                        MusicListRoute(
                            viewModelFactory = viewModelFactory,
                            source = arguments?.getString(MusicListDestination.ARG_SOURCE)
                                ?: "trending",
                            id = arguments?.getString(MusicListDestination.ARG_ID) ?: "all",
                            title = arguments?.getString(MusicListDestination.ARG_TITLE)
                                ?: "Collection",
                            onBack = { navController.popBackStack() },
                            onOpenTrack = { trackId ->
                                navController.navigate(DetailMusicDestination.createRoute(trackId))
                            },
                        )
                    }

                    composable(PlayerDestination.ROUTE) {
                        PlayerScreen(
                            viewModel = playerViewModel,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }

                if (showMiniPlayer) {
                    MiniPlayer(
                        track = playerState.track,
                        isPlaying = playerState.player.isPlaying,
                        progress = playerState.player.progress,
                        onTogglePlayPause = {
                            playerViewModel.onIntent(PlayerIntent.TogglePlayPause)
                        },
                        onNext = { playerViewModel.onIntent(PlayerIntent.Next) },
                        onOpen = {
                            navController.navigate(PlayerDestination.ROUTE) {
                                launchSingleTop = true
                            }
                        },
                    )
                }

                if (showBottomBar) {
                    SonaBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route -> navigateTopLevel(route) },
                    )
                }
            }
        }
    }
}
