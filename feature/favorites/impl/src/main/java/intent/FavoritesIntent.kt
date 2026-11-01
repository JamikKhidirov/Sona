package intent

import com.sona.core.common.model.Track


sealed interface FavoritesIntent {
    data object PlayAll : FavoritesIntent
    data class PlayTrack(val track: Track) : FavoritesIntent
    data class ToggleFavorite(val track: Track) : FavoritesIntent
    data object ClearFavorites : FavoritesIntent
}