package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.CommentRepository
import com.sona.core.common.model.Comment
import javax.inject.Inject

class PostTrackComment @Inject constructor(
    private val repository: CommentRepository,
) {
    suspend operator fun invoke(trackId: String, message: String): Result<Comment> =
        repository.postComment(trackId = trackId, message = message.trim())
}
