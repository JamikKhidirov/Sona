package com.sona.core.common.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sona.core.common.di.ApplicationContext
import com.sona.core.common.data.local.PreferenceKeys
import com.sona.core.common.data.local.sonaDataStore
import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson,
) : LibraryRepository {

    private val dataStore = context.sonaDataStore
    private val favoritesKey = stringPreferencesKey(PreferenceKeys.FAVORITES)
    private val recentlyPlayedKey = stringPreferencesKey(PreferenceKeys.RECENTLY_PLAYED)
    private val listType = object : TypeToken<List<Track>>() {}.type

    override val favorites: Flow<List<Track>> = dataStore.data.map { preferences ->
        decode(preferences[favoritesKey])
    }

    override val recentlyPlayed: Flow<List<Track>> = dataStore.data.map { preferences ->
        decode(preferences[recentlyPlayedKey])
    }

    override val favoriteIds: Flow<Set<String>> = favorites.map { tracks ->
        tracks.mapTo(LinkedHashSet()) { it.id }
    }

    override suspend fun toggleFavorite(track: Track) {
        dataStore.edit { preferences ->
            val current = decode(preferences[favoritesKey]).toMutableList()
            val removed = current.removeAll { it.id == track.id }
            if (!removed) current.add(0, track)
            preferences[favoritesKey] = gson.toJson(current)
        }
    }

    override suspend fun isFavorite(trackId: String): Boolean =
        favorites.first().any { it.id == trackId }

    override suspend fun addRecentlyPlayed(track: Track) {
        dataStore.edit { preferences ->
            val current = decode(preferences[recentlyPlayedKey])
                .filterNot { it.id == track.id }
                .toMutableList()
            current.add(0, track)
            preferences[recentlyPlayedKey] = gson.toJson(current.take(25))
        }
    }

    override suspend fun clearLibrary() {
        dataStore.edit { preferences ->
            preferences.remove(favoritesKey)
            preferences.remove(recentlyPlayedKey)
        }
    }

    private fun decode(json: String?): List<Track> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<Track>>(json, listType).orEmpty()
        }.getOrDefault(emptyList())
    }
}
