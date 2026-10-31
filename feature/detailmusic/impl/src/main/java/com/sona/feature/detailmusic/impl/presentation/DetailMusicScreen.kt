package com.sona.feature.detailmusic.impl.presentation

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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.common.model.Track
import com.sona.core.ui.component.CircleIconButton
import com.sona.core.ui.component.ErrorState
import com.sona.core.ui.component.GradientCircleButton
import com.sona.core.ui.component.LoadingState
import com.sona.core.ui.component.SectionHeader
import com.sona.core.ui.component.StatPill
import com.sona.core.ui.component.TrackRow
import com.sona.core.ui.format.formatCount
import com.sona.core.ui.format.formatDuration
import com.sona.feature.detailmusic.impl.presentation.components.TrackDetailHero

@Composable
fun DetailMusicRoute(
    viewModelFactory: ViewModelProvider.Factory,
    trackId: String,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
) {
    val viewModel: DetailMusicViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(trackId) {
        viewModel.onIntent(DetailMusicIntent.Load(trackId))
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DetailMusicEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        DetailMusicScreen(
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
private fun DetailMusicScreen(
    state: DetailMusicUiState,
    onIntent: (DetailMusicIntent) -> Unit,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
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
                text = "TRACK",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            val track = state.track
            if (track != null) {
                CircleIconButton(
                    icon = if (state.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (state.isFavorite) "Remove from favorites" else "Add to favorites",
                    onClick = { onIntent(DetailMusicIntent.ToggleFavorite(track)) },
                    tint = if (state.isFavorite) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurface,
                    size = 42.dp,
                )
            } else {
                Spacer(Modifier.size(42.dp))
            }
        }

        when {
            state.isLoading -> LoadingState(label = "Loading track…")
            state.error != null && state.track == null -> ErrorState(
                message = state.error.orEmpty(),
                onRetry = { onIntent(DetailMusicIntent.Retry) },
            )
            state.track != null -> DetailContent(
                state = state,
                track = state.track,
                onIntent = onIntent,
                onOpenTrack = onOpenTrack,
                onOpenArtist = onOpenArtist,
            )
        }
    }
}

@Composable
private fun DetailContent(
    state: DetailMusicUiState,
    track: Track,
    onIntent: (DetailMusicIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item("hero") {
            TrackDetailHero(
                track = track,
                onOpenArtist = onOpenArtist,
            )
        }

        item("actions") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                GradientCircleButton(
                    icon = if (state.isCurrent && state.isPlaying) Icons.Rounded.Pause
                    else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isCurrent && state.isPlaying) "Pause" else "Play",
                    onClick = { onIntent(DetailMusicIntent.Play(track)) },
                    size = 66.dp,
                )
                Spacer(Modifier.size(16.dp))
                CircleIconButton(
                    icon = if (state.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Favorite",
                    onClick = { onIntent(DetailMusicIntent.ToggleFavorite(track)) },
                    tint = if (state.isFavorite) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurface,
                    size = 52.dp,
                )
            }
        }

        item("stats") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatPill(Icons.Rounded.Headphones, formatCount(track.playCount) + " plays")
                StatPill(Icons.Rounded.Favorite, formatCount(track.favoriteCount) + " likes")
                StatPill(Icons.Rounded.Repeat, formatCount(track.repostCount) + " reposts")
            }
        }

        item("duration") {
            Text(
                text = "${formatDuration(track.durationSeconds)}  ·  ${formatCount(track.commentCount)} comments",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        val description = track.description
        if (!description.isNullOrBlank()) {
            item("about-title") {
                SectionHeader(title = "About this track")
            }
            item("about") {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (state.related.isNotEmpty()) {
            item("related-header") {
                SectionHeader(
                    title = "More like this",
                    action = "Artist",
                    onAction = { onOpenArtist(track.artist.id, track.artistName) },
                )
            }
            items(state.related, key = { it.id }) { related ->
                TrackRow(
                    track = related,
                    isActive = state.currentTrackId == related.id,
                    isPlaying = state.isPlaying,
                    onClick = { onOpenTrack(related.id) },
                    onPlay = { onIntent(DetailMusicIntent.Play(related)) },
                    showFavorite = false,
                )
            }
        }
    }
}
