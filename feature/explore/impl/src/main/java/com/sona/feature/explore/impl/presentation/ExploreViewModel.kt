package com.sona.feature.explore.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.library.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.library.ToggleFavorite
import com.sona.core.common.domain.usecase.music.GetTrendingPlaylists
import com.sona.core.common.domain.usecase.music.GetUndergroundTracks
import com.sona.core.common.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

class ExploreViewModel @Inject constructor(
    private val getTrendingPlaylists: GetTrendingPlaylists,
    private val getUndergroundTracks: GetUndergroundTracks,
    private val observeFavoriteIds: ObserveFavoriteIds,
    private val toggleFavorite: ToggleFavorite,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreUiState())
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ExploreEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<ExploreEffect> = _effects.asSharedFlow()

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

    fun onIntent(intent: ExploreIntent) {
        when (intent) {
            ExploreIntent.Load -> load()
            ExploreIntent.Retry -> load()
            is ExploreIntent.SelectTab -> _state.update { it.copy(tab = intent.tab) }
            is ExploreIntent.PlayTrack -> play(intent.track)
            is ExploreIntent.ToggleFavorite -> viewModelScope.launch {
                val wasFavorite = _state.value.favoriteIds.contains(intent.track.id)
                toggleFavorite(intent.track)
                _effects.emit(
                    ExploreEffect.Message(
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
        val queue = _state.value.underground.ifEmpty { listOf(track) }
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        audioPlayer.playQueue(queue, index)
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            supervisorScope {
                val playlistsDeferred = async { getTrendingPlaylists(limit = 25) }
                val undergroundDeferred = async { getUndergroundTracks(limit = 50) }

                val playlistsResult = playlistsDeferred.await()
                val undergroundResult = undergroundDeferred.await()

                if (playlistsResult.isFailure && undergroundResult.isFailure) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = playlistsResult.exceptionOrNull()?.message
                                ?: undergroundResult.exceptionOrNull()?.message
                                ?: "Не удалось загрузить обзор.",
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            playlists = playlistsResult.getOrDefault(emptyList()),
                            underground = undergroundResult.getOrDefault(emptyList()),
                            error = null,
                        )
                    }
                }
            }
        }
    }
}
