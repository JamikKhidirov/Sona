package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.model.Track
import javax.inject.Inject



class GetArtistTracks @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(userId: String, limit: Int = 50): Result<List<Track>> =
        repository.getUserTracks(userId = userId, limit = limit)
}