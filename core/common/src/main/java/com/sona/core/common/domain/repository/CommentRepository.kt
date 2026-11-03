package com.sona.core.common.domain.repository

import com.sona.core.common.model.Comment

interface CommentRepository {

    suspend fun getComments(
        trackId: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Comment>>

    suspend fun postComment(
        trackId: String,
        message: String,
    ): Result<Comment>
}
