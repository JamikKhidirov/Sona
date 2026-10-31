package com.sona.feature.searchmusic.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.SearchCatalog
import com.sona.core.common.domain.usecase.ToggleFavorite
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val searchCatalog: SearchCatalog,
    private val observeFavoriteIds: ObserveFavoriteIds,
    private val toggleFavorite: ToggleFavorite,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SearchEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<SearchEffect> = _effects.asSharedFlow()

    private val queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

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

        queryFlow
            .map(String::trim)
            .debounce(350L)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.length < 2) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            tracks = emptyList(),
                            artists = emptyList(),
                            playlists = emptyList(),
                            error = null,
                        )
                    }
                } else {
                    performSearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> {
                _state.update { it.copy(query = intent.query) }
                queryFlow.value = intent.query
            }
            is SearchIntent.TabSelected -> _state.update { it.copy(selectedTab = intent.tab) }
            is SearchIntent.PlayTrack -> play(intent.track)
            is SearchIntent.ToggleFavorite -> viewModelScope.launch {
                val wasFavorite = _state.value.favoriteIds.contains(intent.track.id)
                toggleFavorite(intent.track)
                _effects.emit(
                    SearchEffect.Message(
                        if (wasFavorite) "Removed from favorites" else "Saved to favorites",
                    ),
                )
            }
            is SearchIntent.BrowseGenre -> {
                _state.update { it.copy(query = intent.genre) }
                queryFlow.value = intent.genre
            }
            SearchIntent.Retry -> performSearch(_state.value.query.trim())
            SearchIntent.ClearQuery -> {
                _state.update { it.copy(query = "") }
                queryFlow.value = ""
            }
        }
    }

    private fun play(track: com.sona.core.common.model.Track) {
        if (_state.value.currentTrackId == track.id) {
            audioPlayer.togglePlayPause()
            return
        }
        val queue = _state.value.tracks.ifEmpty { listOf(track) }
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        audioPlayer.playQueue(queue, index)
    }

    private fun performSearch(query: String) {
        if (query.length < 2) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            searchCatalog(query = query, limit = 25)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            tracks = result.tracks,
                            artists = result.artists,
                            playlists = result.playlists,
                            error = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Search failed. Try again.",
                        )
                    }
                }
        }
    }
}
