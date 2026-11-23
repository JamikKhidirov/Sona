package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.ArtistRepository
import com.sona.core.common.model.ArtistCatalog
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

/**
 * Loads an artist profile together with tracks, playlists and related artists.
 *
 * Only the profile is mandatory. The three collections are fetched in parallel
 * and a failure in one of them does not discard the others, which keeps the
 * artist page useful on flaky mobile connections.
 */
class GetArtistCatalog @Inject constructor(
    private val repository: ArtistRepository,
) {
    suspend operator fun invoke(userId: String): Result<ArtistCatalog> = supervisorScope {
        val artistDeferred = async { repository.getArtist(userId) }
        val tracksDeferred = async { repository.getTracks(userId) }
        val playlistsDeferred = async { repository.getPlaylists(userId) }
        val relatedDeferred = async { repository.getRelated(userId) }

        val artistResult = artistDeferred.await()
        val artist = artistResult.getOrNull()
            ?: return@supervisorScope Result.failure(
                artistResult.exceptionOrNull() ?: IllegalStateException("Artist not found"),
            )

        Result.success(
            ArtistCatalog(
                artist = artist,
                tracks = tracksDeferred.await().getOrDefault(emptyList()),
                playlists = playlistsDeferred.await().getOrDefault(emptyList()),
                related = relatedDeferred.await().getOrDefault(emptyList()),
            ),
        )
    }
}
