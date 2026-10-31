package com.sona.feature.navigation.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.common.domain.usecase.ObserveSettings
import com.sona.core.common.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AppUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLoading: Boolean = true,
)

/**
 * Owns app-wide presentation state such as the persisted theme preference.
 */
class AppViewModel @Inject constructor(
    observeSettings: ObserveSettings,
) : ViewModel() {

    val state: StateFlow<AppUiState> = observeSettings()
        .map { AppUiState(themeMode = it.themeMode, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AppUiState(),
        )
}
