package com.sona.core.common.model

/**
 * Everything the artist screen needs in one snapshot.
 *
 * The profile itself is required, while tracks, playlists and related artists
 * are best-effort: one failed section must not hide the rest of the page.
 */
data class ArtistCatalog(
    val artist: Artist,
    val tracks: List<Track> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val related: List<Artist> = emptyList(),
)
