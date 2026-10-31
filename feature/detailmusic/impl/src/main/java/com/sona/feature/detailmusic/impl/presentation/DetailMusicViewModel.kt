package com.sona.feature.detailmusic.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.GetTrack
import com.sona.core.common.domain.usecase.GetTrendingTracks
import com.sona.core.common.domain.usecase.ObserveFavorites
import com.sona.core.common.domain.usecase.ToggleFavorite
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

class DetailMusicViewModel @Inject constructor(
    private val getTrack: GetTrack,
    private val getTrendingTracks: GetTrendingTracks,
    private val observeFavorites: ObserveFavorites,
    private val toggleFavorite: ToggleFavorite,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailMusicUiState())
    val state: StateFlow<DetailMusicUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<DetailMusicEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<DetailMusicEffect> = _effects.asSharedFlow()

    private var lastTrackId: String? = null
    private var loadJob: Job? = null

    init {
        observeFavorites()
            .onEach { favorites ->
                _state.update { current ->
                    current.copy(
                        isFavorite = current.track?.let { track ->
                            favorites.any { it.id == track.id }
                        } ?: false,
                    )
                }
            }
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

    fun onIntent(intent: DetailMusicIntent) {
        when (intent) {
            is DetailMusicIntent.Load -> {
                if (lastTrackId != intent.trackId) {
                    lastTrackId = intent.trackId
                    load(intent.trackId)
                }
            }
            is DetailMusicIntent.Play -> {
                if (_state.value.currentTrackId == intent.track.id) {
                    audioPlayer.togglePlayPause()
                } else {
                    val track = _state.value.track ?: intent.track
                    val queue = listOf(track) + _state.value.related.filterNot { it.id == track.id }
                    audioPlayer.playQueue(queue, 0)
                }
            }
            is DetailMusicIntent.ToggleFavorite -> viewModelScope.launch {
                val wasFavorite = _state.value.isFavorite
                toggleFavorite(intent.track)
                _effects.emit(
                    DetailMusicEffect.Message(
                        if (wasFavorite) "Removed from favorites" else "Saved to favorites",
                    ),
                )
            }
            DetailMusicIntent.Retry -> lastTrackId?.let(::load)
        }
    }

    private fun load(trackId: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getTrack(trackId)
                .onSuccess { track ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            track = track,
                            error = null,
                        )
                    }
                    loadRelated(track)
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Couldn't load this track.",
                        )
                    }
                }
        }
    }

    private suspend fun loadRelated(track: com.sona.core.common.model.Track) {
        getTrendingTracks(genre = track.genre, limit = 12)
            .onSuccess { tracks ->
                _state.update {
                    it.copy(related = tracks.filterNot { related -> related.id == track.id }.take(10))
                }
            }
    }
}
