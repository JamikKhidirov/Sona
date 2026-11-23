package com.sona.feature.searchmusic.impl.presentation

import android.app.appsearch.SearchResults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
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
import com.sona.core.ui.component.common.EmptyState
import com.sona.core.ui.component.common.ErrorState
import com.sona.core.ui.component.common.LoadingState
import com.sona.core.ui.component.track.ArtistRow
import com.sona.core.ui.component.track.PlaylistRow
import com.sona.core.ui.component.track.TrackRow
import com.sona.feature.searchmusic.impl.presentation.components.BrowseGenres
import com.sona.feature.searchmusic.impl.presentation.components.SearchField



@Composable
fun SearchRoute(
    viewModelFactory: ViewModelProvider.Factory,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
    onOpenPlaylist: (id: String, name: String) -> Unit,
) {
    val viewModel: SearchViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SearchEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        SearchScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onOpenTrack = onOpenTrack,
            onOpenArtist = onOpenArtist,
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
private fun SearchScreen(
    state: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
    onOpenPlaylist: (id: String, name: String) -> Unit,
) {
    val density = LocalDensity.current
    val maxScrollPx = with(density) { 100.dp.toPx() }
    var scrollOffsetPx by remember { mutableFloatStateOf(0f) }

    val rawProgress by remember {
        derivedStateOf { (scrollOffsetPx / maxScrollPx).coerceIn(0f, 1f) }
    }
    val scrollProgress by remember {
        derivedStateOf { rawProgress * rawProgress }
    }

    val horizontalPadding by remember {
        derivedStateOf { 20.dp * (1f - scrollProgress) }
    }
    val topPadding by remember {
        derivedStateOf { 18.dp * (1f - scrollProgress) }
    }
    val titleVisible by remember {
        derivedStateOf { scrollProgress < 0.9f }
    }
    val shadowElevation by remember {
        derivedStateOf { if (scrollProgress > 0.05f) 8.dp else 0.dp }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = (scrollOffsetPx - delta).coerceIn(0f, maxScrollPx)
                val consumed = newOffset - scrollOffsetPx
                scrollOffsetPx = newOffset
                return Offset(0f, -consumed)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (scrollOffsetPx > 0f && available.y > 0f) {
                    val newOffset = (scrollOffsetPx - available.y).coerceIn(0f, maxScrollPx)
                    val consumedHere = scrollOffsetPx - newOffset
                    scrollOffsetPx = newOffset
                    return Offset(0f, consumedHere)
                }
                return Offset.Zero
            }
        }
    }

    // ИСПРАВЛЕНО: Задаем единый фон всему экрану, чтобы не было прозрачных "дыр"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // <-- ЕДИНЫЙ ФОН ЭКРАНА
            .statusBarsPadding()
    ) {
        // --- ШАПКА ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .shadow(elevation = shadowElevation)
                // ИСПРАВЛЕНО: Сплошной фон перекрывает треки. Тень падает на фон, а не на карточки!
                // Если в твоем Scaffold фон задан как surface, замени background на surface здесь.
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = topPadding,
                )
                .clipToBounds()
        ) {
            AnimatedVisibility(
                visible = titleVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Text(
                        text = "Поиск",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Миллионы треков из открытой сети.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(18.dp))
                }
            }

            SearchField(
                query = state.query,
                onQueryChange = { onIntent(SearchIntent.QueryChanged(it)) },
                onClear = { onIntent(SearchIntent.ClearQuery) },
            )
        }

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        } else {
            Spacer(Modifier.height(6.dp))
        }

        // --- СКРОЛЛЯЩИЙСЯ КОНТЕНТ ---
        Box(modifier = Modifier.weight(1f)) {
            if (!state.hasQuery) {
                BrowseGenres(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection),
                    onGenreClick = { genre ->
                        onIntent(
                            SearchIntent.BrowseGenre(
                                genre = genre.apiName,
                                displayName = genre.displayName,
                            ),
                        )
                    },
                )
            } else {
                SearchResults(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    nestedScrollConnection = nestedScrollConnection,
                    onIntent = onIntent,
                    onOpenTrack = onOpenTrack,
                    onOpenArtist = onOpenArtist,
                    onOpenPlaylist = onOpenPlaylist,
                )
            }
        }
    }
}

@Composable
private fun SearchResults(
    state: SearchUiState,
    modifier: Modifier = Modifier,
    nestedScrollConnection: NestedScrollConnection,
    onIntent: (SearchIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
    onOpenPlaylist: (id: String, name: String) -> Unit,
) {
    Column(modifier = modifier) {
        TabRow(
            selectedTabIndex = state.selectedTab.ordinal,
            // ИСПРАВЛЕНО: TabRow тоже должен иметь сплошной фон, чтобы не просвечивать
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            SearchTab.entries.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { onIntent(SearchIntent.TabSelected(tab)) },
                    text = { Text(text = tab.label, style = MaterialTheme.typography.labelLarge) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        when {
            state.isLoading && !state.hasResults -> LoadingState(label = "Поиск…")
            state.error != null && !state.hasResults -> ErrorState(
                message = state.error,
                onRetry = { onIntent(SearchIntent.Retry) },
            )
            !state.hasResults -> EmptyState(
                title = "Совпадений пока нет",
                message = "Попробуйте другое написание, имя исполнителя или жанр.",
            )
            else -> SearchTabContent(
                state = state,
                modifier = Modifier.fillMaxSize(),
                nestedScrollConnection = nestedScrollConnection,
                onIntent = onIntent,
                onOpenTrack = onOpenTrack,
                onOpenArtist = onOpenArtist,
                onOpenPlaylist = onOpenPlaylist,
            )
        }
    }
}

@Composable
private fun SearchTabContent(
    state: SearchUiState,
    modifier: Modifier = Modifier,
    nestedScrollConnection: NestedScrollConnection,
    onIntent: (SearchIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenArtist: (id: String, name: String) -> Unit,
    onOpenPlaylist: (id: String, name: String) -> Unit,
) {
    LazyColumn(
        modifier = modifier.nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        when (state.selectedTab) {
            SearchTab.TRACKS -> items(state.tracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    isActive = state.currentTrackId == track.id,
                    isPlaying = state.isPlaying,
                    isFavorite = state.favoriteIds.contains(track.id),
                    onClick = { onOpenTrack(track.id) },
                    onPlay = { onIntent(SearchIntent.PlayTrack(track)) },
                    onFavorite = { onIntent(SearchIntent.ToggleFavorite(track)) },
                )
            }
            SearchTab.ARTISTS -> items(state.artists, key = { it.id }) { artist ->
                ArtistRow(artist = artist, onClick = { onOpenArtist(artist.id, artist.displayName) })
            }
            SearchTab.PLAYLISTS -> items(state.playlists, key = { it.id }) { playlist ->
                PlaylistRow(playlist = playlist, onClick = { onOpenPlaylist(playlist.id, playlist.name) })
            }
        }
    }
}