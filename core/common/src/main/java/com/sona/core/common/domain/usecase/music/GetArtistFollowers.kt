package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.ArtistRepository
import com.sona.core.common.model.Artist
import javax.inject.Inject

class GetArtistFollowers @Inject constructor(
    private val repository: ArtistRepository,
) {
    suspend operator fun invoke(
        userId: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Artist>> = repository.getFollowers(
        userId = userId,
        limit = limit,
        offset = offset,
    )
}
