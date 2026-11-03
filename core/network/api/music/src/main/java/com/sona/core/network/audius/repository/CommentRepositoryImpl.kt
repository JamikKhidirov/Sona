package com.sona.core.network.audius.repository

import com.sona.core.common.coroutines.DispatcherProvider
import com.sona.core.common.domain.repository.CommentRepository
import com.sona.core.common.model.AudiusConfig
import com.sona.core.common.model.Comment
import com.sona.core.network.audius.api.TrackApi
import com.sona.core.network.audius.dto.CreateCommentRequest
import com.sona.core.network.audius.mapper.toDomainOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val trackApi: TrackApi,
    private val dispatchers: DispatcherProvider,
) : CommentRepository {

    override suspend fun getComments(
        trackId: String,
        limit: Int,
        offset: Int,
    ): Result<List<Comment>> = withContext(dispatchers.io) {
        runCatching {
            trackApi.getTrackComments(
                trackId = trackId,
                appName = AudiusConfig.APP_NAME,
                limit = limit,
                offset = offset,
            ).data.orEmpty().mapNotNull { it.toDomainOrNull() }
        }
    }

    override suspend fun postComment(trackId: String, message: String): Result<Comment> =
        withContext(dispatchers.io) {
            runCatching {
                val response = trackApi.postTrackComment(
                    trackId = trackId,
                    appName = AudiusConfig.APP_NAME,
                    body = CreateCommentRequest(message = message),
                )
                response.data?.toDomainOrNull() ?: error("Comment was not accepted")
            }
        }
}
