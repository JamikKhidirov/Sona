package com.sona.core.common.domain.usecase.music

import com.sona.core.common.domain.repository.ArtistRepository
import javax.inject.Inject

/**
 * Follows or unfollows an artist and returns the new following state.
 *
 * Audius requires an OAuth token for this action, so a failure is expected in
 * the public (signed-out) mode and is surfaced to the caller as [Result].
 */
class ToggleFollow @Inject constructor(
    private val repository: ArtistRepository,
) {
    suspend operator fun invoke(userId: String, isFollowing: Boolean): Result<Boolean> =
        if (isFollowing) {
            repository.unfollow(userId).map { false }
        } else {
            repository.follow(userId).map { true }
        }
}
