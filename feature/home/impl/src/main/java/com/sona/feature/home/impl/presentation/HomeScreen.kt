package com.sona.feature.home.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.common.model.Genres
import com.sona.core.ui.component.EmptyState
import com.sona.core.ui.component.ErrorState
import com.sona.core.ui.component.LoadingState
import com.sona.core.ui.component.SectionHeader
import com.sona.core.ui.component.SonaChip
import com.sona.feature.home.impl.presentation.components.FeaturedTrackCard
import com.sona.feature.home.impl.presentation.components.HomeHeader
import com.sona.feature.home.impl.presentation.components.HomeSearchPill
import com.sona.core.ui.component.TrackRow
import com.sona.core.ui.component.TrackTile
import com.sona.core.ui.component.ArtistTile

@Composable
fun HomeRoute(
    viewModelFactory: ViewModelProvider.Factory,
    onOpenTrack: (String) -> Unit,
    onOpenList: (source: String, id: String, title: String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        HomeScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onOpenTrack = onOpenTrack,
            onOpenList = onOpenList,
            onOpenSearch = onOpenSearch,
            onOpenSettings = onOpenSettings,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
        )
    }
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenList: (source: String, id: String, title: String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item("header") {
            Column {
                HomeHeader(onOpenSettings = onOpenSettings)
                Spacer(Modifier.height(22.dp))
                Text(
                    text = "Find your\nnext obsession",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(18.dp))
                HomeSearchPill(onClick = onOpenSearch)
            }
        }

        item("genres") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item("all") {
                    SonaChip(
                        text = "All",
                        selected = state.selectedGenre == null,
                        onClick = { onIntent(HomeIntent.SelectGenre(null)) },
                    )
                }
                items(Genres.all, key = { it.name }) { genre ->
                    SonaChip(
                        text = genre.name,
                        selected = state.selectedGenre == genre.name,
                        onClick = { onIntent(HomeIntent.SelectGenre(genre.name)) },
                    )
                }
            }
        }

        val featured = state.featured
        if (featured != null) {
            item("featured") {
                FeaturedTrackCard(
                    track = featured,
                    isPlaying = state.currentTrackId == featured.id && state.isPlaying,
                    onClick = { onOpenTrack(featured.id) },
                    onPlay = { onIntent(HomeIntent.PlayFeatured) },
                )
            }
        }

        if (state.isLoading && state.tracks.isEmpty()) {
            item("loading") { LoadingState() }
        } else if (state.error != null && state.tracks.isEmpty()) {
            item("error") {
                ErrorState(message = state.error.orEmpty(), onRetry = { onIntent(HomeIntent.Retry) })
            }
        } else if (state.tracks.isEmpty()) {
            item("empty") {
                EmptyState(
                    title = "Nothing on the dial",
                    message = "Try another genre or refresh the feed.",
                    actionText = "Refresh",
                    onAction = { onIntent(HomeIntent.Retry) },
                )
            }
        }

        if (state.trending.isNotEmpty()) {
            item("trending-header") {
                SectionHeader(
                    title = if (state.selectedGenre == null) "Trending now" else "${state.selectedGenre} heat",
                    action = "See all",
                    onAction = {
                        val genre = state.selectedGenre
                        if (genre == null) {
                            onOpenList("trending", "all", "Trending now")
                        } else {
                            onOpenList("genre", genre, genre)
                        }
                    },
                )
            }
            items(state.trending.take(8), key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    isActive = state.currentTrackId == track.id,
                    isPlaying = state.isPlaying,
                    isFavorite = state.favoriteIds.contains(track.id),
                    onClick = { onOpenTrack(track.id) },
                    onPlay = { onIntent(HomeIntent.PlayTrack(track)) },
                    onFavorite = { onIntent(HomeIntent.ToggleFavorite(track)) },
                )
            }
        }

        if (state.freshDrops.isNotEmpty()) {
            item("fresh-header") {
                SectionHeader(title = "Fresh drops", action = "Explore") {
                    onOpenList("trending", "all", "Fresh drops")
                }
            }
            item("fresh-list") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.freshDrops, key = { it.id }) { track ->
                        TrackTile(
                            track = track,
                            isActive = state.currentTrackId == track.id,
                            isPlaying = state.isPlaying,
                            onClick = { onOpenTrack(track.id) },
                            onPlay = { onIntent(HomeIntent.PlayTrack(track)) },
                        )
                    }
                }
            }
        }

        if (state.artists.isNotEmpty()) {
            item("artists-header") {
                SectionHeader(title = "Artists to watch")
            }
            item("artists-list") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.artists, key = { it.id }) { artist ->
                        ArtistTile(
                            artist = artist,
                            onClick = { onOpenList("artist", artist.id, artist.displayName) },
                        )
                    }
                }
            }
        }

        item("footer") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Powered by the Audius open network",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

