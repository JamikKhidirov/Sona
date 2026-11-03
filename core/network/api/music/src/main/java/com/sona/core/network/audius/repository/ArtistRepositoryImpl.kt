package com.sona.core.network.audius.repository

import com.sona.core.common.coroutines.DispatcherProvider
import com.sona.core.common.domain.repository.ArtistRepository
import com.sona.core.common.model.Artist
import com.sona.core.common.model.AudiusConfig
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.Track
import com.sona.core.network.audius.api.UserApi
import com.sona.core.network.audius.mapper.toDomain
import com.sona.core.network.audius.mapper.toDomainOrNull
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val dispatchers: DispatcherProvider,
) : ArtistRepository {

    override suspend fun getArtist(userId: String): Result<Artist> =
        withContext(dispatchers.io) {
            runCatching {
                userApi.getUser(userId, AudiusConfig.APP_NAME).data?.toDomain()
                    ?: error("Artist not found")
            }
        }

    override suspend fun getTracks(
        userId: String,
        limit: Int,
        offset: Int,
    ): Result<List<Track>> = withContext(dispatchers.io) {
        runCatching {
            userApi.getUserTracks(
                userId = userId,
                appName = AudiusConfig.APP_NAME,
                limit = limit,
                offset = offset,
            ).data.orEmpty().mapNotNull { it.toDomainOrNull() }
        }
    }

    override suspend fun getPlaylists(
        userId: String,
        limit: Int,
        offset: Int,
    ): Result<List<Playlist>> = withContext(dispatchers.io) {
        runCatching {
            userApi.getUserPlaylists(
                userId = userId,
                appName = AudiusConfig.APP_NAME,
                limit = limit,
                offset = offset,
            ).data.orEmpty().map { it.toDomain() }
        }
    }

    override suspend fun getRelated(userId: String, limit: Int): Result<List<Artist>> =
        withContext(dispatchers.io) {
            runCatching {
                userApi.getRelatedUsers(
                    userId = userId,
                    appName = AudiusConfig.APP_NAME,
                    limit = limit,
                ).data.orEmpty().map { it.toDomain() }
            }
        }

    override suspend fun getFollowers(
        userId: String,
        limit: Int,
        offset: Int,
    ): Result<List<Artist>> = withContext(dispatchers.io) {
        runCatching {
            userApi.getFollowers(
                userId = userId,
                appName = AudiusConfig.APP_NAME,
                limit = limit,
                offset = offset,
            ).data.orEmpty().map { it.toDomain() }
        }
    }

    override suspend fun getFollowing(
        userId: String,
        limit: Int,
        offset: Int,
    ): Result<List<Artist>> = withContext(dispatchers.io) {
        runCatching {
            userApi.getFollowing(
                userId = userId,
                appName = AudiusConfig.APP_NAME,
                limit = limit,
                offset = offset,
            ).data.orEmpty().map { it.toDomain() }
        }
    }

    override suspend fun getFavorites(
        userId: String,
        limit: Int,
        offset: Int,
    ): Result<List<Track>> = withContext(dispatchers.io) {
        runCatching {
            userApi.getUserFavorites(
                userId = userId,
                appName = AudiusConfig.APP_NAME,
                limit = limit,
                offset = offset,
            ).data.orEmpty().mapNotNull { it.toDomainOrNull() }
        }
    }

    override suspend fun follow(userId: String): Result<Unit> =
        withContext(dispatchers.io) {
            runCatching {
                userApi.followUser(userId, AudiusConfig.APP_NAME).requireSuccess()
            }
        }

    override suspend fun unfollow(userId: String): Result<Unit> =
        withContext(dispatchers.io) {
            runCatching {
                userApi.unfollowUser(userId, AudiusConfig.APP_NAME).requireSuccess()
            }
        }
}

internal fun Response<Unit>.requireSuccess() {
    if (!isSuccessful) error("Audius returned HTTP ${code()}")
}
