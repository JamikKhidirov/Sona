package com.sona.feature.charts.impl.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.common.model.Genres
import com.sona.core.common.model.Track
import com.sona.core.ui.component.Artwork
import com.sona.core.ui.component.common.CircleIconButton
import com.sona.core.ui.component.common.EmptyState
import com.sona.core.ui.component.common.ErrorState
import com.sona.core.ui.component.common.LoadingState
import com.sona.core.ui.component.common.SonaChip
import com.sona.core.ui.format.formatDuration

@Composable
fun ChartsRoute(
    viewModelFactory: ViewModelProvider.Factory,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
) {
    val viewModel: ChartsViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ChartsEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        ChartsScreen(
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
private fun ChartsScreen(
    state: ChartsUiState,
    onIntent: (ChartsIntent) -> Unit,
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
                text = "CHARTS",
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
                text = "Чарты Audius",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Что слушает открытая сеть прямо сейчас — по неделям, месяцам и жанрам.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ChartPeriod.entries.toList(), key = { it.name }) { period ->
                SonaChip(
                    text = period.label,
                    selected = state.period == period,
                    onClick = { onIntent(ChartsIntent.SelectPeriod(period)) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item("all-genres") {
                SonaChip(
                    text = "Все жанры",
                    selected = state.genre == null,
                    onClick = { onIntent(ChartsIntent.SelectGenre(null)) },
                )
            }
            items(Genres.all, key = { it.apiName }) { genre ->
                SonaChip(
                    text = genre.displayName,
                    selected = state.genre == genre.apiName,
                    onClick = { onIntent(ChartsIntent.SelectGenre(genre.apiName)) },
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading && state.tracks.isEmpty() -> LoadingState(label = "Считаем чарты…")
                state.error != null && state.tracks.isEmpty() -> ErrorState(
                    message = state.error,
                    onRetry = { onIntent(ChartsIntent.Retry) },
                )
                state.tracks.isEmpty() -> EmptyState(
                    title = "Чарт пуст",
                    message = "Для выбранного периода и жанра пока нет треков.",
                    actionText = "Обновить",
                    onAction = { onIntent(ChartsIntent.Retry) },
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
                    itemsIndexed(state.tracks, key = { _, track -> track.id }) { index, track ->
                        ChartRow(
                            rank = index + 1,
                            track = track,
                            isActive = state.currentTrackId == track.id,
                            isPlaying = state.isPlaying,
                            isFavorite = state.favoriteIds.contains(track.id),
                            onClick = { onOpenTrack(track.id) },
                            onPlay = { onIntent(ChartsIntent.PlayTrack(track)) },
                            onFavorite = { onIntent(ChartsIntent.ToggleFavorite(track)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartRow(
    rank: Int,
    track: Track,
    isActive: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
                else Color.Transparent,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = if (rank <= 3) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(24.dp),
        )
        Artwork(
            url = track.artworkUrl,
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(14.dp),
            contentDescription = track.title,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = buildString {
                    append(track.artistName)
                    track.genre?.takeIf { it.isNotBlank() }?.let {
                        append("  ·  ")
                        append(it)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = formatDuration(track.durationSeconds),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(onClick = onFavorite, modifier = Modifier.size(34.dp)) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = if (isFavorite) "Убрать из избранного" else "В избранное",
                tint = if (isFavorite) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                )
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isActive && isPlaying) Icons.Rounded.Pause
                else Icons.Rounded.PlayArrow,
                contentDescription = if (isActive && isPlaying) "Пауза" else "Играть",
                tint = if (isActive) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
