package com.sona.core.common.domain.model

import com.sona.core.common.model.ThemeMode

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val apiToken: String = "",
) {
    val isSignedIn: Boolean get() = apiToken.isNotBlank()
}
