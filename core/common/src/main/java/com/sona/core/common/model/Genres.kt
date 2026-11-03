package com.sona.core.common.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class RepeatMode { OFF, ALL, ONE }

enum class CollectionSource { TRENDING, GENRE, ARTIST, PLAYLIST }

/**
 * A music genre with two names:
 *
 * - [apiName] is the canonical English value understood by the Audius API.
 * - [displayName] is the localized label shown to the user.
 *
 * Keeping both prevents the UI language from leaking into network requests:
 * a Russian label such as "Хип-хоп/Рэп" must never be sent as a genre filter.
 */
data class Genre(
    val apiName: String,
    val displayName: String,
    val emoji: String,
) {
    /** Backwards-compatible alias for the value used by the API. */
    val name: String get() = apiName
}

object Genres {
    val all: List<Genre> = listOf(
        Genre("Electronic", "Электроника", "⚡"),
        Genre("Hip-Hop/Rap", "Хип-хоп/Рэп", "🎤"),
        Genre("R&B/Soul", "R&B/Соул", "💜"),
        Genre("Pop", "Поп", "✨"),
        Genre("Rock", "Рок", "🎸"),
        Genre("Ambient", "Эмбиент", "🌙"),
        Genre("Lo-Fi", "Lo-Fi", "🏠"),
        Genre("Techno", "Техно", "🔊"),
        Genre("Jazz", "Джаз", "🎷"),
        Genre("House", "Хаус", "🏡"),
        Genre("Dubstep", "Дабстеп", "🌀"),
        Genre("Trap", "Трэп", "🔥"),
    )

    /** Finds a genre by either its API name or its localized label. */
    fun byName(name: String?): Genre? {
        if (name.isNullOrBlank()) return null
        return all.firstOrNull {
            it.apiName.equals(name, ignoreCase = true) ||
                it.displayName.equals(name, ignoreCase = true)
        }
    }

    /** Finds a genre by the exact value sent to the API. */
    fun byApiName(apiName: String?): Genre? {
        if (apiName.isNullOrBlank()) return null
        return all.firstOrNull { it.apiName.equals(apiName, ignoreCase = true) }
    }

    /** Localized label for an API genre value, falling back to the raw value. */
    fun displayNameOf(apiName: String?): String? =
        apiName?.let { byApiName(it)?.displayName ?: it }
}
