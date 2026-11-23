package com.sona.feature.artistcommunity.impl.presentation

import com.sona.core.common.model.Artist

enum class CommunityTab(val label: String) {
    FOLLOWERS("Подписчики"),
    FOLLOWING("Подписки"),
}

data class ArtistCommunityUiState(
    val artistId: String = "",
    val tab: CommunityTab = CommunityTab.FOLLOWERS,
    val artist: Artist? = null,
    val followers: List<Artist> = emptyList(),
    val following: List<Artist> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    val items: List<Artist>
        get() = when (tab) {
            CommunityTab.FOLLOWERS -> followers
            CommunityTab.FOLLOWING -> following
        }
}

sealed interface ArtistCommunityIntent {
    data class Load(val artistId: String) : ArtistCommunityIntent
    data object Retry : ArtistCommunityIntent
    data class SelectTab(val tab: CommunityTab) : ArtistCommunityIntent
}
