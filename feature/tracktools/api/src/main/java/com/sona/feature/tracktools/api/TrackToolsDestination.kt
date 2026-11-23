package com.sona.feature.tracktools.api

import java.net.URLEncoder

object TrackToolsDestination {
    const val ARG_TRACK_ID: String = "trackId"
    const val ROUTE: String = "track/{$ARG_TRACK_ID}/tools"

    fun createRoute(trackId: String): String = "track/${encode(trackId)}/tools"

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
