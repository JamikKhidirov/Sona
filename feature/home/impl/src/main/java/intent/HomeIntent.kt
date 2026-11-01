package intent

import com.sona.core.common.model.Track

sealed interface HomeIntent {
    data class SelectGenre(val genre: String?) : HomeIntent
    data class PlayTrack(val track: Track) : HomeIntent
    data class ToggleFavorite(val track: Track) : HomeIntent
    data object PlayFeatured : HomeIntent
    data object Retry : HomeIntent
}