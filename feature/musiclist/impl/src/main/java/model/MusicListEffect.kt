package model





sealed interface MusicListEffect {
    data class Message(val text: String) : MusicListEffect
}
