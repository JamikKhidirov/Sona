package com.sona.core.network.audius.api

import com.google.gson.JsonElement
import com.sona.core.network.audius.dto.AudiusEnvelope
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Audius cross-entity search endpoints.
 * Docs: https://docs.audius.co/api#tag/search
 */
interface SearchApi {

    @GET("search")
    suspend fun search(
        @Query("query") query: String,
        @Query("app_name") appName: String,
        @Query("genre") genre: String? = null,
        @Query("mood") mood: String? = null,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
    ): AudiusEnvelope<JsonElement>

    @GET("search/full")
    suspend fun searchFull(
        @Query("query") query: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
        @Query("kind") kind: String? = null,
    ): AudiusEnvelope<JsonElement>

    @GET("search/autocomplete")
    suspend fun autocomplete(
        @Query("query") query: String,
        @Query("app_name") appName: String,
        @Query("limit") limit: Int = 10,
    ): AudiusEnvelope<JsonElement>
}
