package com.sona.feature.playlistdetail.impl.presentation

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.common.model.Playlist
import com.sona.core.ui.component.Artwork
import com.sona.core.ui.component.common.CircleIconButton
import com.sona.core.ui.component.common.ErrorState
import com.sona.core.ui.component.common.LoadingState
import com.sona.core.ui.component.common.SectionHeader
import com.sona.core.ui.component.track.ArtistTile
import com.sona.core.ui.component.track.TrackRow
import com.sona.core.ui.format.formatCount
import com.sona.core.ui.format.formatDuration
import com.sona.core.ui.util.shareText

@Composable
fun PlaylistDetailRoute(
    viewModelFactory: ViewModelProvider.Factory,
    playlistId: String,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
) {
    val viewModel: PlaylistDetailViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(playlistId) {
        viewModel.onIntent(PlaylistDetailIntent.Load(playlistId))
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PlaylistDetailEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        PlaylistDetailScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onBack = onBack,
            onOpenTrack = onOpenTrack,
            onOpenArtist = onOpenArtist,
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
private fun PlaylistDetailScreen(
    state: PlaylistDetailUiState,
    onIntent: (PlaylistDetailIntent) -> Unit,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
) {
    val context = LocalContext.current
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
                text = if (state.playlist?.isAlbum == true) "ALBUM" else "PLAYLIST",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            val playlist = state.playlist
            if (playlist != null) {
                CircleIconButton(
                    icon = Icons.Rounded.Share,
                    contentDescription = "Поделиться",
                    onClick = {
                        context.shareText(
                            text = "${playlist.name} — ${playlist.ownerName ?: "Audius"}\n" +
                                "https://audius.co/playlist/${playlist.id}",
                            title = "Поделиться плейлистом",
                        )
                    },
                    size = 42.dp,
                )
            } else {
                Spacer(Modifier.size(42.dp))
            }
        }

        when {
            state.isLoading && state.playlist == null -> LoadingState(label = "Загружаем плейлист…")
            state.error != null && state.playlist == null -> ErrorState(
                message = state.error.orEmpty(),
                onRetry = { onIntent(PlaylistDetailIntent.Retry) },
            )
            state.playlist != null -> PlaylistContent(
                state = state,
                playlist = state.playlist,
                onIntent = onIntent,
                onOpenTrack = onOpenTrack,
                onOpenArtist = onOpenArtist,
            )
        }
    }
}

@Composable
private fun PlaylistContent(
    state: PlaylistDetailUiState,
    playlist: Playlist,
    onIntent: (PlaylistDetailIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item("hero") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Artwork(
                    url = playlist.artworkUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    shape = RoundedCornerShape(26.dp),
                    contentDescription = playlist.name,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                playlist.ownerName?.takeIf { it.isNotBlank() }?.let { owner ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = owner,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = buildString {
                        append(playlist.trackCount)
                        append(" треков")
                        if (state.totalDurationSeconds > 0) {
                            append("  ·  ")
                            append(formatDuration(state.totalDurationSeconds))
                        }
                        append("  ·  ")
                        append(formatCount(playlist.playCount))
                        append(" прослушиваний")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item("actions") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { onIntent(PlaylistDetailIntent.PlayAll) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    enabled = state.tracks.isNotEmpty(),
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
                OutlinedButton(
                    onClick = { onIntent(PlaylistDetailIntent.TogglePlaylistFavorite) },
                    enabled = !state.isFavoriteLoading,
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(
                        imageVector = if (state.isPlaylistFavorite) Icons.Rounded.Favorite
                        else Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = when {
                            state.isFavoriteLoading -> "..."
                            state.isPlaylistFavorite -> "Сохранено"
                            state.isSignedIn -> "Сохранить"
                            else -> "Войти"
                        },
                        modifier = Modifier.padding(start = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        playlist.description?.takeIf { it.isNotBlank() }?.let { description ->
            item("description") {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (state.favoritedBy.isNotEmpty()) {
            item("favorited-header") {
                SectionHeader(title = "Сохранили")
            }
            item("favorited-list") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(state.favoritedBy, key = { it.id }) { artist ->
                        ArtistTile(
                            artist = artist,
                            onClick = { onOpenArtist(artist.id, artist.displayName) },
                        )
                    }
                }
            }
        }

        if (state.tracks.isNotEmpty()) {
            item("tracks-header") {
                SectionHeader(title = "Треки", action = state.tracks.size.toString())
            }
            items(state.tracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    isActive = state.currentTrackId == track.id,
                    isPlaying = state.isPlaying,
                    isFavorite = state.favoriteIds.contains(track.id),
                    onClick = { onOpenTrack(track.id) },
                    onPlay = { onIntent(PlaylistDetailIntent.PlayTrack(track)) },
                    onFavorite = { onIntent(PlaylistDetailIntent.ToggleTrackFavorite(track)) },
                )
            }
        }
    }
}
