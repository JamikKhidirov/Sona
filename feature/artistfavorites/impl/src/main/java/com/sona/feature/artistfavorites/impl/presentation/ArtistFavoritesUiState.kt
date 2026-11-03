package com.sona.feature.artistfavorites.impl.presentation

import com.sona.core.common.model.Artist
import com.sona.core.common.model.Track

data class ArtistFavoritesUiState(
    val artistId: String = "",
    val artist: Artist? = null,
    val tracks: List<Track> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    val totalDurationSeconds: Int get() = tracks.sumOf { it.durationSeconds }
}

sealed interface ArtistFavoritesIntent {
    data class Load(val artistId: String) : ArtistFavoritesIntent
    data object Retry : ArtistFavoritesIntent
    data class PlayTrack(val track: Track) : ArtistFavoritesIntent
    data object PlayAll : ArtistFavoritesIntent
    data class ToggleFavorite(val track: Track) : ArtistFavoritesIntent
}

sealed interface ArtistFavoritesEffect {
    data class Message(val text: String) : ArtistFavoritesEffect
}
