package com.sona.feature.home.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.GetTrendingTracks
import com.sona.core.common.domain.usecase.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.ToggleFavorite
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

class HomeViewModel @Inject constructor(
    private val getTrendingTracks: GetTrendingTracks,
    private val observeFavoriteIds: ObserveFavoriteIds,
    private val toggleFavorite: ToggleFavorite,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HomeEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

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

        load(genre = null)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SelectGenre -> {
                val next = if (_state.value.selectedGenre == intent.genre) null else intent.genre
                _state.update { it.copy(selectedGenre = next) }
                load(next)
            }
            is HomeIntent.PlayTrack -> play(intent.track)
            is HomeIntent.ToggleFavorite -> viewModelScope.launch {
                val wasFavorite = _state.value.favoriteIds.contains(intent.track.id)
                toggleFavorite(intent.track)
                _effects.emit(
                    HomeEffect.Message(
                        if (wasFavorite) "Removed from favorites" else "Saved to favorites",
                    ),
                )
            }
            HomeIntent.PlayFeatured -> _state.value.featured?.let(::play)
            HomeIntent.Retry -> load(_state.value.selectedGenre)
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

    private fun load(genre: String?) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getTrendingTracks(genre = genre, limit = 50)
                .onSuccess { tracks ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            tracks = tracks,
                            error = if (tracks.isEmpty()) {
                                "No tracks found for this vibe yet."
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
                            error = throwable.message ?: "Couldn't reach Audius. Check your connection.",
                        )
                    }
                }
        }
    }
}
