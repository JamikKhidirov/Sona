package com.sona.feature.artistfavorites.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.library.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.library.ToggleFavorite
import com.sona.core.common.domain.usecase.music.GetArtist
import com.sona.core.common.domain.usecase.music.GetArtistFavorites
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

class ArtistFavoritesViewModel @Inject constructor(
    private val getArtist: GetArtist,
    private val getArtistFavorites: GetArtistFavorites,
    private val observeFavoriteIds: ObserveFavoriteIds,
    private val toggleFavorite: ToggleFavorite,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(ArtistFavoritesUiState())
    val state: StateFlow<ArtistFavoritesUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ArtistFavoritesEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<ArtistFavoritesEffect> = _effects.asSharedFlow()

    private var lastArtistId: String? = null
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

    fun onIntent(intent: ArtistFavoritesIntent) {
        when (intent) {
            is ArtistFavoritesIntent.Load -> {
                if (lastArtistId != intent.artistId) {
                    lastArtistId = intent.artistId
                    load(intent.artistId)
                }
            }
            ArtistFavoritesIntent.Retry -> lastArtistId?.let(::load)
            is ArtistFavoritesIntent.PlayTrack -> play(intent.track)
            ArtistFavoritesIntent.PlayAll -> {
                val tracks = _state.value.tracks
                if (tracks.isNotEmpty()) audioPlayer.playQueue(tracks, 0)
            }
            is ArtistFavoritesIntent.ToggleFavorite -> viewModelScope.launch {
                val wasFavorite = _state.value.favoriteIds.contains(intent.track.id)
                toggleFavorite(intent.track)
                _effects.emit(
                    ArtistFavoritesEffect.Message(
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

    private fun load(artistId: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(artistId = artistId, isLoading = true, error = null) }
            supervisorScope {
                val artistDeferred = async { getArtist(artistId) }
                val tracksDeferred = async { getArtistFavorites(artistId) }

                val artistResult = artistDeferred.await()
                val tracksResult = tracksDeferred.await()

                if (tracksResult.isFailure && artistResult.isFailure) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            tracks = emptyList(),
                            error = tracksResult.exceptionOrNull()?.message
                                ?: "Не удалось загрузить избранное артиста.",
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            artist = artistResult.getOrNull() ?: it.artist,
                            tracks = tracksResult.getOrDefault(emptyList()),
                            error = null,
                        )
                    }
                }
            }
        }
    }
}
