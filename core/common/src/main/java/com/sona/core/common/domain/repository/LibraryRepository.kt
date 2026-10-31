package com.sona.core.common.domain.repository

import com.sona.core.common.model.Track
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    val favorites: Flow<List<Track>>
    val favoriteIds: Flow<Set<String>>
    val recentlyPlayed: Flow<List<Track>>

    suspend fun toggleFavorite(track: Track)
    suspend fun isFavorite(trackId: String): Boolean
    suspend fun addRecentlyPlayed(track: Track)
    suspend fun clearLibrary()
}
