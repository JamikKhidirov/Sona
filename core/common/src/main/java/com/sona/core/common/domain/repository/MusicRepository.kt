package com.sona.core.common.domain.repository

import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.SearchResult
import com.sona.core.common.model.Track

interface MusicRepository {

    suspend fun getTrending(
        genre: String? = null,
        time: String = "week",
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Track>>

    suspend fun search(query: String, limit: Int = 25): Result<SearchResult>

    suspend fun getTrack(trackId: String): Result<Track>

    suspend fun getUser(userId: String): Result<Artist>

    suspend fun getUserTracks(userId: String, limit: Int = 50, offset: Int = 0): Result<List<Track>>

    suspend fun getPlaylist(playlistId: String): Result<Playlist>

    suspend fun getPlaylistTracks(playlistId: String): Result<List<Track>>

    suspend fun getCurrentUser(): Result<Artist>
}
