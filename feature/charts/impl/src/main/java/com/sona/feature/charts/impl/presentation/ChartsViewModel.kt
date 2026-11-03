package com.sona.feature.charts.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.library.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.library.ToggleFavorite
import com.sona.core.common.domain.usecase.music.GetTrendingTracks
import com.sona.core.common.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChartsViewModel @Inject constructor(
    private val getTrendingTracks: GetTrendingTracks,
    private val observeFavoriteIds: ObserveFavoriteIds,
    private val toggleFavorite: ToggleFavorite,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(ChartsUiState())
    val state: StateFlow<ChartsUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ChartsEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<ChartsEffect> = _effects.asSharedFlow()

    private var loadJob: Job? = null

    init {
        observeFavoriteIds()
            .onEach { ids -> _state.update { it.copy(favoriteIds = ids) } }
            .launchIn(viewModelScope)

        audioPlayer.state
            .onEach { playback ->
                _state.update {
                    it.copy(
                        currentTrackId = playback.currentTrack?.id,
                        isPlaying = playback.isPlaying,
                    )
                }
            }
            .launchIn(viewModelScope)

        load()
    }

    fun onIntent(intent: ChartsIntent) {
        when (intent) {
            ChartsIntent.Load, ChartsIntent.Retry -> load()
            is ChartsIntent.SelectPeriod -> {
                if (_state.value.period == intent.period) return
                _state.update { it.copy(period = intent.period) }
                load()
            }
            is ChartsIntent.SelectGenre -> {
                if (_state.value.genre == intent.genre) return
                _state.update { it.copy(genre = intent.genre) }
                load()
            }
            is ChartsIntent.PlayTrack -> play(intent.track)
            is ChartsIntent.ToggleFavorite -> viewModelScope.launch {
                val wasFavorite = _state.value.favoriteIds.contains(intent.track.id)
                toggleFavorite(intent.track)
                _effects.emit(
                    ChartsEffect.Message(
                        if (wasFavorite) "Удалено из избранного" else "Сохранено в избранном",
                    ),
                )
            }
        }
    }

    private fun play(track: Track) {
        if (_state.value.currentTrackId == track.id) {
            audioPlayer.togglePlayPause()
            return
        }
        val queue = _state.value.tracks.ifEmpty { listOf(track) }
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        audioPlayer.playQueue(queue, index)
    }

    private fun load() {
        loadJob?.cancel()
        val snapshot = _state.value
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getTrendingTracks(
                genre = snapshot.genre,
                time = snapshot.period.apiValue,
                limit = 100,
            )
                .onSuccess { tracks ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            tracks = tracks,
                            error = if (tracks.isEmpty()) {
                                "В этом чарте пока нет треков. Попробуйте другой период."
                            } else {
                                null
                            },
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            tracks = emptyList(),
                            error = throwable.message ?: "Не удалось загрузить чарты.",
                        )
                    }
                }
        }
    }
}
