package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.CommentRepository
import com.sona.core.common.model.Comment
import javax.inject.Inject

class GetTrackComments @Inject constructor(
    private val repository: CommentRepository,
) {
    suspend operator fun invoke(
        trackId: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Comment>> = repository.getComments(
        trackId = trackId,
        limit = limit,
        offset = offset,
    )
}
