package com.sona.core.common.model

data class Playlist(
    val id: String,
    val name: String,
    val ownerName: String? = null,
    val artworkUrl: String? = null,
    val description: String? = null,
    val trackCount: Int = 0,
    val playCount: Long = 0,
    val isAlbum: Boolean = false,
    val tracks: List<Track> = emptyList(),
)
