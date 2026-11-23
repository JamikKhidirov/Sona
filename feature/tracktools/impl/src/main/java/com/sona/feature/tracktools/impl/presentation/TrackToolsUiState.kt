package com.sona.feature.tracktools.impl.presentation

import com.sona.core.common.model.Track

enum class TrackToolsTab(val label: String) {
    REMIXES("Ремиксы"),
    STEMS("Стемы"),
}

data class TrackToolsUiState(
    val trackId: String = "",
    val track: Track? = null,
    val tab: TrackToolsTab = TrackToolsTab.REMIXES,
    val remixes: List<Track> = emptyList(),
    val stems: List<Track> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    val items: List<Track>
        get() = when (tab) {
            TrackToolsTab.REMIXES -> remixes
            TrackToolsTab.STEMS -> stems
        }
}

sealed interface TrackToolsIntent {
    data class Load(val trackId: String) : TrackToolsIntent
    data object Retry : TrackToolsIntent
    data class SelectTab(val tab: TrackToolsTab) : TrackToolsIntent
    data class PlayTrack(val track: Track) : TrackToolsIntent
    data class ToggleFavorite(val track: Track) : TrackToolsIntent
}

sealed interface TrackToolsEffect {
    data class Message(val text: String) : TrackToolsEffect
}
