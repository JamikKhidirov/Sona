package com.sona.feature.tracktools.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.library.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.library.ToggleFavorite
import com.sona.core.common.domain.usecase.music.GetTrack
import com.sona.core.common.domain.usecase.music.GetTrackRemixes
import com.sona.core.common.domain.usecase.music.GetTrackStems
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

class TrackToolsViewModel @Inject constructor(
    private val getTrack: GetTrack,
    private val getTrackRemixes: GetTrackRemixes,
    private val getTrackStems: GetTrackStems,
    private val observeFavoriteIds: ObserveFavoriteIds,
    private val toggleFavorite: ToggleFavorite,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(TrackToolsUiState())
    val state: StateFlow<TrackToolsUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TrackToolsEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<TrackToolsEffect> = _effects.asSharedFlow()

    private var lastTrackId: String? = null
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
    }

    fun onIntent(intent: TrackToolsIntent) {
        when (intent) {
            is TrackToolsIntent.Load -> {
                if (lastTrackId != intent.trackId) {
                    lastTrackId = intent.trackId
                    load(intent.trackId)
                }
            }
            TrackToolsIntent.Retry -> lastTrackId?.let(::load)
            is TrackToolsIntent.SelectTab -> _state.update { it.copy(tab = intent.tab) }
            is TrackToolsIntent.PlayTrack -> play(intent.track)
            is TrackToolsIntent.ToggleFavorite -> viewModelScope.launch {
                val wasFavorite = _state.value.favoriteIds.contains(intent.track.id)
                toggleFavorite(intent.track)
                _effects.emit(
                    TrackToolsEffect.Message(
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
        val queue = _state.value.items.ifEmpty { listOf(track) }
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        audioPlayer.playQueue(queue, index)
    }

    private fun load(trackId: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(trackId = trackId, isLoading = true, error = null) }
            supervisorScope {
                val trackDeferred = async { getTrack(trackId) }
                val remixesDeferred = async { getTrackRemixes(trackId) }
                val stemsDeferred = async { getTrackStems(trackId) }

                val trackResult = trackDeferred.await()
                val remixesResult = remixesDeferred.await()
                val stemsResult = stemsDeferred.await()

                val allFailed = trackResult.isFailure &&
                    remixesResult.isFailure &&
                    stemsResult.isFailure

                _state.update {
                    it.copy(
                        isLoading = false,
                        track = trackResult.getOrNull() ?: it.track,
                        remixes = remixesResult.getOrDefault(it.remixes),
                        stems = stemsResult.getOrDefault(it.stems),
                        error = if (allFailed) {
                            remixesResult.exceptionOrNull()?.message
                                ?: stemsResult.exceptionOrNull()?.message
                                ?: "Не удалось загрузить ремиксы и стемы."
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}
