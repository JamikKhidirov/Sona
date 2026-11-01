package model



sealed interface DetailMusicEffect {
    data class Message(val text: String) : DetailMusicEffect
}
