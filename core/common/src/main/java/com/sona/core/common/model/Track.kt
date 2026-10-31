package com.sona.core.common.model

data class Track(
    val id: String,
    val title: String,
    val artist: Artist,
    val artworkUrl: String? = null,
    val genre: String? = null,
    val mood: String? = null,
    val durationSeconds: Int = 0,
    val playCount: Long = 0,
    val favoriteCount: Long = 0,
    val repostCount: Long = 0,
    val commentCount: Long = 0,
    val description: String? = null,
    val isStreamable: Boolean = true,
    val releaseDate: String? = null,
    val permalink: String? = null,
    val streamUrl: String = AudiusConfig.streamUrl(id),
) {
    val artistName: String get() = artist.displayName
}
