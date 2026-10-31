package com.sona.core.common.domain.repository

import com.sona.core.common.domain.model.AppSettings
import com.sona.core.common.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setApiToken(token: String)
}
