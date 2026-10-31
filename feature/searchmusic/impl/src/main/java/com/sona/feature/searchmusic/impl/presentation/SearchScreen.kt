package com.sona.feature.searchmusic.impl.presentation

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.ui.component.ArtistRow
import com.sona.core.ui.component.EmptyState
import com.sona.core.ui.component.ErrorState
import com.sona.core.ui.component.LoadingState
import com.sona.core.ui.component.PlaylistRow
import com.sona.core.ui.component.TrackRow
import com.sona.feature.searchmusic.impl.presentation.components.BrowseGenres
import com.sona.feature.searchmusic.impl.presentation.components.SearchField

@Composable
fun SearchRoute(
    viewModelFactory: ViewModelProvider.Factory,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
    onOpenPlaylist: (id: String, name: String) -> Unit,
) {
    val viewModel: SearchViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SearchEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        SearchScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onOpenTrack = onOpenTrack,
            onOpenArtist = onOpenArtist,
            onOpenPlaylist = onOpenPlaylist,
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
private fun SearchScreen(
    state: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
    onOpenPlaylist: (id: String, name: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp)) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Millions of tracks from the open Audius network.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            SearchField(
                query = state.query,
                onQueryChange = { onIntent(SearchIntent.QueryChanged(it)) },
                onClear = { onIntent(SearchIntent.ClearQuery) },
            )
        }

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        } else {
            Spacer(Modifier.height(6.dp))
        }

        if (!state.hasQuery) {
            BrowseGenres(
                modifier = Modifier.weight(1f),
                onGenreClick = { onIntent(SearchIntent.BrowseGenre(it)) },
            )
        } else {
            SearchResults(
                state = state,
                modifier = Modifier.weight(1f),
                onIntent = onIntent,
                onOpenTrack = onOpenTrack,
                onOpenArtist = onOpenArtist,
                onOpenPlaylist = onOpenPlaylist,
            )
        }
    }
}

@Composable
private fun SearchResults(
    state: SearchUiState,
    modifier: Modifier = Modifier,
    onIntent: (SearchIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
    onOpenPlaylist: (id: String, name: String) -> Unit,
) {
    Column(modifier = modifier) {
        TabRow(
            selectedTabIndex = state.selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            SearchTab.entries.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { onIntent(SearchIntent.TabSelected(tab)) },
                    text = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        when {
            state.isLoading && !state.hasResults -> LoadingState(label = "Searching Audius…")
            state.error != null && !state.hasResults -> ErrorState(
                message = state.error,
                onRetry = { onIntent(SearchIntent.Retry) },
            )
            !state.hasResults -> EmptyState(
                title = "No matches yet",
                message = "Try a different spelling, artist handle, or genre.",
            )
            else -> SearchTabContent(
                state = state,
                onIntent = onIntent,
                onOpenTrack = onOpenTrack,
                onOpenArtist = onOpenArtist,
                onOpenPlaylist = onOpenPlaylist,
            )
        }
    }
}

@Composable
private fun SearchTabContent(
    state: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
    onOpenPlaylist: (id: String, name: String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        when (state.selectedTab) {
            SearchTab.TRACKS -> items(state.tracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    isActive = state.currentTrackId == track.id,
                    isPlaying = state.isPlaying,
                    isFavorite = state.favoriteIds.contains(track.id),
                    onClick = { onOpenTrack(track.id) },
                    onPlay = { onIntent(SearchIntent.PlayTrack(track)) },
                    onFavorite = { onIntent(SearchIntent.ToggleFavorite(track)) },
                )
            }
            SearchTab.ARTISTS -> items(state.artists, key = { it.id }) { artist ->
                ArtistRow(
                    artist = artist,
                    onClick = { onOpenArtist(artist.id, artist.displayName) },
                )
            }
            SearchTab.PLAYLISTS -> items(state.playlists, key = { it.id }) { playlist ->
                PlaylistRow(
                    playlist = playlist,
                    onClick = { onOpenPlaylist(playlist.id, playlist.name) },
                )
            }
        }
    }
}
