package com.sona.feature.detailmusic.impl.presentation

import com.sona.core.common.model.Track

data class DetailMusicUiState(
    val isLoading: Boolean = true,
    val track: Track? = null,
    val related: List<Track> = emptyList(),
    val isFavorite: Boolean = false,
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val error: String? = null,
) {
    val isCurrent: Boolean get() = track != null && track.id == currentTrackId
}

sealed interface DetailMusicIntent {
    data class Load(val trackId: String) : DetailMusicIntent
    data class Play(val track: Track) : DetailMusicIntent
    data class ToggleFavorite(val track: Track) : DetailMusicIntent
    data object Retry : DetailMusicIntent
}

sealed interface DetailMusicEffect {
    data class Message(val text: String) : DetailMusicEffect
}
