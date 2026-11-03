package com.sona.feature.artistcommunity.impl.presentation

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.ui.component.common.CircleIconButton
import com.sona.core.ui.component.common.EmptyState
import com.sona.core.ui.component.common.ErrorState
import com.sona.core.ui.component.common.LoadingState
import com.sona.core.ui.component.common.SonaChip
import com.sona.core.ui.component.track.ArtistRow

@Composable
fun ArtistCommunityRoute(
    viewModelFactory: ViewModelProvider.Factory,
    artistId: String,
    onBack: () -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
) {
    val viewModel: ArtistCommunityViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(artistId) {
        viewModel.onIntent(ArtistCommunityIntent.Load(artistId))
    }

    ArtistCommunityScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onOpenArtist = onOpenArtist,
    )
}

@Composable
private fun ArtistCommunityScreen(
    state: ArtistCommunityUiState,
    onIntent: (ArtistCommunityIntent) -> Unit,
    onBack: () -> Unit,
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
                contentDescription = "Назад",
                onClick = onBack,
                size = 42.dp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "COMMUNITY",
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
                text = "Сообщество",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = state.artist?.displayName?.let { "Вокруг $it" }
                    ?: "Люди, которые слушают этого артиста.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CommunityTab.entries.forEach { tab ->
                    SonaChip(
                        text = "${tab.label} ${if (tab == CommunityTab.FOLLOWERS) state.followers.size else state.following.size}",
                        selected = state.tab == tab,
                        onClick = { onIntent(ArtistCommunityIntent.SelectTab(tab)) },
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        when {
            state.isLoading && state.items.isEmpty() -> LoadingState(label = "Загружаем сообщество…")
            state.error != null && state.items.isEmpty() -> ErrorState(
                message = state.error,
                onRetry = { onIntent(ArtistCommunityIntent.Retry) },
            )
            state.items.isEmpty() -> EmptyState(
                title = if (state.tab == CommunityTab.FOLLOWERS) "Пока нет подписчиков"
                else "Пока нет подписок",
                message = "Список появится, как только артист начнёт взаимодействовать с сетью.",
                actionText = "Обновить",
                onAction = { onIntent(ArtistCommunityIntent.Retry) },
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
                items(state.items, key = { it.id }) { artist ->
                    ArtistRow(
                        artist = artist,
                        onClick = { onOpenArtist(artist.id, artist.displayName) },
                    )
                }
            }
        }
    }
}
