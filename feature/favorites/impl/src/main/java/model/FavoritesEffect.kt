package model



sealed interface FavoritesEffect {
    data class Message(val text: String) : FavoritesEffect
}
