package com.sona.core.common.domain.usecase.library

import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.model.Track
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject




class ObserveFavorites @Inject constructor(
    private val repository: LibraryRepository,
) {
    operator fun invoke(): Flow<List<Track>> = repository.favorites
}