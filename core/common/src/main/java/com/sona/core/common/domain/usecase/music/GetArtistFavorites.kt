package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.ArtistRepository
import com.sona.core.common.model.Track
import javax.inject.Inject

class GetArtistFavorites @Inject constructor(
    private val repository: ArtistRepository,
) {
    suspend operator fun invoke(
        userId: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Track>> = repository.getFavorites(
        userId = userId,
        limit = limit,
        offset = offset,
    )
}
