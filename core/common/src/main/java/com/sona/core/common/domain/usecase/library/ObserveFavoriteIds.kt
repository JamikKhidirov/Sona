package com.sona.core.common.domain.usecase.library

import com.sona.core.common.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ObserveFavoriteIds @Inject constructor(
    private val repository: LibraryRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.favoriteIds
}