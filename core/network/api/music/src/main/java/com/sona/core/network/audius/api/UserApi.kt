package com.sona.core.network.audius.api

import com.google.gson.JsonElement
import com.sona.core.network.audius.dto.AudiusEnvelope
import com.sona.core.network.audius.dto.PlaylistDto
import com.sona.core.network.audius.dto.TrackDto
import com.sona.core.network.audius.dto.UserDto
import com.sona.core.network.audius.model.FilterTracks
import com.sona.core.network.audius.model.FilterTracksLimited
import com.sona.core.network.audius.model.SortAi
import com.sona.core.network.audius.model.SortDirection
import com.sona.core.network.audius.model.SortMethod
import com.sona.core.network.audius.model.SortMethodUser
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Audius user/account endpoints.
 * Docs: https://docs.audius.co/api#tag/users
 */
interface UserApi {

    @GET("users")
    suspend fun getUsers(
        @Query("app_name") appName: String,
        @Query("id") ids: List<String>,
    ): AudiusEnvelope<List<UserDto>>

    @GET("users/{userId}")
    suspend fun getUser(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
    ): AudiusEnvelope<UserDto>

    @GET("users/handle/{handle}")
    suspend fun getUserByHandle(
        @Path("handle") handle: String,
        @Query("app_name") appName: String,
    ): AudiusEnvelope<UserDto>

    @GET("users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("app_name") appName: String,
        @Query("genre") genre: List<String> = emptyList(),
        @Query("sort_method") sortMethod: SortMethodUser? = null,
        @Query("is_verified") isVerified: String? = null,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<UserDto>>

    @GET("users/me")
    suspend fun getCurrentUser(
        @Query("app_name") appName: String,
    ): AudiusEnvelope<UserDto>

    @GET("users/account")
    suspend fun getAccount(
        @Query("app_name") appName: String,
    ): AudiusEnvelope<JsonElement>

    @GET("users/{userId}/tracks")
    suspend fun getUserTracks(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("sort_method") sortMethod: SortMethod? = null,
        @Query("sort_direction") sortDirection: SortDirection? = null,
        @Query("filter_tracks") filterTracks: FilterTracks? = null,
        @Query("query") query: String? = null,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("users/handle/{handle}/tracks")
    suspend fun getUserTracksByHandle(
        @Path("handle") handle: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("sort_method") sortMethod: SortMethod? = null,
        @Query("sort_direction") sortDirection: SortDirection? = null,
        @Query("filter_tracks") filterTracks: FilterTracks? = null,
        @Query("query") query: String? = null,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("users/{userId}/tracks/ai_attributed")
    suspend fun getAiAttributedTracks(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
        @Query("sort") sort: SortAi? = null,
        @Query("sort_method") sortMethod: SortMethod? = null,
        @Query("sort_direction") sortDirection: SortDirection? = null,
        @Query("filter_tracks") filterTracks: FilterTracksLimited? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("users/{userId}/playlists")
    suspend fun getUserPlaylists(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<PlaylistDto>>

    @GET("users/{userId}/favorites")
    suspend fun getUserFavorites(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("users/{userId}/reposts")
    suspend fun getUserReposts(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<JsonElement>

    @GET("users/{userId}/followers")
    suspend fun getFollowers(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<UserDto>>

    @GET("users/{userId}/following")
    suspend fun getFollowing(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<UserDto>>

    @GET("users/{userId}/related")
    suspend fun getRelatedUsers(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 25,
    ): AudiusEnvelope<List<UserDto>>

    @GET("users/{userId}/tags")
    suspend fun getUserTags(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
    ): AudiusEnvelope<JsonElement>

    @GET("users/{userId}/history")
    suspend fun getUserHistory(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<JsonElement>

    @POST("users/{userId}/follow")
    suspend fun followUser(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @DELETE("users/{userId}/follow")
    suspend fun unfollowUser(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @POST("users/{userId}/mute")
    suspend fun muteUser(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @DELETE("users/{userId}/mute")
    suspend fun unmuteUser(
        @Path("userId") userId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @GET("users/verify_token")
    suspend fun verifyToken(
        @Query("app_name") appName: String,
    ): AudiusEnvelope<JsonElement>
}
