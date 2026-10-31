package com.sona.feature.searchmusic.impl.presentation

import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.Track

enum class SearchTab(val label: String) {
    TRACKS("Tracks"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists"),
}

data class SearchUiState(
    val query: String = "",
    val selectedTab: SearchTab = SearchTab.TRACKS,
    val isLoading: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val error: String? = null,
) {
    val hasQuery: Boolean get() = query.isNotBlank()
    val hasResults: Boolean get() = tracks.isNotEmpty() || artists.isNotEmpty() || playlists.isNotEmpty()
}

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data class TabSelected(val tab: SearchTab) : SearchIntent
    data class PlayTrack(val track: Track) : SearchIntent
    data class ToggleFavorite(val track: Track) : SearchIntent
    data class BrowseGenre(val genre: String) : SearchIntent
    data object Retry : SearchIntent
    data object ClearQuery : SearchIntent
}

sealed interface SearchEffect {
    data class Message(val text: String) : SearchEffect
}
