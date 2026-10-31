package com.sona.feature.musiclist.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.GetArtistTracks
import com.sona.core.common.domain.usecase.GetPlaylistTracks
import com.sona.core.common.domain.usecase.GetTrendingTracks
import com.sona.core.common.domain.usecase.ObserveFavoriteIds
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

class MusicListViewModel @Inject constructor(
    private val getTrendingTracks: GetTrendingTracks,
    private val getArtistTracks: GetArtistTracks,
    private val getPlaylistTracks: GetPlaylistTracks,
    private val observeFavoriteIds: ObserveFavoriteIds,
    private val toggleFavorite: ToggleFavorite,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(MusicListUiState())
    val state: StateFlow<MusicListUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MusicListEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<MusicListEffect> = _effects.asSharedFlow()

    private var lastKey: String? = null
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

    fun onIntent(intent: MusicListIntent) {
        when (intent) {
            is MusicListIntent.Load -> {
                val key = "${intent.source}:${intent.id}"
                if (key != lastKey) {
                    lastKey = key
                    load(intent.source, intent.id, intent.title)
                }
            }
            MusicListIntent.Retry -> {
                val key = lastKey ?: return
                val parts = key.split(":", limit = 2)
                load(parts[0], parts.getOrElse(1) { "" }, _state.value.title)
            }
            MusicListIntent.PlayAll -> {
                if (_state.value.tracks.isNotEmpty()) {
                    audioPlayer.playQueue(_state.value.tracks, 0)
                }
            }
            is MusicListIntent.PlayTrack -> {
                if (_state.value.currentTrackId == intent.track.id) {
                    audioPlayer.togglePlayPause()
                } else {
                    val index = _state.value.tracks.indexOfFirst { it.id == intent.track.id }
                        .coerceAtLeast(0)
                    audioPlayer.playQueue(_state.value.tracks, index)
                }
            }
            is MusicListIntent.ToggleFavorite -> viewModelScope.launch {
                val wasFavorite = _state.value.favoriteIds.contains(intent.track.id)
                toggleFavorite(intent.track)
                _effects.emit(
                    MusicListEffect.Message(
                        if (wasFavorite) "Removed from favorites" else "Saved to favorites",
                    ),
                )
            }
        }
    }

    private fun load(source: String, id: String, title: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    title = title,
                    source = source,
                    error = null,
                    tracks = emptyList(),
                )
            }

            val result = when (source) {
                "trending" -> getTrendingTracks(genre = null, limit = 100)
                "genre" -> getTrendingTracks(genre = id, limit = 100)
                "artist" -> getArtistTracks(id)
                "playlist" -> getPlaylistTracks(id)
                else -> getTrendingTracks(genre = null, limit = 100)
            }

            result
                .onSuccess { tracks ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            tracks = tracks,
                            error = if (tracks.isEmpty()) "Nothing here yet." else null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Couldn't load this collection.",
                        )
                    }
                }
        }
    }
}
