package com.sona.core.common.domain.repository

import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.Track

/**
 * Read/write operations for an Audius artist profile.
 *
 * Kept separate from [MusicRepository] so artist screens depend only on the
 * endpoints they actually use and existing catalog fakes stay small.
 */
interface ArtistRepository {

    suspend fun getArtist(userId: String): Result<Artist>

    suspend fun getTracks(
        userId: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Track>>

    suspend fun getPlaylists(
        userId: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Playlist>>

    suspend fun getRelated(
        userId: String,
        limit: Int = 12,
    ): Result<List<Artist>>

    /** Users that follow [userId] (`users/{id}/followers`). */
    suspend fun getFollowers(
        userId: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Artist>>

    /** Users that [userId] follows (`users/{id}/following`). */
    suspend fun getFollowing(
        userId: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Artist>>

    /** Tracks the artist saved to their own favorites (`users/{id}/favorites`). */
    suspend fun getFavorites(
        userId: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Track>>

    suspend fun follow(userId: String): Result<Unit>

    suspend fun unfollow(userId: String): Result<Unit>
}
