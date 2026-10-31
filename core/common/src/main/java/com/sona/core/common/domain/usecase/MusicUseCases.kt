package com.sona.core.common.domain.usecase

import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.SearchResult
import com.sona.core.common.model.Track
import javax.inject.Inject

class GetTrendingTracks @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(
        genre: String? = null,
        time: String = "week",
        limit: Int = 50,
    ): Result<List<Track>> = repository.getTrending(genre = genre, time = time, limit = limit)
}

class SearchCatalog @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(query: String, limit: Int = 25): Result<SearchResult> =
        repository.search(query = query, limit = limit)
}

class GetTrack @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(trackId: String): Result<Track> = repository.getTrack(trackId)
}

class GetArtist @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(userId: String): Result<Artist> = repository.getUser(userId)
}

class GetArtistTracks @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(userId: String, limit: Int = 50): Result<List<Track>> =
        repository.getUserTracks(userId = userId, limit = limit)
}

class GetPlaylist @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(playlistId: String): Result<Playlist> =
        repository.getPlaylist(playlistId)
}

class GetPlaylistTracks @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(playlistId: String): Result<List<Track>> =
        repository.getPlaylistTracks(playlistId)
}

class GetCurrentUser @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(): Result<Artist> = repository.getCurrentUser()
}
