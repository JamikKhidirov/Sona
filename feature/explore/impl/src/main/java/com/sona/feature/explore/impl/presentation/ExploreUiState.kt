package com.sona.feature.explore.impl.presentation

import com.sona.core.common.model.Playlist
import com.sona.core.common.model.Track

enum class ExploreTab {
    PLAYLISTS,
    UNDERGROUND,
}

data class ExploreUiState(
    val tab: ExploreTab = ExploreTab.PLAYLISTS,
    val isLoading: Boolean = true,
    val playlists: List<Playlist> = emptyList(),
    val underground: List<Track> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val error: String? = null,
) {
    val itemsCount: Int get() = when (tab) {
        ExploreTab.PLAYLISTS -> playlists.size
        ExploreTab.UNDERGROUND -> underground.size
    }
}

sealed interface ExploreIntent {
    data object Load : ExploreIntent
    data object Retry : ExploreIntent
    data class SelectTab(val tab: ExploreTab) : ExploreIntent
    data class PlayTrack(val track: Track) : ExploreIntent
    data class ToggleFavorite(val track: Track) : ExploreIntent
}

sealed interface ExploreEffect {
    data class Message(val text: String) : ExploreEffect
}
