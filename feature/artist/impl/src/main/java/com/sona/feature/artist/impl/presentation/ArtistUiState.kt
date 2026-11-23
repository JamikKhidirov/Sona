package com.sona.feature.artist.impl.presentation

import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.Track

data class ArtistUiState(
    val artistId: String = "",
    val isLoading: Boolean = true,
    val artist: Artist? = null,
    val tracks: List<Track> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val related: List<Artist> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val isSignedIn: Boolean = false,
    val isFollowing: Boolean = false,
    val isFollowLoading: Boolean = false,
    val error: String? = null,
) {
    val hasContent: Boolean get() = artist != null
    val totalDurationSeconds: Int get() = tracks.sumOf { it.durationSeconds }
}

sealed interface ArtistIntent {
    data class Load(val artistId: String) : ArtistIntent
    data object Retry : ArtistIntent
    data class PlayTrack(val track: Track) : ArtistIntent
    data object PlayAll : ArtistIntent
    data class ToggleFavorite(val track: Track) : ArtistIntent
    data object ToggleFollow : ArtistIntent
}

sealed interface ArtistEffect {
    data class Message(val text: String) : ArtistEffect
}
