package com.sona.feature.home.impl.presentation

import com.sona.core.common.model.Track

data class HomeUiState(
    val isLoading: Boolean = true,
    val tracks: List<Track> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val selectedGenre: String? = null,
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val error: String? = null,
) {
    val featured: Track? get() = tracks.firstOrNull()
    val trending: List<Track> get() = tracks.drop(1)
    val freshDrops: List<Track> get() = tracks.drop(1).take(10)
    val artists get() = tracks.map { it.artist }.distinctBy { it.id }.take(14)
}

sealed interface HomeIntent {
    data class SelectGenre(val genre: String?) : HomeIntent
    data class PlayTrack(val track: Track) : HomeIntent
    data class ToggleFavorite(val track: Track) : HomeIntent
    data object PlayFeatured : HomeIntent
    data object Retry : HomeIntent
}

sealed interface HomeEffect {
    data class Message(val text: String) : HomeEffect
}
