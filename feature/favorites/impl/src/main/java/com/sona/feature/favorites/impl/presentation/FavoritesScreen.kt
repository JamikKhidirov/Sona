package com.sona.feature.favorites.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.LinearProgressIndicator
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
import com.sona.core.ui.component.EmptyState
import com.sona.feature.favorites.impl.presentation.components.FavoritesHero
import com.sona.core.ui.component.TrackRow

@Composable
fun FavoritesRoute(
    viewModelFactory: ViewModelProvider.Factory,
    onOpenTrack: (String) -> Unit,
) {
    val viewModel: FavoritesViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is FavoritesEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        FavoritesScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onOpenTrack = onOpenTrack,
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
private fun FavoritesScreen(
    state: FavoritesUiState,
    onIntent: (FavoritesIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item("hero") {
            FavoritesHero(
                count = state.tracks.size,
                totalDuration = state.totalDurationSeconds,
                onPlayAll = { onIntent(FavoritesIntent.PlayAll) },
                enabled = state.tracks.isNotEmpty(),
            )
        }

        if (state.isLoading) {
            item("loading") {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        } else if (state.tracks.isEmpty()) {
            item("empty") {
                EmptyState(
                    title = "Your heart is open",
                    message = "Tap the heart on any track and it will show up here.",
                    icon = Icons.Rounded.FavoriteBorder,
                )
            }
        } else {
            item("list-header") {
                Text(
                    text = "Liked tracks",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 22.dp, bottom = 8.dp),
                )
            }
            items(state.tracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    isActive = state.currentTrackId == track.id,
                    isPlaying = state.isPlaying,
                    isFavorite = true,
                    onClick = { onOpenTrack(track.id) },
                    onPlay = { onIntent(FavoritesIntent.PlayTrack(track)) },
                    onFavorite = { onIntent(FavoritesIntent.ToggleFavorite(track)) },
                )
            }
        }
    }
}
