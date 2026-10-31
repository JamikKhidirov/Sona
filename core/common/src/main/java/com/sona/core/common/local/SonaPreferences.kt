package com.sona.core.common.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

internal val Context.sonaDataStore: DataStore<Preferences> by preferencesDataStore(name = "sona_prefs")

internal object PreferenceKeys {
    const val THEME_MODE = "theme_mode"
    const val API_TOKEN = "api_token"
    const val FAVORITES = "favorites_json"
    const val RECENTLY_PLAYED = "recently_played_json"
}
