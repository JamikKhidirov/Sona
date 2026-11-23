package com.sona.feature.artistfavorites.api

import java.net.URLEncoder

object ArtistFavoritesDestination {
    const val ARG_ARTIST_ID: String = "artistId"
    const val ROUTE: String = "artist/{$ARG_ARTIST_ID}/favorites"

    fun createRoute(artistId: String): String = "artist/${encode(artistId)}/favorites"

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
