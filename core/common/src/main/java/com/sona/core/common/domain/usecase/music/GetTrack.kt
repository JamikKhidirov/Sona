package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.model.Track
import javax.inject.Inject


class GetTrack @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(trackId: String): Result<Track> = repository.getTrack(trackId)
}
