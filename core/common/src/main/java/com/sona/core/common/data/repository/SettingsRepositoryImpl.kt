package com.sona.core.common.data.repository

import com.google.gson.Gson
import com.sona.core.common.auth.TokenProvider
import com.sona.core.common.coroutines.DispatcherProvider
import com.sona.core.common.di.ApplicationContext
import com.sona.core.common.di.ApplicationScope
import com.sona.core.common.data.local.PreferenceKeys
import com.sona.core.common.data.local.sonaDataStore
import com.sona.core.common.domain.model.AppSettings
import com.sona.core.common.domain.repository.SettingsRepository
import com.sona.core.common.model.ThemeMode
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext context: android.content.Context,
    @ApplicationScope private val appScope: CoroutineScope,
    private val dispatchers: DispatcherProvider,
    private val gson: Gson,
) : SettingsRepository, TokenProvider {

    private val dataStore = context.sonaDataStore
    private val themeKey = stringPreferencesKey(PreferenceKeys.THEME_MODE)
    private val tokenKey = stringPreferencesKey(PreferenceKeys.API_TOKEN)

    @Volatile
    private var cachedToken: String? = null

    override val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            themeMode = runCatching {
                ThemeMode.valueOf(preferences[themeKey] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
            apiToken = preferences[tokenKey].orEmpty(),
        )
    }

    init {
        appScope.launch(dispatchers.io) {
            settings.collect { cachedToken = it.apiToken.takeIf(String::isNotBlank) }
        }
    }

    override fun token(): String? = cachedToken

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[themeKey] = mode.name }
    }

    override suspend fun setApiToken(token: String) {
        val normalized = token.trim()
        cachedToken = normalized.takeIf(String::isNotBlank)
        dataStore.edit { preferences ->
            if (normalized.isBlank()) preferences.remove(tokenKey) else preferences[tokenKey] = normalized
        }
    }
}
