package com.sona.feature.artist.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.library.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.library.ToggleFavorite
import com.sona.core.common.domain.usecase.music.GetArtistCatalog
import com.sona.core.common.domain.usecase.music.ToggleFollow
import com.sona.core.common.domain.usecase.settings.ObserveSettings
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

class ArtistViewModel @Inject constructor(
    private val getArtistCatalog: GetArtistCatalog,
    private val toggleFollow: ToggleFollow,
    private val observeFavoriteIds: ObserveFavoriteIds,
    private val toggleFavorite: ToggleFavorite,
    private val observeSettings: ObserveSettings,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(ArtistUiState())
    val state: StateFlow<ArtistUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ArtistEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<ArtistEffect> = _effects.asSharedFlow()

    private var lastArtistId: String? = null
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

    fun onIntent(intent: ArtistIntent) {
        when (intent) {
            is ArtistIntent.Load -> {
                if (lastArtistId != intent.artistId) {
                    lastArtistId = intent.artistId
                    load(intent.artistId)
                }
            }
            ArtistIntent.Retry -> lastArtistId?.let(::load)
            is ArtistIntent.PlayTrack -> play(intent.track)
            ArtistIntent.PlayAll -> {
                val tracks = _state.value.tracks
                if (tracks.isNotEmpty()) audioPlayer.playQueue(tracks, 0)
            }
            is ArtistIntent.ToggleFavorite -> viewModelScope.launch {
                val wasFavorite = _state.value.favoriteIds.contains(intent.track.id)
                toggleFavorite(intent.track)
                _effects.emit(
                    ArtistEffect.Message(
                        if (wasFavorite) "Удалено из избранного" else "Сохранено в избранном",
                    ),
                )
            }
            ArtistIntent.ToggleFollow -> follow()
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

    private fun follow() {
        val current = _state.value
        val artistId = current.artistId
        if (artistId.isBlank() || current.isFollowLoading) return
        if (!current.isSignedIn) {
            viewModelScope.launch {
                _effects.emit(
                    ArtistEffect.Message("Добавьте токен Audius в настройках, чтобы подписаться"),
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isFollowLoading = true) }
            toggleFollow(artistId, current.isFollowing)
                .onSuccess { isFollowing ->
                    _state.update { it.copy(isFollowing = isFollowing, isFollowLoading = false) }
                    _effects.emit(
                        ArtistEffect.Message(
                            if (isFollowing) "Вы подписались" else "Вы отписались",
                        ),
                    )
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isFollowLoading = false) }
                    _effects.emit(
                        ArtistEffect.Message(
                            throwable.message ?: "Не удалось изменить подписку",
                        ),
                    )
                }
        }
    }

    private fun load(artistId: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(artistId = artistId, isLoading = true, error = null) }
            getArtistCatalog(artistId)
                .onSuccess { catalog ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            artist = catalog.artist,
                            tracks = catalog.tracks,
                            playlists = catalog.playlists,
                            related = catalog.related,
                            error = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Не удалось загрузить артиста.",
                        )
                    }
                }
        }
    }
}
