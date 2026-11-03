package com.sona.feature.explore.impl.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.common.model.Playlist
import com.sona.core.ui.component.Artwork
import com.sona.core.ui.component.common.CircleIconButton
import com.sona.core.ui.component.common.EmptyState
import com.sona.core.ui.component.common.ErrorState
import com.sona.core.ui.component.common.LoadingState
import com.sona.core.ui.component.common.SonaChip
import com.sona.core.ui.component.track.TrackRow

@Composable
fun ExploreRoute(
    viewModelFactory: ViewModelProvider.Factory,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenPlaylist: (id: String, title: String) -> Unit,
) {
    val viewModel: ExploreViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ExploreEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        ExploreScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onBack = onBack,
            onOpenTrack = onOpenTrack,
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
private fun ExploreScreen(
    state: ExploreUiState,
    onIntent: (ExploreIntent) -> Unit,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenPlaylist: (id: String, title: String) -> Unit,
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
                size = 42.dp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "EXPLORE",
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
                text = "Открывай новое",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Плейлисты недели и скрытые gems андеграунда — всё в одном месте.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SonaChip(
                    text = "Плейлисты",
                    selected = state.tab == ExploreTab.PLAYLISTS,
                    onClick = { onIntent(ExploreIntent.SelectTab(ExploreTab.PLAYLISTS)) },
                )
                SonaChip(
                    text = "Андеграунд",
                    selected = state.tab == ExploreTab.UNDERGROUND,
                    onClick = { onIntent(ExploreIntent.SelectTab(ExploreTab.UNDERGROUND)) },
                )
            }
            Spacer(Modifier.height(6.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading && state.itemsCount == 0 -> LoadingState(
                    label = "Собираем подборку…",
                )
                state.error != null && state.itemsCount == 0 -> ErrorState(
                    message = state.error,
                    onRetry = { onIntent(ExploreIntent.Retry) },
                )
                state.tab == ExploreTab.PLAYLISTS -> {
                    if (state.playlists.isEmpty()) {
                        EmptyState(
                            title = "Плейлистов пока нет",
                            message = "Audius ещё не собрал плейлисты недели. Загляните позже.",
                            actionText = "Обновить",
                            onAction = { onIntent(ExploreIntent.Retry) },
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 4.dp,
                                bottom = 28.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(state.playlists, key = { it.id }) { playlist ->
                                PlaylistRow(
                                    playlist = playlist,
                                    onClick = { onOpenPlaylist(playlist.id, playlist.name) },
                                )
                            }
                        }
                    }
                }
                else -> {
                    if (state.underground.isEmpty()) {
                        EmptyState(
                            title = "Андеграунд молчит",
                            message = "Не удалось найти треки вне мейнстрима. Попробуйте обновить.",
                            actionText = "Обновить",
                            onAction = { onIntent(ExploreIntent.Retry) },
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 4.dp,
                                bottom = 28.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            items(state.underground, key = { it.id }) { track ->
                                TrackRow(
                                    track = track,
                                    isActive = state.currentTrackId == track.id,
                                    isPlaying = state.isPlaying,
                                    isFavorite = state.favoriteIds.contains(track.id),
                                    onClick = { onOpenTrack(track.id) },
                                    onPlay = { onIntent(ExploreIntent.PlayTrack(track)) },
                                    onFavorite = { onIntent(ExploreIntent.ToggleFavorite(track)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Artwork(
            url = playlist.artworkUrl,
            modifier = Modifier.size(62.dp),
            shape = RoundedCornerShape(16.dp),
            contentDescription = playlist.name,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = buildString {
                    playlist.ownerName?.takeIf { it.isNotBlank() }?.let {
                        append(it)
                        append("  ·  ")
                    }
                    append(playlist.trackCount)
                    append(" треков")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
