package com.sona.core.network.audius.repository

import com.google.gson.Gson
import com.sona.core.common.coroutines.DispatcherProvider
import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.model.Artist
import com.sona.core.common.model.AudiusConfig
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.SearchResult
import com.sona.core.common.model.Track
import com.sona.core.network.audius.api.PlaylistApi
import com.sona.core.network.audius.api.TrackApi
import com.sona.core.network.audius.api.UserApi
import com.sona.core.network.audius.dto.PlaylistDto
import com.sona.core.network.audius.mapper.toDomain
import com.sona.core.network.audius.mapper.toDomainOrNull
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val trackApi: TrackApi,
    private val playlistApi: PlaylistApi,
    private val userApi: UserApi,
    private val gson: Gson,
    private val dispatchers: DispatcherProvider,
) : MusicRepository {

    override suspend fun getTrending(
        genre: String?,
        time: String,
        limit: Int,
        offset: Int,
    ): Result<List<Track>> = withContext(dispatchers.io) {
        runCatching {
            trackApi.getTrendingTracks(
                appName = AudiusConfig.APP_NAME,
                genre = genre,
                time = time,
                limit = limit,
                offset = offset,
            ).data.orEmpty().mapNotNull { it.toDomainOrNull() }
        }
    }

    override suspend fun search(query: String, limit: Int): Result<SearchResult> =
        withContext(dispatchers.io) {
            supervisorScope {
                val tracksDeferred = async {
                    runCatching {
                        trackApi.searchTracks(
                            query = query,
                            appName = AudiusConfig.APP_NAME,
                            limit = limit,
                            offset = 0,
                        ).data.orEmpty().mapNotNull { it.toDomainOrNull() }
                    }
                }
                val artistsDeferred = async {
                    runCatching {
                        userApi.searchUsers(
                            query = query,
                            appName = AudiusConfig.APP_NAME,
                            limit = limit,
                            offset = 0,
                        ).data.orEmpty().map { it.toDomain() }
                    }
                }
                val playlistsDeferred = async {
                    runCatching {
                        playlistApi.searchPlaylists(
                            query = query,
                            appName = AudiusConfig.APP_NAME,
                            limit = limit,
                            offset = 0,
                        ).data.orEmpty().map { it.toDomain() }
                    }
                }

                val tracks = tracksDeferred.await()
                val artists = artistsDeferred.await()
                val playlists = playlistsDeferred.await()

                if (tracks.isFailure && artists.isFailure && playlists.isFailure) {
                    Result.failure(tracks.exceptionOrNull() ?: IllegalStateException("Search failed"))
                } else {
                    Result.success(
                        SearchResult(
                            tracks = tracks.getOrDefault(emptyList()),
                            artists = artists.getOrDefault(emptyList()),
                            playlists = playlists.getOrDefault(emptyList()),
                        ),
                    )
                }
            }
        }

    override suspend fun getTrack(trackId: String): Result<Track> =
        withContext(dispatchers.io) {
            runCatching {
                val dto = trackApi.getTrack(trackId, AudiusConfig.APP_NAME).data
                    ?: error("Track not found")
                dto.toDomainOrNull() ?: error("Track not found")
            }
        }

    override suspend fun getUser(userId: String): Result<Artist> =
        withContext(dispatchers.io) {
            runCatching {
                userApi.getUser(userId, AudiusConfig.APP_NAME).data?.toDomain()
                    ?: error("Artist not found")
            }
        }

    override suspend fun getUserTracks(userId: String, limit: Int, offset: Int): Result<List<Track>> =
        withContext(dispatchers.io) {
            runCatching {
                userApi.getUserTracks(
                    userId = userId,
                    appName = AudiusConfig.APP_NAME,
                    limit = limit,
                    offset = offset,
                ).data.orEmpty().mapNotNull { it.toDomainOrNull() }
            }
        }

    override suspend fun getPlaylist(playlistId: String): Result<Playlist> =
        withContext(dispatchers.io) {
            runCatching {
                val element = playlistApi.getPlaylist(playlistId, AudiusConfig.APP_NAME).data
                    ?: error("Playlist not found")
                val dto = when {
                    element.isJsonArray -> element.asJsonArray.firstOrNull()?.let {
                        gson.fromJson(it, PlaylistDto::class.java)
                    }
                    else -> gson.fromJson(element, PlaylistDto::class.java)
                } ?: error("Playlist not found")
                dto.toDomain()
            }
        }

    override suspend fun getPlaylistTracks(playlistId: String): Result<List<Track>> =
        withContext(dispatchers.io) {
            runCatching {
                playlistApi.getPlaylistTracks(
                    playlistId = playlistId,
                    appName = AudiusConfig.APP_NAME,
                    limit = 100,
                    offset = 0,
                ).data.orEmpty().mapNotNull { it.toDomainOrNull() }
            }
        }

    override suspend fun getCurrentUser(): Result<Artist> =
        withContext(dispatchers.io) {
            runCatching {
                userApi.getCurrentUser(AudiusConfig.APP_NAME).data?.toDomain()
                    ?: error("Not signed in")
            }
        }
}
