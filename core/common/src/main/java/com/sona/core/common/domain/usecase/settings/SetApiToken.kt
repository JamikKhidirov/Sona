package com.sona.core.common.domain.usecase.settings

import com.sona.core.common.domain.repository.SettingsRepository
import javax.inject.Inject

class SetApiToken @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(token: String) = repository.setApiToken(token)
}