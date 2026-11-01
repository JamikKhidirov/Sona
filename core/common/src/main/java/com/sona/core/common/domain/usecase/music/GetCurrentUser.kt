package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.model.Artist
import javax.inject.Inject

class GetCurrentUser @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(): Result<Artist> = repository.getCurrentUser()
}