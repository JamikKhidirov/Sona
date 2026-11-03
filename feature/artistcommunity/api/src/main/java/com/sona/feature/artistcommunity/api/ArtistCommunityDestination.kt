package com.sona.feature.artistcommunity.api

import java.net.URLEncoder

object ArtistCommunityDestination {
    const val ARG_ARTIST_ID: String = "artistId"
    const val ROUTE: String = "artist/{$ARG_ARTIST_ID}/community"

    fun createRoute(artistId: String): String = "artist/${encode(artistId)}/community"

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
