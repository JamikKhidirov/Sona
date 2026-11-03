package com.sona.core.network.audius.repository

import com.google.gson.JsonElement
import com.sona.core.common.coroutines.DispatcherProvider
import com.sona.core.common.domain.repository.DiscoveryRepository
import com.sona.core.common.model.Artist
import com.sona.core.common.model.AudiusConfig
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.Track
import com.sona.core.network.audius.api.PlaylistApi
import com.sona.core.network.audius.api.SearchApi
import com.sona.core.network.audius.api.TrackApi
import com.sona.core.network.audius.mapper.toDomain
import com.sona.core.network.audius.mapper.toDomainOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoveryRepositoryImpl @Inject constructor(
    private val trackApi: TrackApi,
    private val playlistApi: PlaylistApi,
    private val searchApi: SearchApi,
    private val dispatchers: DispatcherProvider,
) : DiscoveryRepository {

    override suspend fun getUnderground(limit: Int, offset: Int): Result<List<Track>> =
        withContext(dispatchers.io) {
            runCatching {
                trackApi.getUndergroundTrendingTracks(
                    appName = AudiusConfig.APP_NAME,
                    limit = limit,
                    offset = offset,
                ).data.orEmpty().mapNotNull { it.toDomainOrNull() }
            }
        }

    override suspend fun getTrendingPlaylists(
        time: String,
        limit: Int,
        offset: Int,
    ): Result<List<Playlist>> = withContext(dispatchers.io) {
        runCatching {
            playlistApi.getTrendingPlaylists(
                appName = AudiusConfig.APP_NAME,
                time = time,
                limit = limit,
                offset = offset,
            ).data.orEmpty().map { it.toDomain() }
        }
    }

    override suspend fun getRelatedTracks(
        trackId: String,
        limit: Int,
        offset: Int,
    ): Result<List<Track>> = withContext(dispatchers.io) {
        runCatching {
            trackApi.getRelatedTracks(
                trackId = trackId,
                appName = AudiusConfig.APP_NAME,
                limit = limit,
                offset = offset,
            ).data.orEmpty().mapNotNull { it.toDomainOrNull() }
        }
    }

    override suspend fun getRemixes(
        trackId: String,
        limit: Int,
        offset: Int,
    ): Result<List<Track>> = withContext(dispatchers.io) {
        runCatching {
            trackApi.getTrackRemixes(
                trackId = trackId,
                appName = AudiusConfig.APP_NAME,
                limit = limit,
                offset = offset,
            ).data.orEmpty().mapNotNull { it.toDomainOrNull() }
        }
    }

    override suspend fun getStems(trackId: String): Result<List<Track>> =
        withContext(dispatchers.io) {
            runCatching {
                trackApi.getTrackStems(
                    trackId = trackId,
                    appName = AudiusConfig.APP_NAME,
                ).data.orEmpty().mapNotNull { it.toDomainOrNull() }
            }
        }

    override suspend fun getPlaylistFavorites(
        playlistId: String,
        limit: Int,
        offset: Int,
    ): Result<List<Artist>> = withContext(dispatchers.io) {
        runCatching {
            playlistApi.getPlaylistFavorites(
                playlistId = playlistId,
                appName = AudiusConfig.APP_NAME,
            ).data.orEmpty()
                .drop(offset)
                .take(limit)
                .map { it.toDomain() }
        }
    }

    override suspend fun favoritePlaylist(playlistId: String): Result<Unit> =
        withContext(dispatchers.io) {
            runCatching {
                playlistApi.favoritePlaylist(playlistId, AudiusConfig.APP_NAME).requireSuccess()
            }
        }

    override suspend fun unfavoritePlaylist(playlistId: String): Result<Unit> =
        withContext(dispatchers.io) {
            runCatching {
                playlistApi.unfavoritePlaylist(playlistId, AudiusConfig.APP_NAME).requireSuccess()
            }
        }

    override suspend fun getAutocomplete(query: String, limit: Int): Result<List<String>> =        withContext(dispatchers.io) {
            runCatching {
                searchApi.autocomplete(
                    query = query,
                    appName = AudiusConfig.APP_NAME,
                    limit = limit,
                ).data.toStringList()
            }
        }

    private fun JsonElement?.toStringList(): List<String> = when {
        this == null -> emptyList()
        isJsonArray -> asJsonArray.mapNotNull { element ->
            when {
                element.isJsonPrimitive -> element.asString
                element.isJsonObject -> element.asJsonObject
                    .get("name")
                    ?.takeIf { it.isJsonPrimitive }
                    ?.asString
                else -> null
            }
        }.filter { it.isNotBlank() }
        isJsonPrimitive -> listOf(asString).filter { it.isNotBlank() }
        else -> emptyList()
    }
}
