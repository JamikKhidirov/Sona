package com.sona.core.common.domain.usecase.library

import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.model.Track
import javax.inject.Inject

class AddRecentlyPlayed @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(track: Track) = repository.addRecentlyPlayed(track)
}