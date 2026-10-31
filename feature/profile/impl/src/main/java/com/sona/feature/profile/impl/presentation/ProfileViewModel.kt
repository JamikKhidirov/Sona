package com.sona.feature.profile.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.common.domain.usecase.GetCurrentUser
import com.sona.core.common.domain.usecase.ObserveFavorites
import com.sona.core.common.domain.usecase.ObserveRecentlyPlayed
import com.sona.core.common.domain.usecase.ObserveSettings
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val getCurrentUser: GetCurrentUser,
    private val observeFavorites: ObserveFavorites,
    private val observeRecentlyPlayed: ObserveRecentlyPlayed,
    private val observeSettings: ObserveSettings,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(ProfileUiState())
    val state: kotlinx.coroutines.flow.StateFlow<ProfileUiState> = _state

    private var loadedForToken: Boolean = false

    init {
        observeFavorites()
            .onEach { tracks ->
                _state.update { it.copy(isLoading = false, favorites = tracks) }
            }
            .launchIn(viewModelScope)

        observeRecentlyPlayed()
            .onEach { tracks -> _state.update { it.copy(recentlyPlayed = tracks) } }
            .launchIn(viewModelScope)

        observeSettings()
            .onEach { settings ->
                _state.update {
                    it.copy(
                        isSignedIn = settings.isSignedIn,
                        isLoading = if (!settings.isSignedIn) false else it.isLoading,
                    )
                }
                if (settings.isSignedIn && !loadedForToken) {
                    loadedForToken = true
                    loadUser()
                }
                if (!settings.isSignedIn) {
                    loadedForToken = false
                    _state.update { it.copy(user = null) }
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

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.PlayTrack -> {
                if (_state.value.currentTrackId == intent.track.id) {
                    audioPlayer.togglePlayPause()
                } else {
                    val queue = _state.value.recentlyPlayed.ifEmpty {
                        _state.value.favorites.ifEmpty { listOf(intent.track) }
                    }
                    val index = queue.indexOfFirst { it.id == intent.track.id }.coerceAtLeast(0)
                    audioPlayer.playQueue(queue, index)
                }
            }
            ProfileIntent.Refresh -> loadUser()
        }
    }

    private fun loadUser() {
        if (!_state.value.isSignedIn) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getCurrentUser()
                .onSuccess { user ->
                    _state.update { it.copy(isLoading = false, user = user) }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Couldn't load your Audius profile.",
                        )
                    }
                }
        }
    }
}
