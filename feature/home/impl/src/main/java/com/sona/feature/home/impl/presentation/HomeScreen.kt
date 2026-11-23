package com.sona.feature.home.impl.presentation

import android.R.attr.translationY
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    onOpenArtist: (id: String) -> Unit,
    onOpenExplore: () -> Unit,
    onOpenCharts: () -> Unit,
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
            onOpenArtist = onOpenArtist,
            onOpenExplore = onOpenExplore,
            onOpenCharts = onOpenCharts,
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
    onOpenArtist: (id: String) -> Unit,
    onOpenExplore: () -> Unit,
    onOpenCharts: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    // 1. Логика первого запуска (без дергания при обновлении)
    val isFirstLaunch = rememberSaveable { mutableStateOf(true) }
    val isInitialLoading = state.isLoading && state.tracks.isEmpty() && state.featured == null

    LaunchedEffect(isInitialLoading) {
        if (!isInitialLoading && isFirstLaunch.value) {
            isFirstLaunch.value = false
        }
    }

    val showFullScreenLoader = isFirstLaunch.value && isInitialLoading

    val density = LocalDensity.current
    val maxScrollPx = with(density) { 150.dp.toPx() }
    var scrollOffsetPx by remember { mutableFloatStateOf(0f) }

    val scrollProgress by remember {
        derivedStateOf { (scrollOffsetPx / maxScrollPx).coerceIn(0f, 1f) }
    }
    val headerVisible = scrollProgress < 0.8f

    // 2. Стейт списка для отслеживания позиции
    val lazyListState = rememberLazyListState()

    // 3. ИСПРАВЛЕННАЯ ЛОГИКА СКРОЛЛА (Без залипания)
    // Список скроллится до самого верха -> ТОЛЬКО ПОТОМ открывается шапка.
    // При скролле вниз шапка закрывается сразу.
    val nestedScrollConnection = remember(lazyListState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val isListAtTop = lazyListState.firstVisibleItemIndex == 0 &&
                        lazyListState.firstVisibleItemScrollOffset == 0

                // delta < 0: Палец вверх (скроллим контент вниз) -> СКРЫВАЕМ шапку
                if (delta < 0 && scrollOffsetPx < maxScrollPx) {
                    val newOffset = (scrollOffsetPx - delta).coerceIn(0f, maxScrollPx)
                    val consumed = newOffset - scrollOffsetPx
                    scrollOffsetPx = newOffset
                    return Offset(0f, -consumed)
                }

                // delta > 0: Палец вниз (скроллим контент вверх) -> ОТКРЫВАЕМ шапку
                // Открываем ТОЛЬКО если лента уже в самом верху
                if (delta > 0 && scrollOffsetPx > 0f && isListAtTop) {
                    val newOffset = (scrollOffsetPx - delta).coerceIn(0f, maxScrollPx)
                    val consumed = newOffset - scrollOffsetPx
                    scrollOffsetPx = newOffset
                    return Offset(0f, -consumed)
                }

                return Offset.Zero
            }
        }
    }

    // 4. ИСПРАВЛЕНО: Цвет панели жанров теперь идеально совпадает с фоном


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Явный фон
            .statusBarsPadding()
    ) {
        // --- ИНДИКАТОР ЗАГРУЗКИ ---
        AnimatedVisibility(
            visible = showFullScreenLoader,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(400, easing = FastOutSlowInEasing))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingState()
            }
        }

        // --- ОСНОВНОЙ ЭКРАН ---
        AnimatedVisibility(
            visible = !showFullScreenLoader,
            enter = fadeIn(animationSpec = tween(600, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight / 4 },
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    ),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- ВЕРХНЯЯ ЧАСТЬ (Лого + Заголовок + Поиск) ---
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                            expandVertically(
                                expandFrom = Alignment.Top,
                                animationSpec = tween(350, easing = FastOutSlowInEasing)
                            ),
                    exit = fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                            shrinkVertically(
                                shrinkTowards = Alignment.Top,
                                animationSpec = tween(350, easing = FastOutSlowInEasing)
                            )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        HomeHeader(
                            onOpenSettings = onOpenSettings,
                            onOpenExplore = onOpenExplore,
                        )
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
                Surface(
                    color = MaterialTheme.colorScheme.background, // Динамический цвет
                    tonalElevation = if (scrollProgress > 0.05f) 3.dp else 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item("all") {
                            SonaChip(
                                text = "Все",
                                selected = state.selectedGenre == null,
                                onClick = { onIntent(HomeIntent.SelectGenre(null)) },
                            )
                        }
                        items(Genres.all, key = { it.apiName }) { genre ->
                            SonaChip(
                                text = genre.displayName,
                                selected = state.selectedGenre == genre.apiName,
                                onClick = { onIntent(HomeIntent.SelectGenre(genre.apiName)) },
                            )
                        }
                    }
                }

                // --- СКРОЛЛЯЩИЙСЯ КОНТЕНТ (Лента) ---
                val trendingTracks = remember(state.trending) { state.trending.take(8) }

                LazyColumn(
                    state = lazyListState, // Передаем стейт сюда!
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .nestedScroll(nestedScrollConnection),
                    contentPadding = PaddingValues(bottom = 26.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    if (state.isLoading && state.tracks.isEmpty() && !isFirstLaunch.value) {
                        item("loading") {
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                LoadingState()
                            }
                        }
                    } else if (state.error != null && state.tracks.isEmpty()) {
                        item("error") {
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                ErrorState(message = state.error.orEmpty(), onRetry = { onIntent(HomeIntent.Retry) })
                            }
                        }
                    } else if (state.tracks.isEmpty() && !state.isLoading) {
                        item("empty") {
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                EmptyState(
                                    title = "На циферблате ничего нет",
                                    message = "Попробуйте другой жанр или обновите ленту.",
                                    actionText = "Обновить",
                                    onAction = { onIntent(HomeIntent.Retry) },
                                )
                            }
                        }
                    }

                    val featured = state.featured
                    if (featured != null) {
                        item("featured") {
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                FeaturedTrackCard(
                                    track = featured,
                                    isPlaying = state.currentTrackId == featured.id && state.isPlaying,
                                    onClick = { onOpenTrack(featured.id) },
                                    onPlay = { onIntent(HomeIntent.PlayFeatured) },
                                )
                            }
                        }
                    }

                    item("explore") {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Surface(
                                onClick = onOpenExplore,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                                    Text(
                                        text = "Обзор Audius",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Плейлисты недели, андеграунд и новые имена открытой сети.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    item("charts") {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Surface(
                                onClick = onOpenCharts,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                                    Text(
                                        text = "Чарты Audius",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Топ недели, месяца и за всё время — с фильтром по жанрам.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    if (trendingTracks.isNotEmpty()) {
                        item("trending-header") {
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                val selectedGenre = Genres.byApiName(state.selectedGenre)
                                SectionHeader(
                                    title = if (selectedGenre == null) "Сейчас это в тренде"
                                    else "${selectedGenre.displayName}: самое горячее",
                                    action = "Увидеть все",
                                    onAction = {
                                        if (selectedGenre == null) {
                                            onOpenList("trending", "all", "Trending now")
                                        } else {
                                            onOpenList("genre", selectedGenre.apiName, selectedGenre.displayName)
                                        }
                                    },
                                )
                            }
                        }
                        items(trendingTracks, key = { it.id }) { track ->
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
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
                    }

                    if (state.freshDrops.isNotEmpty()) {
                        item("fresh-header") {
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                SectionHeader(title = "Свежие капли", action = "Исследовать") {
                                    onOpenList("trending", "all", "Fresh drops")
                                }
                            }
                        }
                        item("fresh-list") {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                SectionHeader(title = "Артисты, за которыми стоит наблюдать")
                            }
                        }
                        item("artists-list") {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(state.artists, key = { it.id }) { artist ->
                                    ArtistTile(
                                        artist = artist,
                                        onClick = { onOpenArtist(artist.id) },
                                    )
                                }
                            }
                        }
                    }

                    item("footer") {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "Работает от открытой сети Audius",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.alpha(0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}