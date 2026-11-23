package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.DiscoveryRepository
import com.sona.core.common.model.Track
import javax.inject.Inject

class GetTrackRemixes @Inject constructor(
    private val repository: DiscoveryRepository,
) {
    suspend operator fun invoke(trackId: String, limit: Int = 25): Result<List<Track>> =
        repository.getRemixes(trackId = trackId, limit = limit)
}
