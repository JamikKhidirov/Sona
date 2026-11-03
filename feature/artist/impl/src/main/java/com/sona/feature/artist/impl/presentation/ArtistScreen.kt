package com.sona.feature.artist.impl.presentation

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
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.ui.component.common.CircleIconButton
import com.sona.core.ui.component.common.ErrorState
import com.sona.core.ui.component.common.LoadingState
import com.sona.core.ui.component.common.SectionHeader
import com.sona.core.ui.component.track.ArtistTile
import com.sona.core.ui.component.track.PlaylistTile
import com.sona.core.ui.component.track.TrackRow
import com.sona.core.ui.util.shareText
import com.sona.feature.artist.impl.presentation.components.ArtistHero

@Composable
fun ArtistRoute(
    viewModelFactory: ViewModelProvider.Factory,
    artistId: String,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenPlaylist: (id: String, title: String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
    onOpenCommunity: (id: String) -> Unit,
    onOpenFavorites: (id: String) -> Unit,
) {
    val viewModel: ArtistViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(artistId) {
        viewModel.onIntent(ArtistIntent.Load(artistId))
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ArtistEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        ArtistScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onBack = onBack,
            onOpenTrack = onOpenTrack,
            onOpenPlaylist = onOpenPlaylist,
            onOpenArtist = onOpenArtist,
            onOpenCommunity = onOpenCommunity,
            onOpenFavorites = onOpenFavorites,
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
private fun ArtistScreen(
    state: ArtistUiState,
    onIntent: (ArtistIntent) -> Unit,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenPlaylist: (id: String, title: String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
    onOpenCommunity: (id: String) -> Unit,
    onOpenFavorites: (id: String) -> Unit,
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
                contentDescription = "Back",
                onClick = onBack,
                size = 42.dp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "ARTIST",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            val artist = state.artist
            if (artist != null) {
                CircleIconButton(
                    icon = Icons.Rounded.Share,
                    contentDescription = "Share artist",
                    onClick = {
                        context.shareText(
                            text = "${artist.displayName} — https://audius.co/${artist.handle}",
                            title = "Share artist",
                        )
                    },
                    size = 42.dp,
                )
            } else {
                Spacer(Modifier.size(42.dp))
            }
        }

        when {
            state.isLoading && state.artist == null -> LoadingState(label = "Загружаем артиста…")
            state.error != null && state.artist == null -> ErrorState(
                message = state.error.orEmpty(),
                onRetry = { onIntent(ArtistIntent.Retry) },
            )
            state.artist != null -> {
                val artist = state.artist
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item("hero") {
                        ArtistHero(
                            artist = artist,
                            isFollowing = state.isFollowing,
                            isFollowLoading = state.isFollowLoading,
                            isSignedIn = state.isSignedIn,
                            trackCount = state.tracks.size,
                            onToggleFollow = { onIntent(ArtistIntent.ToggleFollow) },
                            onPlayAll = { onIntent(ArtistIntent.PlayAll) },
                        )
                    }

                    item("community-actions") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedButton(
                                onClick = { onOpenCommunity(artist.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    text = "Сообщество",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                            OutlinedButton(
                                onClick = { onOpenFavorites(artist.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    text = "Избранное",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }

                    if (state.playlists.isNotEmpty()) {
                        item("playlists-header") {
                            SectionHeader(title = "Плейлисты")
                        }
                        item("playlists") {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(state.playlists, key = { it.id }) { playlist ->
                                    PlaylistTile(
                                        playlist = playlist,
                                        onClick = { onOpenPlaylist(playlist.id, playlist.name) },
                                    )
                                }
                            }
                        }
                    }

                    if (state.tracks.isNotEmpty()) {
                        item("tracks-header") {
                            SectionHeader(
                                title = "Треки",
                                action = "${state.tracks.size}",
                            )
                        }
                        items(state.tracks, key = { it.id }) { track ->
                            TrackRow(
                                track = track,
                                isActive = state.currentTrackId == track.id,
                                isPlaying = state.isPlaying,
                                isFavorite = state.favoriteIds.contains(track.id),
                                onClick = { onOpenTrack(track.id) },
                                onPlay = { onIntent(ArtistIntent.PlayTrack(track)) },
                                onFavorite = { onIntent(ArtistIntent.ToggleFavorite(track)) },
                            )
                        }
                    }

                    if (state.related.isNotEmpty()) {
                        item("related-header") {
                            SectionHeader(title = "Похожие артисты")
                        }
                        item("related") {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(state.related, key = { it.id }) { related ->
                                    ArtistTile(
                                        artist = related,
                                        onClick = { onOpenArtist(related.id, related.displayName) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
