package com.sona.core.common.domain.repository

import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.Track

/**
 * Extra discovery surfaces exposed by Audius that are not part of the
 * trending/search flow: underground charts, trending playlists, remixes,
 * related tracks and search autocomplete.
 */
interface DiscoveryRepository {

    suspend fun getUnderground(
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Track>>

    suspend fun getTrendingPlaylists(
        time: String = "week",
        limit: Int = 25,
        offset: Int = 0,
    ): Result<List<Playlist>>

    suspend fun getRelatedTracks(
        trackId: String,
        limit: Int = 25,
        offset: Int = 0,
    ): Result<List<Track>>

    suspend fun getRemixes(
        trackId: String,
        limit: Int = 25,
        offset: Int = 0,
    ): Result<List<Track>>

    /** Stems published for a track (`tracks/{id}/stems`). */
    suspend fun getStems(trackId: String): Result<List<Track>>

    /** Users that saved [playlistId] to their library. */
    suspend fun getPlaylistFavorites(
        playlistId: String,
        limit: Int = 25,
        offset: Int = 0,
    ): Result<List<Artist>>

    suspend fun favoritePlaylist(playlistId: String): Result<Unit>

    suspend fun unfavoritePlaylist(playlistId: String): Result<Unit>

    suspend fun getAutocomplete(
        query: String,
        limit: Int = 10,
    ): Result<List<String>>
}
