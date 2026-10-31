package com.sona.core.network.audius.api

import com.google.gson.JsonElement
import com.sona.core.network.audius.dto.AudiusEnvelope
import com.sona.core.network.audius.dto.CommentDto
import com.sona.core.network.audius.dto.CommentResponse
import com.sona.core.network.audius.dto.CreateCommentRequest
import com.sona.core.network.audius.dto.TrackDto
import com.sona.core.network.audius.dto.UpdateTrackRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * Audius track/music endpoints.
 * Docs: https://docs.audius.co/api#tag/tracks
 */
interface TrackApi {

    @GET("tracks/trending")
    suspend fun getTrendingTracks(
        @Query("app_name") appName: String,
        @Query("genre") genre: String? = null,
        @Query("time") time: String = "week",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("tracks/trending/underground")
    suspend fun getUndergroundTrendingTracks(
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("tracks/trending/ids")
    suspend fun getTrendingTrackIds(
        @Query("app_name") appName: String,
        @Query("genre") genre: String? = null,
        @Query("time") time: String = "week",
        @Query("limit") limit: Int = 50,
    ): AudiusEnvelope<JsonElement>

    @GET("tracks")
    suspend fun getTracks(
        @Query("app_name") appName: String,
        @Query("id") ids: List<String>,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("tracks/{trackId}")
    suspend fun getTrack(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
    ): AudiusEnvelope<TrackDto>

    @GET("tracks/search")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("app_name") appName: String,
        @Query("genre") genre: String? = null,
        @Query("mood") mood: String? = null,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("tracks/{trackId}/related")
    suspend fun getRelatedTracks(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("tracks/{trackId}/remixes")
    suspend fun getTrackRemixes(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("tracks/{trackId}/stems")
    suspend fun getTrackStems(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
    ): AudiusEnvelope<List<TrackDto>>

    @GET("tracks/{trackId}/comments")
    suspend fun getTrackComments(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<List<CommentDto>>

    @POST("tracks/{trackId}/comments")
    suspend fun postTrackComment(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
        @Body body: CreateCommentRequest,
    ): CommentResponse

    @GET("tracks/{trackId}/access_info")
    suspend fun getTrackAccessInfo(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
    ): AudiusEnvelope<JsonElement>

    @Streaming
    @GET("tracks/{trackId}/stream")
    suspend fun streamTrack(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
    ): ResponseBody

    @Streaming
    @GET("tracks/{trackId}/download")
    suspend fun downloadTrack(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
    ): ResponseBody

    @POST("tracks/{trackId}/favorite")
    suspend fun favoriteTrack(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @DELETE("tracks/{trackId}/favorite")
    suspend fun unfavoriteTrack(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @POST("tracks/{trackId}/repost")
    suspend fun repostTrack(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @DELETE("tracks/{trackId}/repost")
    suspend fun unrepostTrack(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>

    @PUT("tracks/{trackId}")
    suspend fun updateTrack(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
        @Body body: UpdateTrackRequest,
    ): Response<Unit>

    @DELETE("tracks/{trackId}")
    suspend fun deleteTrack(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String,
    ): Response<Unit>
}
