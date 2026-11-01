package model

sealed interface HomeEffect {
    data class Message(val text: String) : HomeEffect
}