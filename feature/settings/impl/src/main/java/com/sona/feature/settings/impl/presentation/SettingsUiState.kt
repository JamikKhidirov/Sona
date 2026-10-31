package com.sona.feature.settings.impl.presentation

import com.sona.core.common.domain.model.AppSettings

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val tokenDraft: String = "",
    val isSavingToken: Boolean = false,
    val showClearDialog: Boolean = false,
    val device: DeviceSummary? = null,
)

data class DeviceSummary(
    val model: String,
    val androidVersion: String,
    val battery: String,
    val network: String,
    val storage: String,
    val apps: String,
    val sensors: String,
    val permissions: String,
)

sealed interface SettingsIntent {
    data class ThemeChanged(val mode: com.sona.core.common.model.ThemeMode) : SettingsIntent
    data class TokenChanged(val value: String) : SettingsIntent
    data object SaveToken : SettingsIntent
    data object ClearToken : SettingsIntent
    data class ClearDialogChanged(val visible: Boolean) : SettingsIntent
    data object ClearLibrary : SettingsIntent
    data object RefreshDevice : SettingsIntent
}

sealed interface SettingsEffect {
    data class Message(val text: String) : SettingsEffect
}
