package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.DiscoveryRepository
import com.sona.core.common.model.Track
import javax.inject.Inject

class GetUndergroundTracks @Inject constructor(
    private val repository: DiscoveryRepository,
) {
    suspend operator fun invoke(limit: Int = 50, offset: Int = 0): Result<List<Track>> =
        repository.getUnderground(limit = limit, offset = offset)
}
