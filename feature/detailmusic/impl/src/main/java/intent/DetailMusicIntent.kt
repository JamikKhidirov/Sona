package intent

import com.sona.core.common.model.Track




sealed interface DetailMusicIntent {
    data class Load(val trackId: String) : DetailMusicIntent
    data class Play(val track: Track) : DetailMusicIntent
    data class ToggleFavorite(val track: Track) : DetailMusicIntent
    data class AddToQueue(val track: Track) : DetailMusicIntent
    data object Retry : DetailMusicIntent
}