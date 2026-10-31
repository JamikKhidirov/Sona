package com.sona.feature.player.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.audio.AudioPlayer
import com.sona.core.audio.PlayerState
import com.sona.core.common.domain.usecase.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.ToggleFavorite
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val player: PlayerState = PlayerState(),
    val favoriteIds: Set<String> = emptySet(),
) {
    val track get() = player.currentTrack
    val isFavorite: Boolean get() = track?.id?.let { favoriteIds.contains(it) } == true
}

sealed interface PlayerIntent {
    data object TogglePlayPause : PlayerIntent
    data object Next : PlayerIntent
    data object Previous : PlayerIntent
    data class SeekTo(val positionMs: Long) : PlayerIntent
    data object ToggleShuffle : PlayerIntent
    data object CycleRepeat : PlayerIntent
    data class SelectQueueItem(val index: Int) : PlayerIntent
    data object ToggleFavorite : PlayerIntent
}

class PlayerViewModel @Inject constructor(
    private val audioPlayer: AudioPlayer,
    private val observeFavoriteIds: ObserveFavoriteIds,
    private val toggleFavorite: ToggleFavorite,
) : ViewModel() {

    val state: StateFlow<PlayerUiState> = combine(
        audioPlayer.state,
        observeFavoriteIds(),
    ) { playback, favoriteIds ->
        PlayerUiState(player = playback, favoriteIds = favoriteIds)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = PlayerUiState(
            player = audioPlayer.state.value,
            favoriteIds = emptySet(),
        ),
    )

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.TogglePlayPause -> audioPlayer.togglePlayPause()
            PlayerIntent.Next -> audioPlayer.next()
            PlayerIntent.Previous -> audioPlayer.previous()
            is PlayerIntent.SeekTo -> audioPlayer.seekTo(intent.positionMs)
            PlayerIntent.ToggleShuffle -> audioPlayer.setShuffle(!state.value.player.shuffle)
            PlayerIntent.CycleRepeat -> audioPlayer.cycleRepeatMode()
            is PlayerIntent.SelectQueueItem -> audioPlayer.selectQueueItem(intent.index)
            PlayerIntent.ToggleFavorite -> {
                val track = state.value.track ?: return
                viewModelScope.launch { toggleFavorite(track) }
            }
        }
    }
}
