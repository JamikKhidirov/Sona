package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.DiscoveryRepository
import com.sona.core.common.model.Artist
import javax.inject.Inject

class GetPlaylistFavorites @Inject constructor(
    private val repository: DiscoveryRepository,
) {
    suspend operator fun invoke(
        playlistId: String,
        limit: Int = 25,
    ): Result<List<Artist>> = repository.getPlaylistFavorites(
        playlistId = playlistId,
        limit = limit,
    )
}
