package com.sona.feature.profile.impl.presentation

import com.sona.core.common.model.Artist
import com.sona.core.common.model.Track

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: Artist? = null,
    val favorites: List<Track> = emptyList(),
    val recentlyPlayed: List<Track> = emptyList(),
    val isSignedIn: Boolean = false,
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val error: String? = null,
) {
    val displayName: String get() = user?.displayName ?: "Sona listener"
    val handle: String get() = user?.handle?.let { "@$it" } ?: "Local profile"
    val followerCount: Long get() = user?.followerCount ?: 0L
}

sealed interface ProfileIntent {
    data class PlayTrack(val track: Track) : ProfileIntent
    data object Refresh : ProfileIntent
}
