package com.sona.feature.home.impl.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.common.model.Genres
import com.sona.feature.home.impl.presentation.components.FeaturedTrackCard
import com.sona.feature.home.impl.presentation.components.HomeHeader
import com.sona.feature.home.impl.presentation.components.HomeSearchPill
import com.sona.core.ui.component.common.EmptyState
import com.sona.core.ui.component.common.ErrorState
import com.sona.core.ui.component.common.LoadingState
import com.sona.core.ui.component.common.SectionHeader
import com.sona.core.ui.component.common.SonaChip
import com.sona.core.ui.component.track.ArtistTile
import com.sona.core.ui.component.track.TrackRow
import com.sona.core.ui.component.track.TrackTile
import model.HomeEffect
import intent.HomeIntent

@Composable
fun HomeRoute(
    viewModelFactory: ViewModelProvider.Factory,
    onOpenTrack: (String) -> Unit,
    onOpenList: (source: String, id: String, title: String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        HomeScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onOpenTrack = onOpenTrack,
            onOpenList = onOpenList,
            onOpenSearch = onOpenSearch,
            onOpenSettings = onOpenSettings,
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
private fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenList: (source: String, id: String, title: String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val density = LocalDensity.current
    // Дистанция, после которой шапка полностью скрывается
    val maxScrollPx = with(density) { 150.dp.toPx() }

    var scrollOffsetPx by remember { mutableFloatStateOf(0f) }

    val scrollProgress by remember {
        derivedStateOf { (scrollOffsetPx / maxScrollPx).coerceIn(0f, 1f) }
    }

    // Плавное затухание и сжатие для хедера
    val headerAlpha = 1f - (scrollProgress * 1.5f).coerceIn(0f, 1f)
    val headerVisible = scrollProgress < 0.8f

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Тянем вверх (available.y < 0) или вниз (available.y > 0)
                val newOffset = (scrollOffsetPx - available.y).coerceIn(0f, maxScrollPx)
                val consumed = scrollOffsetPx - newOffset
                scrollOffsetPx = newOffset
                return Offset(0f, available.y + consumed)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .nestedScroll(nestedScrollConnection)
    ) {
        // --- ВЕРХНЯЯ ЧАСТЬ (Лого + Заголовок + Поиск) ---
        // Оборачиваем в AnimatedVisibility, чтобы при скролле она плавно складывалась и уходила.
        AnimatedVisibility(
            visible = headerVisible,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .alpha(headerAlpha) // Дополнительное затухание
            ) {
                HomeHeader(onOpenSettings = onOpenSettings)
                Spacer(Modifier.height(22.dp))
                Text(
                    text = "Найди свою следующую навязчивую идею",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(18.dp))
                HomeSearchPill(onClick = onOpenSearch)
            }
        }

        // --- ЛИПКИЕ ЖАНРЫ ---
        // Они не скроллятся вместе с лентой, а всегда находятся сверху
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = if (scrollProgress > 0.05f) 8.dp else 0.dp)
                .background(Color.Transparent)
        ) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item("all") {
                    SonaChip(
                        text = "Все",
                        selected = state.selectedGenre == null,
                        onClick = { onIntent(HomeIntent.SelectGenre(null)) },
                    )
                }
                items(Genres.all, key = { it.name }) { genre ->
                    SonaChip(
                        text = genre.name,
                        selected = state.selectedGenre == genre.name,
                        onClick = { onIntent(HomeIntent.SelectGenre(genre.name)) },
                    )
                }
            }
        }

        // --- СКРОЛЛЯЩИЙСЯ КОНТЕНТ (Лента) ---
        val trendingTracks = remember(state.trending) { state.trending.take(8) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f), // Занимает оставшееся место
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Убрали "header" и "genres" из LazyColumn, так как они теперь наверху
            val featured = state.featured
            if (featured != null) {
                item("featured") {
                    FeaturedTrackCard(
                        track = featured,
                        isPlaying = state.currentTrackId == featured.id && state.isPlaying,
                        onClick = { onOpenTrack(featured.id) },
                        onPlay = { onIntent(HomeIntent.PlayFeatured) },
                    )
                }
            }

            if (state.isLoading && state.tracks.isEmpty()) {
                item("loading") { LoadingState() }
            } else if (state.error != null && state.tracks.isEmpty()) {
                item("error") {
                    ErrorState(message = state.error.orEmpty(), onRetry = { onIntent(HomeIntent.Retry) })
                }
            } else if (state.tracks.isEmpty()) {
                item("empty") {
                    EmptyState(
                        title = "На циферблате ничего нет",
                        message = "Попробуйте другой жанр или обновите ленту.",
                        actionText = "Обновить",
                        onAction = { onIntent(HomeIntent.Retry) },
                    )
                }
            }

            if (trendingTracks.isNotEmpty()) {
                item("trending-header") {
                    SectionHeader(
                        title = if (state.selectedGenre == null) "Сейчас это в тренде" else "${state.selectedGenre} жара",
                        action = "Увидеть все",
                        onAction = {
                            val genre = state.selectedGenre
                            if (genre == null) {
                                onOpenList("trending", "all", "Trending now")
                            } else {
                                onOpenList("genre", genre, genre)
                            }
                        },
                    )
                }
                items(trendingTracks, key = { it.id }) { track ->
                    TrackRow(
                        track = track,
                        isActive = state.currentTrackId == track.id,
                        isPlaying = state.isPlaying,
                        isFavorite = state.favoriteIds.contains(track.id),
                        onClick = { onOpenTrack(track.id) },
                        onPlay = { onIntent(HomeIntent.PlayTrack(track)) },
                        onFavorite = { onIntent(HomeIntent.ToggleFavorite(track)) },
                    )
                }
            }

            if (state.freshDrops.isNotEmpty()) {
                item("fresh-header") {
                    SectionHeader(title = "Свежие капли", action = "Исследовать") {
                        onOpenList("trending", "all", "Fresh drops")
                    }
                }
                item("fresh-list") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.freshDrops, key = { it.id }) { track ->
                            TrackTile(
                                track = track,
                                isActive = state.currentTrackId == track.id,
                                isPlaying = state.isPlaying,
                                onClick = { onOpenTrack(track.id) },
                                onPlay = { onIntent(HomeIntent.PlayTrack(track)) },
                            )
                        }
                    }
                }
            }

            if (state.artists.isNotEmpty()) {
                item("artists-header") {
                    SectionHeader(title = "Артисты, за которыми стоит наблюдать")
                }
                item("artists-list") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.artists, key = { it.id }) { artist ->
                            ArtistTile(
                                artist = artist,
                                onClick = { onOpenList("artist", artist.id, artist.displayName) },
                            )
                        }
                    }
                }
            }

            item("footer") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Работает от открытой сети Audius",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}