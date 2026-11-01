package model

import com.sona.core.common.model.ThemeMode




data class AppUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLoading: Boolean = true,
)
