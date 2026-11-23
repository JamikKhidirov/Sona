package com.sona.feature.playlistdetail.impl.presentation

import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.Track

data class PlaylistDetailUiState(
    val playlistId: String = "",
    val isLoading: Boolean = true,
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val favoritedBy: List<Artist> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isPlaylistFavorite: Boolean = false,
    val isFavoriteLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val error: String? = null,
) {
    val hasContent: Boolean get() = playlist != null
    val totalDurationSeconds: Int get() = tracks.sumOf { it.durationSeconds }
}

sealed interface PlaylistDetailIntent {
    data class Load(val playlistId: String) : PlaylistDetailIntent
    data object Retry : PlaylistDetailIntent
    data class PlayTrack(val track: Track) : PlaylistDetailIntent
    data object PlayAll : PlaylistDetailIntent
    data class ToggleTrackFavorite(val track: Track) : PlaylistDetailIntent
    data object TogglePlaylistFavorite : PlaylistDetailIntent
}

sealed interface PlaylistDetailEffect {
    data class Message(val text: String) : PlaylistDetailEffect
}
