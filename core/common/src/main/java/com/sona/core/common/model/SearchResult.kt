package com.sona.core.common.model

data class SearchResult(
    val tracks: List<Track> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
) {
    val isEmpty: Boolean get() = tracks.isEmpty() && artists.isEmpty() && playlists.isEmpty()
}
