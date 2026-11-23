package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.DiscoveryRepository
import javax.inject.Inject

/**
 * Saves or removes a playlist from the signed-in user's library and returns
 * the new favorite state.
 */
class TogglePlaylistFavorite @Inject constructor(
    private val repository: DiscoveryRepository,
) {
    suspend operator fun invoke(playlistId: String, isFavorite: Boolean): Result<Boolean> =
        if (isFavorite) {
            repository.unfavoritePlaylist(playlistId).map { false }
        } else {
            repository.favoritePlaylist(playlistId).map { true }
        }
}
