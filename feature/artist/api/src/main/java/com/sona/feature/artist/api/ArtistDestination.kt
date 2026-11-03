package com.sona.feature.artist.api

import java.net.URLEncoder

object ArtistDestination {
    const val ARG_ARTIST_ID: String = "artistId"
    const val ROUTE: String = "artist/{$ARG_ARTIST_ID}"

    fun createRoute(artistId: String): String = "artist/${encode(artistId)}"

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
