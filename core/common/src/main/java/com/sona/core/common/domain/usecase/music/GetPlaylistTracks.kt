package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.model.Track
import javax.inject.Inject


class GetPlaylistTracks @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(playlistId: String): Result<List<Track>> =
        repository.getPlaylistTracks(playlistId)
}