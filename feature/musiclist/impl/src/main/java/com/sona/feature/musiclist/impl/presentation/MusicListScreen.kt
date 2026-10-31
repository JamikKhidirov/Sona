package com.sona.feature.musiclist.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
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
import com.sona.core.ui.component.CircleIconButton
import com.sona.core.ui.component.EmptyState
import com.sona.core.ui.component.ErrorState
import com.sona.core.ui.component.LoadingState
import com.sona.core.ui.component.TrackRow
import com.sona.core.ui.format.formatDuration
import com.sona.feature.musiclist.impl.presentation.components.CollectionHero

@Composable
fun MusicListRoute(
    viewModelFactory: ViewModelProvider.Factory,
    source: String,
    id: String,
    title: String,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
) {
    val viewModel: MusicListViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(source, id) {
        viewModel.onIntent(MusicListIntent.Load(source, id, title))
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MusicListEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        MusicListScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onBack = onBack,
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
private fun MusicListScreen(
    state: MusicListUiState,
    onIntent: (MusicListIntent) -> Unit,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(
                icon = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = state.source.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(42.dp))
        }

        when {
            state.isLoading -> LoadingState(label = "Gathering tracks…")
            state.error != null && state.tracks.isEmpty() -> ErrorState(
                message = state.error.orEmpty(),
                onRetry = { onIntent(MusicListIntent.Retry) },
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                item("hero") {
                    CollectionHero(
                        title = state.title,
                        subtitle = "${state.tracks.size} tracks  ·  ${formatDuration(state.totalDurationSeconds)}",
                        artworkUrl = state.heroArtwork,
                        showPlayAll = state.tracks.isNotEmpty(),
                        onPlayAll = { onIntent(MusicListIntent.PlayAll) },
                    )
                }
                if (state.tracks.isEmpty()) {
                    item("empty") {
                        EmptyState(
                            title = "No tracks here",
                            message = "Try another collection from Home or Search.",
                        )
                    }
                } else {
                    items(state.tracks, key = { it.id }) { track ->
                        TrackRow(
                            track = track,
                            isActive = state.currentTrackId == track.id,
                            isPlaying = state.isPlaying,
                            isFavorite = state.favoriteIds.contains(track.id),
                            onClick = { onOpenTrack(track.id) },
                            onPlay = { onIntent(MusicListIntent.PlayTrack(track)) },
                            onFavorite = { onIntent(MusicListIntent.ToggleFavorite(track)) },
                        )
                    }
                }
            }
        }
    }
}
