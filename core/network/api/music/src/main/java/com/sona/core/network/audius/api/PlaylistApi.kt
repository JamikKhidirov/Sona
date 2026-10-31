package com.sona.core.network.audius.api

import com.google.gson.JsonElement
import com.sona.core.network.audius.dto.AudiusEnvelope
import com.sona.core.network.audius.dto.PlaylistDto
import com.sona.core.network.audius.dto.TrackDto
import com.sona.core.network.audius.dto.UpdatePlaylistRequest
import com.sona.core.network.audius.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Audius playlist/album endpoints.
 * Docs: https://docs.audius.co/api#tag/playlists
 */
interface PlaylistApi {

    @GET("playlists")
    suspend fun getPlaylists(
        @Query("app_name") appName: String,
        @Query("id") ids: List<String>,
    ): AudiusEnvelope<List<PlaylistDto>>

    @GET("playlists/{playlistId}")
    suspend fun getPlaylist(
        @Path("playlistId") playlistId: String,
        @Query("app_name") appName: String,
    ): AudiusEnvelope<JsonElement>

    @GET("playlists/{playlistId}/tracks")
    suspend fun getPlaylistTracks(
        @Path("playlistId") playlistId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("playlists/search")
    suspend fun searchPlaylists(
        @Query("query") query: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<PlaylistDto>>

    @GET("playlists/trending")
    suspend fun getTrendingPlaylists(
        @Query("app_name") appName: String,
        @Query("time") time: String = "week",
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<PlaylistDto>>

    @GET("playlists/{playlistId}/favorites")
    suspend fun getPlaylistFavorites(
        @Path("playlistId") playlistId: String,
        @Query("app_name") appName: String,
    ): AudiusEnvelope<List<UserDto>>

    @GET("playlists/{playlistId}/access_info")
    suspend fun getPlaylistAccessInfo(
        @Path("playlistId") playlistId: String,
        @Query("app_name") appName: String,
    ): AudiusEnvelope<JsonElement>

    @POST("playlists/{playlistId}/favorite")
    suspend fun favoritePlaylist(
        @Path("playlistId") playlistId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @DELETE("playlists/{playlistId}/favorite")
    suspend fun unfavoritePlaylist(
        @Path("playlistId") playlistId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @POST("playlists/{playlistId}/repost")
    suspend fun repostPlaylist(
        @Path("playlistId") playlistId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @DELETE("playlists/{playlistId}/repost")
    suspend fun unrepostPlaylist(
        @Path("playlistId") playlistId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @PUT("playlists/{playlistId}")
    suspend fun updatePlaylist(
        @Path("playlistId") playlistId: String,
        @Query("app_name") appName: String,
        @Body body: UpdatePlaylistRequest,
    ): Response<Unit>

    @DELETE("playlists/{playlistId}")
    suspend fun deletePlaylist(
        @Path("playlistId") playlistId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>
}
