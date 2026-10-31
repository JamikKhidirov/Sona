package com.sona.feature.favorites.impl.presentation

import com.sona.core.common.model.Track

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val tracks: List<Track> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
) {
    val totalDurationSeconds: Int get() = tracks.sumOf { it.durationSeconds }
}

sealed interface FavoritesIntent {
    data object PlayAll : FavoritesIntent
    data class PlayTrack(val track: Track) : FavoritesIntent
    data class ToggleFavorite(val track: Track) : FavoritesIntent
    data object ClearFavorites : FavoritesIntent
}

sealed interface FavoritesEffect {
    data class Message(val text: String) : FavoritesEffect
}
