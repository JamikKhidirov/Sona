package com.sona.feature.favorites.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.ClearLibrary
import com.sona.core.common.domain.usecase.ObserveFavorites
import com.sona.core.common.domain.usecase.ToggleFavorite
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

class FavoritesViewModel @Inject constructor(
    private val observeFavorites: ObserveFavorites,
    private val toggleFavorite: ToggleFavorite,
    private val clearLibrary: ClearLibrary,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<FavoritesEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<FavoritesEffect> = _effects.asSharedFlow()

    init {
        observeFavorites()
            .onEach { tracks ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        tracks = tracks,
                        favoriteIds = tracks.mapTo(LinkedHashSet()) { track -> track.id },
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

    fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            FavoritesIntent.PlayAll -> {
                val tracks = _state.value.tracks
                if (tracks.isNotEmpty()) audioPlayer.playQueue(tracks, 0)
            }
            is FavoritesIntent.PlayTrack -> {
                if (_state.value.currentTrackId == intent.track.id) {
                    audioPlayer.togglePlayPause()
                } else {
                    val index = _state.value.tracks.indexOfFirst { it.id == intent.track.id }
                        .coerceAtLeast(0)
                    audioPlayer.playQueue(_state.value.tracks, index)
                }
            }
            is FavoritesIntent.ToggleFavorite -> viewModelScope.launch {
                toggleFavorite(intent.track)
                _effects.emit(FavoritesEffect.Message("Removed from favorites"))
            }
            FavoritesIntent.ClearFavorites -> viewModelScope.launch {
                clearLibrary()
                _effects.emit(FavoritesEffect.Message("Library cleared"))
            }
        }
    }
}
