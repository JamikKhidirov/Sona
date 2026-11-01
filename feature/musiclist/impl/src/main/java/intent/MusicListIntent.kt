package intent

import com.sona.core.common.model.Track


sealed interface MusicListIntent {
    data class Load(val source: String, val id: String, val title: String) : MusicListIntent
    data object Retry : MusicListIntent
    data object PlayAll : MusicListIntent
    data class PlayTrack(val track: Track) : MusicListIntent
    data class ToggleFavorite(val track: Track) : MusicListIntent
}