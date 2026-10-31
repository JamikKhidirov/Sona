package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.model.SearchResult
import javax.inject.Inject



class SearchCatalog @Inject constructor(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(query: String, limit: Int = 25): Result<SearchResult> =
        repository.search(query = query, limit = limit)
}