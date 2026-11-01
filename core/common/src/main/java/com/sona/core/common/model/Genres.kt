package com.sona.core.common.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class RepeatMode { OFF, ALL, ONE }

enum class CollectionSource { TRENDING, GENRE, ARTIST, PLAYLIST }

data class Genre(val name: String, val emoji: String)

object Genres {
    val all: List<Genre> = listOf(
        Genre("Зажигательный", "⚡"),
        Genre("Хип-Хоп/Реп", "🎤"),
        Genre("R&B/Душа", "💜"),
        Genre("Поп", "✨"),
        Genre("Рок", "🎸"),
        Genre("Для сна", "🌙"),
        Genre("Ваш вайб", "🏠"),
        Genre("Техно", "🔊"),
        Genre("Джаз", "🎷"),
        Genre("Окружающий", "🌫️"),
        Genre("Дабстеп", "🌀"),
        Genre("Трэп", "🔥"),
    )

    fun byName(name: String?): Genre? = all.firstOrNull { it.name.equals(name, true) }
}
