package com.sona.core.common.domain.usecase.settings

import com.sona.core.common.domain.model.AppSettings
import com.sona.core.common.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ObserveSettings @Inject constructor(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<AppSettings> = repository.settings
}
