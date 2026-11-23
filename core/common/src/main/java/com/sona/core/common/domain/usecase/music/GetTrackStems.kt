package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.DiscoveryRepository
import com.sona.core.common.model.Track
import javax.inject.Inject

class GetTrackStems @Inject constructor(
    private val repository: DiscoveryRepository,
) {
    suspend operator fun invoke(trackId: String): Result<List<Track>> =
        repository.getStems(trackId)
}
