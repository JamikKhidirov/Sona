package com.sona.core.common.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class RepeatMode { OFF, ALL, ONE }

enum class CollectionSource { TRENDING, GENRE, ARTIST, PLAYLIST }

data class Genre(val name: String, val emoji: String)

object Genres {
    val all: List<Genre> = listOf(
        Genre("Electronic", "⚡"),
        Genre("Hip-Hop/Rap", "🎤"),
        Genre("R&B/Soul", "💜"),
        Genre("Pop", "✨"),
        Genre("Rock", "🎸"),
        Genre("Lo-Fi", "🌙"),
        Genre("House", "🏠"),
        Genre("Techno", "🔊"),
        Genre("Jazz", "🎷"),
        Genre("Ambient", "🌫️"),
        Genre("Dubstep", "🌀"),
        Genre("Trap", "🔥"),
    )

    fun byName(name: String?): Genre? = all.firstOrNull { it.name.equals(name, true) }
}
