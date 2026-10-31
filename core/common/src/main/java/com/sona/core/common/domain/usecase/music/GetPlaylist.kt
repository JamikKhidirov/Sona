package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.model.Playlist
import javax.inject.Inject



class GetPlaylist @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(playlistId: String): Result<Playlist> =
        repository.getPlaylist(playlistId)
}
