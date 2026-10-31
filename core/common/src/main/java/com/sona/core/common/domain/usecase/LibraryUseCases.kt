package com.sona.core.common.domain.usecase

import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.model.Track
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavorites @Inject constructor(
    private val repository: LibraryRepository,
) {
    operator fun invoke(): Flow<List<Track>> = repository.favorites
}

class ObserveFavoriteIds @Inject constructor(
    private val repository: LibraryRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.favoriteIds
}

class ObserveRecentlyPlayed @Inject constructor(
    private val repository: LibraryRepository,
) {
    operator fun invoke(): Flow<List<Track>> = repository.recentlyPlayed
}

class ToggleFavorite @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(track: Track) = repository.toggleFavorite(track)
}

class AddRecentlyPlayed @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke(track: Track) = repository.addRecentlyPlayed(track)
}

class ClearLibrary @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke() = repository.clearLibrary()
}
