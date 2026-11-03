package com.sona.feature.detailmusic.impl.presentation

import com.sona.core.common.model.Track

data class DetailMusicUiState(
    val isLoading: Boolean = true,
    val track: Track? = null,
    val related: List<Track> = emptyList(),
    val remixes: List<Track> = emptyList(),
    val isFavorite: Boolean = false,
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val error: String? = null,
) {
    val isCurrent: Boolean get() = track != null && track.id == currentTrackId
}



