package com.sona.feature.tracktools.impl.presentation

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.ui.component.common.CircleIconButton
import com.sona.core.ui.component.common.EmptyState
import com.sona.core.ui.component.common.ErrorState
import com.sona.core.ui.component.common.LoadingState
import com.sona.core.ui.component.common.SonaChip
import com.sona.core.ui.component.track.TrackRow

@Composable
fun TrackToolsRoute(
    viewModelFactory: ViewModelProvider.Factory,
    trackId: String,
    onBack: () -> Unit,
    onOpenTrack: (String) -> Unit,
) {
    val viewModel: TrackToolsViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(trackId) {
        viewModel.onIntent(TrackToolsIntent.Load(trackId))
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TrackToolsEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        TrackToolsScreen(
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
private fun TrackToolsScreen(
    state: TrackToolsUiState,
    onIntent: (TrackToolsIntent) -> Unit,
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
                text = "TOOLS",
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
                text = "Ремиксы и стемы",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = state.track?.title?.let { "Всё вокруг «$it»" }
                    ?: "Альтернативные версии и отдельные дорожки трека.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TrackToolsTab.entries.forEach { tab ->
                    val count = when (tab) {
                        TrackToolsTab.REMIXES -> state.remixes.size
                        TrackToolsTab.STEMS -> state.stems.size
                    }
                    SonaChip(
                        text = "${tab.label} $count",
                        selected = state.tab == tab,
                        onClick = { onIntent(TrackToolsIntent.SelectTab(tab)) },
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        when {
            state.isLoading && state.items.isEmpty() -> LoadingState(label = "Ищем версии трека…")
            state.error != null && state.items.isEmpty() -> ErrorState(
                message = state.error,
                onRetry = { onIntent(TrackToolsIntent.Retry) },
            )
            state.items.isEmpty() -> EmptyState(
                title = if (state.tab == TrackToolsTab.REMIXES) "Ремиксов пока нет"
                else "Стемы не опубликованы",
                message = "Автор не выкладывал альтернативные версии этого трека.",
                actionText = "Обновить",
                onAction = { onIntent(TrackToolsIntent.Retry) },
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
                items(state.items, key = { it.id }) { track ->
                    TrackRow(
                        track = track,
                        isActive = state.currentTrackId == track.id,
                        isPlaying = state.isPlaying,
                        isFavorite = state.favoriteIds.contains(track.id),
                        onClick = { onOpenTrack(track.id) },
                        onPlay = { onIntent(TrackToolsIntent.PlayTrack(track)) },
                        onFavorite = { onIntent(TrackToolsIntent.ToggleFavorite(track)) },
                    )
                }
            }
        }
    }
}
