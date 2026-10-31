package com.sona.feature.musiclist.impl.presentation

import com.sona.core.common.model.Track

data class MusicListUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val source: String = "trending",
    val tracks: List<Track> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val error: String? = null,
) {
    val totalDurationSeconds: Int get() = tracks.sumOf { it.durationSeconds }
    val heroArtwork: String? get() = tracks.firstOrNull()?.artworkUrl
}

sealed interface MusicListIntent {
    data class Load(val source: String, val id: String, val title: String) : MusicListIntent
    data object Retry : MusicListIntent
    data object PlayAll : MusicListIntent
    data class PlayTrack(val track: Track) : MusicListIntent
    data class ToggleFavorite(val track: Track) : MusicListIntent
}

sealed interface MusicListEffect {
    data class Message(val text: String) : MusicListEffect
}
