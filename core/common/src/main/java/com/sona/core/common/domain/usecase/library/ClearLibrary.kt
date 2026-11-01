package com.sona.core.common.domain.usecase.library

import com.sona.core.common.domain.repository.LibraryRepository
import javax.inject.Inject



class ClearLibrary @Inject constructor(
    private val repository: LibraryRepository,
) {
    suspend operator fun invoke() = repository.clearLibrary()
}
