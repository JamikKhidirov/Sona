package com.sona.core.common.domain.usecase

import com.sona.core.common.domain.model.AppSettings
import com.sona.core.common.domain.repository.SettingsRepository
import com.sona.core.common.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSettings @Inject constructor(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<AppSettings> = repository.settings
}

class SetThemeMode @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}

class SetApiToken @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(token: String) = repository.setApiToken(token)
}
