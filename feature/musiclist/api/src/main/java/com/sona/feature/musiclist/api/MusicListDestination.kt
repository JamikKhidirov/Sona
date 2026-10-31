package com.sona.feature.musiclist.api

import java.net.URLEncoder

/**
 * Source is one of: trending, genre, artist, playlist.
 */
object MusicListDestination {
    const val ARG_SOURCE: String = "source"
    const val ARG_ID: String = "id"
    const val ARG_TITLE: String = "title"

    const val ROUTE: String =
        "collection?$ARG_SOURCE={$ARG_SOURCE}&$ARG_ID={$ARG_ID}&$ARG_TITLE={$ARG_TITLE}"

    fun createRoute(source: String, id: String, title: String): String =
        "collection?$ARG_SOURCE=${encode(source)}&$ARG_ID=${encode(id)}&$ARG_TITLE=${encode(title)}"

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
