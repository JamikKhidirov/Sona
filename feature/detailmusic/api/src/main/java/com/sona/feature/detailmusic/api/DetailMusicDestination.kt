package com.sona.feature.detailmusic.api

import java.net.URLEncoder

object DetailMusicDestination {
    const val ARG_TRACK_ID: String = "trackId"
    const val ROUTE: String = "track/{$ARG_TRACK_ID}"

    fun createRoute(trackId: String): String = "track/${encode(trackId)}"

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
