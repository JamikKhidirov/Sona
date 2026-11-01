package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.MusicRepository
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