package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.model.Artist
import javax.inject.Inject



class GetArtist @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(userId: String): Result<Artist> = repository.getUser(userId)
}