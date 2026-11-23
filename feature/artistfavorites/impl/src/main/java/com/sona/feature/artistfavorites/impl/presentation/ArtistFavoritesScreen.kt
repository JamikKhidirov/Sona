package com.sona.feature.artistfavorites.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.ui.component.common.CircleIconButton
import com.sona.core.ui.component.common.EmptyState
import com.sona.core.ui.component.common.ErrorState
import com.sona.core.ui.component.common.LoadingState
import com.sona.core.ui.component.track.TrackRow

@Composable
fun ArtistFavoritesRoute(
    viewModelFactory: ViewModelProvider.Factory,
    artistId: String,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
) {
    val viewModel: ArtistFavoritesViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(artistId) {
        viewModel.onIntent(ArtistFavoritesIntent.Load(artistId))
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ArtistFavoritesEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        ArtistFavoritesScreen(
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
private fun ArtistFavoritesScreen(
    state: ArtistFavoritesUiState,
    onIntent: (ArtistFavoritesIntent) -> Unit,
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
                contentDescription = "Назад",
                onClick = onBack,
                size = 42.dp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "FAVORITES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(42.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = "Избранное артиста",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = state.artist?.displayName?.let { "Что слушает $it" }
                    ?: "Треки, которые артист сохранил в своей библиотеке.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { onIntent(ArtistFavoritesIntent.PlayAll) },
                enabled = state.tracks.isNotEmpty(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Слушать всё",
                    modifier = Modifier.padding(start = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(6.dp))
        }

        when {
            state.isLoading && state.tracks.isEmpty() -> LoadingState(label = "Загружаем избранное…")
            state.error != null && state.tracks.isEmpty() -> ErrorState(
                message = state.error,
                onRetry = { onIntent(ArtistFavoritesIntent.Retry) },
            )
            state.tracks.isEmpty() -> EmptyState(
                title = "Избранное пусто",
                message = "Артист пока не сохранил ни одного трека.",
                actionText = "Обновить",
                onAction = { onIntent(ArtistFavoritesIntent.Retry) },
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = 28.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(state.tracks, key = { it.id }) { track ->
                    TrackRow(
                        track = track,
                        isActive = state.currentTrackId == track.id,
                        isPlaying = state.isPlaying,
                        isFavorite = state.favoriteIds.contains(track.id),
                        onClick = { onOpenTrack(track.id) },
                        onPlay = { onIntent(ArtistFavoritesIntent.PlayTrack(track)) },
                        onFavorite = { onIntent(ArtistFavoritesIntent.ToggleFavorite(track)) },
                    )
                }
            }
        }
    }
}
