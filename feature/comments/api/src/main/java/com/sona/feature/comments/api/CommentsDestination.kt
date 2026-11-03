package com.sona.feature.comments.api

import java.net.URLEncoder

object CommentsDestination {
    const val ARG_TRACK_ID: String = "trackId"
    const val ROUTE: String = "track/{$ARG_TRACK_ID}/comments"

    fun createRoute(trackId: String): String = "track/${encode(trackId)}/comments"

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
