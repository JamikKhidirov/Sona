package com.sona.feature.charts.impl.presentation

import com.sona.core.common.model.Track

/**
 * Time window used by the Audius trending endpoint.
 *
 * [apiValue] is the canonical English value sent to the server, [label] is
 * what the user sees. Keeping them apart prevents localization from leaking
 * into network requests.
 */
enum class ChartPeriod(val apiValue: String, val label: String) {
    WEEK("week", "Неделя"),
    MONTH("month", "Месяц"),
    YEAR("year", "Год"),
    ALL_TIME("allTime", "Всё время"),
}

data class ChartsUiState(
    val period: ChartPeriod = ChartPeriod.WEEK,
    /** Canonical English genre sent to Audius, or null for all genres. */
    val genre: String? = null,
    val isLoading: Boolean = true,
    val tracks: List<Track> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val error: String? = null,
)

sealed interface ChartsIntent {
    data object Load : ChartsIntent
    data object Retry : ChartsIntent
    data class SelectPeriod(val period: ChartPeriod) : ChartsIntent
    data class SelectGenre(val genre: String?) : ChartsIntent
    data class PlayTrack(val track: Track) : ChartsIntent
    data class ToggleFavorite(val track: Track) : ChartsIntent
}

sealed interface ChartsEffect {
    data class Message(val text: String) : ChartsEffect
}
