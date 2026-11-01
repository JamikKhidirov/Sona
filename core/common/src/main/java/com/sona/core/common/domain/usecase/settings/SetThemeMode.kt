package com.sona.core.common.domain.usecase.settings

import com.sona.core.common.domain.repository.SettingsRepository
import com.sona.core.common.model.ThemeMode
import javax.inject.Inject


class SetThemeMode @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}