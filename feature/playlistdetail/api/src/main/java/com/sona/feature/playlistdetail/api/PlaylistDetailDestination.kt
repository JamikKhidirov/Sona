package com.sona.feature.playlistdetail.api

import java.net.URLEncoder

object PlaylistDetailDestination {
    const val ARG_PLAYLIST_ID: String = "playlistId"
    const val ROUTE: String = "playlist/{$ARG_PLAYLIST_ID}"

    fun createRoute(playlistId: String): String = "playlist/${encode(playlistId)}"

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
