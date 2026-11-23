package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.DiscoveryRepository
import com.sona.core.common.model.Playlist
import javax.inject.Inject

class GetTrendingPlaylists @Inject constructor(
    private val repository: DiscoveryRepository,
) {
    suspend operator fun invoke(
        time: String = "week",
        limit: Int = 25,
        offset: Int = 0,
    ): Result<List<Playlist>> = repository.getTrendingPlaylists(
        time = time,
        limit = limit,
        offset = offset,
    )
}
