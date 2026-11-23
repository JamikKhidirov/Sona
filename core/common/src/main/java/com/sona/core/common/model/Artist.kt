package com.sona.core.common.model

data class Artist(
    val id: String,
    val handle: String,
    val name: String,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val bio: String? = null,
    val followerCount: Long = 0,
    val followingCount: Long = 0,
    val trackCount: Long = 0,
    val playlistCount: Long = 0,
    val location: String? = null,
    val isVerified: Boolean = false,
) {
    val displayName: String get() = name.ifBlank { handle }
}
