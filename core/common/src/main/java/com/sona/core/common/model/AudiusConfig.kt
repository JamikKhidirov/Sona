package com.sona.core.common.model

object AudiusConfig {
    const val APP_NAME = "Sona"
    const val API_BASE_URL = "https://api.audius.co/v1/"

    fun streamUrl(trackId: String): String =
        "${API_BASE_URL}tracks/$trackId/stream?app_name=$APP_NAME"

    fun artworkUrl(artwork: Artwork?): String? = artwork?.bestUrl()
}

data class Artwork(
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null,
) {
    fun bestUrl(): String? = large ?: medium ?: small
}
