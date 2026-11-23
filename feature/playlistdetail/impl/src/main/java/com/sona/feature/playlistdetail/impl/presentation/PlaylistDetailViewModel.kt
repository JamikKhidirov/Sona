package com.sona.feature.playlistdetail.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.library.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.library.ToggleFavorite
import com.sona.core.common.domain.usecase.music.GetPlaylist
import com.sona.core.common.domain.usecase.music.GetPlaylistFavorites
import com.sona.core.common.domain.usecase.music.GetPlaylistTracks
import com.sona.core.common.domain.usecase.music.TogglePlaylistFavorite
import com.sona.core.common.domain.usecase.settings.ObserveSettings
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

class PlaylistDetailViewModel @Inject constructor(
    private val getPlaylist: GetPlaylist,
    private val getPlaylistTracks: GetPlaylistTracks,
    private val getPlaylistFavorites: GetPlaylistFavorites,
    private val togglePlaylistFavorite: TogglePlaylistFavorite,
    private val observeFavoriteIds: ObserveFavoriteIds,
    private val toggleFavorite: ToggleFavorite,
    private val observeSettings: ObserveSettings,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistDetailUiState())
    val state: StateFlow<PlaylistDetailUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PlaylistDetailEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<PlaylistDetailEffect> = _effects.asSharedFlow()

    private var lastPlaylistId: String? = null
    private var loadJob: Job? = null

    init {
        observeFavoriteIds()
            .onEach { ids -> _state.update { it.copy(favoriteIds = ids) } }
            .launchIn(viewModelScope)

        observeSettings()
            .onEach { settings -> _state.update { it.copy(isSignedIn = settings.isSignedIn) } }
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

    fun onIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.Load -> {
                if (lastPlaylistId != intent.playlistId) {
                    lastPlaylistId = intent.playlistId
                    load(intent.playlistId)
                }
            }
            PlaylistDetailIntent.Retry -> lastPlaylistId?.let(::load)
            is PlaylistDetailIntent.PlayTrack -> play(intent.track)
            PlaylistDetailIntent.PlayAll -> {
                val tracks = _state.value.tracks
                if (tracks.isNotEmpty()) audioPlayer.playQueue(tracks, 0)
            }
            is PlaylistDetailIntent.ToggleTrackFavorite -> viewModelScope.launch {
                val wasFavorite = _state.value.favoriteIds.contains(intent.track.id)
                toggleFavorite(intent.track)
                _effects.emit(
                    PlaylistDetailEffect.Message(
                        if (wasFavorite) "Удалено из избранного" else "Сохранено в избранном",
                    ),
                )
            }
            PlaylistDetailIntent.TogglePlaylistFavorite -> togglePlaylistFavorite()
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

    private fun togglePlaylistFavorite() {
        val current = _state.value
        val playlistId = current.playlistId
        if (playlistId.isBlank() || current.isFavoriteLoading) return
        if (!current.isSignedIn) {
            viewModelScope.launch {
                _effects.emit(
                    PlaylistDetailEffect.Message(
                        "Добавьте токен Audius в настройках, чтобы сохранять плейлисты",
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isFavoriteLoading = true) }
            togglePlaylistFavorite(playlistId, current.isPlaylistFavorite)
                .onSuccess { isFavorite ->
                    _state.update {
                        it.copy(isPlaylistFavorite = isFavorite, isFavoriteLoading = false)
                    }
                    _effects.emit(
                        PlaylistDetailEffect.Message(
                            if (isFavorite) "Плейлист сохранён" else "Плейлист удалён из библиотеки",
                        ),
                    )
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isFavoriteLoading = false) }
                    _effects.emit(
                        PlaylistDetailEffect.Message(
                            throwable.message ?: "Не удалось сохранить плейлист",
                        ),
                    )
                }
        }
    }

    private fun load(playlistId: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(playlistId = playlistId, isLoading = true, error = null)
            }
            supervisorScope {
                val playlistDeferred = async { getPlaylist(playlistId) }
                val tracksDeferred = async { getPlaylistTracks(playlistId) }
                val favoritesDeferred = async { getPlaylistFavorites(playlistId) }

                val playlistResult = playlistDeferred.await()
                val tracksResult = tracksDeferred.await()
                val favoritesResult = favoritesDeferred.await()

                playlistResult
                    .onSuccess { playlist ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                playlist = playlist,
                                tracks = tracksResult.getOrDefault(emptyList()),
                                favoritedBy = favoritesResult.getOrDefault(emptyList()),
                                error = null,
                            )
                        }
                    }
                    .onFailure { throwable ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                playlist = null,
                                tracks = emptyList(),
                                favoritedBy = emptyList(),
                                error = throwable.message ?: "Не удалось загрузить плейлист.",
                            )
                        }
                    }
            }
        }
    }
}
