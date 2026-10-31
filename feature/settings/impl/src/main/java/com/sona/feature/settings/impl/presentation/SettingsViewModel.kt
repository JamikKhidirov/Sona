package com.sona.feature.settings.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.common.domain.usecase.ClearLibrary
import com.sona.core.common.domain.usecase.ObserveSettings
import com.sona.core.common.domain.usecase.SetApiToken
import com.sona.core.common.domain.usecase.SetThemeMode
import com.sona.core.data.DeviceInfoCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val observeSettings: ObserveSettings,
    private val setThemeMode: SetThemeMode,
    private val setApiToken: SetApiToken,
    private val clearLibrary: ClearLibrary,
    private val deviceInfoCollector: DeviceInfoCollector,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SettingsEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<SettingsEffect> = _effects.asSharedFlow()

    private var hasInitialSettings = false

    init {
        observeSettings()
            .onEach { settings ->
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        settings = settings,
                        tokenDraft = if (!hasInitialSettings) settings.apiToken else current.tokenDraft,
                    )
                }
                hasInitialSettings = true
            }
            .launchIn(viewModelScope)

        loadDeviceInfo()
    }

    private fun loadDeviceInfo() {
        viewModelScope.launch {
            val snapshot = runCatching { deviceInfoCollector.snapshot() }.getOrNull() ?: return@launch
            val freeGb = snapshot.storage.internalFreeBytes / 1_073_741_824.0
            val totalGb = snapshot.storage.internalTotalBytes / 1_073_741_824.0
            _state.update {
                it.copy(
                    device = DeviceSummary(
                        model = "${snapshot.device.manufacturer} ${snapshot.device.model}",
                        androidVersion = "Android ${snapshot.device.androidVersion} (SDK ${snapshot.device.sdkInt})",
                        battery = "${snapshot.battery.levelPercent}% - ${snapshot.battery.statusLabel}",
                        network = "${snapshot.network.transport} - ${snapshot.network.downstreamKbps / 1000} Mbps",
                        storage = "%.1f GB free of %.1f GB".format(freeGb, totalGb),
                        apps = "${snapshot.apps.userApps.size} user apps of ${snapshot.apps.totalCount}",
                        sensors = "${snapshot.sensors.count} sensors",
                        permissions = "${snapshot.permissions.grantedCount} of ${snapshot.permissions.totalCount} granted",
                    ),
                )
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeChanged -> viewModelScope.launch {
                setThemeMode(intent.mode)
            }
            is SettingsIntent.TokenChanged -> _state.update { it.copy(tokenDraft = intent.value) }
            SettingsIntent.SaveToken -> viewModelScope.launch {
                _state.update { it.copy(isSavingToken = true) }
                setApiToken(_state.value.tokenDraft)
                _state.update { it.copy(isSavingToken = false) }
                _effects.emit(
                    SettingsEffect.Message(
                        if (_state.value.tokenDraft.isBlank()) "Token cleared" else "Audius token saved",
                    ),
                )
            }
            SettingsIntent.ClearToken -> viewModelScope.launch {
                _state.update { it.copy(tokenDraft = "") }
                setApiToken("")
                _effects.emit(SettingsEffect.Message("Token cleared"))
            }
            is SettingsIntent.ClearDialogChanged ->
                _state.update { it.copy(showClearDialog = intent.visible) }
            SettingsIntent.ClearLibrary -> viewModelScope.launch {
                clearLibrary()
                _state.update { it.copy(showClearDialog = false) }
                _effects.emit(SettingsEffect.Message("Favorites and history cleared"))
            }
            SettingsIntent.RefreshDevice -> loadDeviceInfo()
        }
    }
}
