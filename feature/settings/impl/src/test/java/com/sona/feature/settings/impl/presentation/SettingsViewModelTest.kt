package com.sona.feature.settings.impl.presentation

import com.sona.core.common.domain.model.AppSettings
import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.domain.repository.SettingsRepository
import com.sona.core.common.domain.usecase.library.ClearLibrary
import com.sona.core.common.domain.usecase.settings.ObserveSettings
import com.sona.core.common.domain.usecase.settings.SetApiToken
import com.sona.core.common.domain.usecase.settings.SetThemeMode
import com.sona.core.common.model.ThemeMode
import com.sona.core.common.model.Track
import com.sona.core.data.DeviceSnapshot
import com.sona.core.data.DeviceSnapshotProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository = FakeSettingsRepository()
    private val libraryRepository = FakeLibraryRepository()
    private val deviceProvider = FakeDeviceSnapshotProvider()

    private fun viewModel() = SettingsViewModel(
        observeSettings = ObserveSettings(settingsRepository),
        setThemeMode = SetThemeMode(settingsRepository),
        setApiToken = SetApiToken(settingsRepository),
        clearLibrary = ClearLibrary(libraryRepository),
        deviceInfoCollector = deviceProvider,
    )

    @Test
    fun initial_settings_fill_state_and_only_first_token_reaches_the_draft() =
        runTest(mainDispatcherRule.dispatcher) {
            settingsRepository.settingsFlow.value = AppSettings(
                themeMode = ThemeMode.DARK,
                apiToken = "stored-token",
            )

            val viewModel = viewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isLoading)
            assertEquals(ThemeMode.DARK, state.settings.themeMode)
            assertEquals("stored-token", state.tokenDraft)

            settingsRepository.settingsFlow.value = AppSettings(
                themeMode = ThemeMode.LIGHT,
                apiToken = "other-token",
            )
            advanceUntilIdle()

            assertEquals("stored-token", viewModel.state.value.tokenDraft)
            assertEquals(ThemeMode.LIGHT, viewModel.state.value.settings.themeMode)
        }

    @Test
    fun theme_change_delegates_to_repository() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.ThemeChanged(ThemeMode.LIGHT))
        advanceUntilIdle()

        assertEquals(ThemeMode.LIGHT, settingsRepository.lastThemeMode)
    }

    @Test
    fun token_draft_updates_and_save_persists_it() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        val effects = collectEffects(viewModel)

        viewModel.onIntent(SettingsIntent.TokenChanged("abc"))
        viewModel.onIntent(SettingsIntent.SaveToken)
        advanceUntilIdle()

        assertEquals("abc", viewModel.state.value.tokenDraft)
        assertEquals("abc", settingsRepository.lastToken)
        assertFalse(viewModel.state.value.isSavingToken)
        assertEquals(listOf(SettingsEffect.Message("Audius token saved")), effects)
    }

    @Test
    fun saving_blank_token_reports_cleared() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        val effects = collectEffects(viewModel)

        viewModel.onIntent(SettingsIntent.TokenChanged(""))
        viewModel.onIntent(SettingsIntent.SaveToken)
        advanceUntilIdle()

        assertEquals("", settingsRepository.lastToken)
        assertEquals(listOf(SettingsEffect.Message("Token cleared")), effects)
    }

    @Test
    fun clear_token_clears_draft_and_persists_empty_token() =
        runTest(mainDispatcherRule.dispatcher) {
            settingsRepository.settingsFlow.value = AppSettings(apiToken = "stored")
            val viewModel = viewModel()
            advanceUntilIdle()
            val effects = collectEffects(viewModel)

            viewModel.onIntent(SettingsIntent.ClearToken)
            advanceUntilIdle()

            assertEquals("", viewModel.state.value.tokenDraft)
            assertEquals("", settingsRepository.lastToken)
            assertEquals(listOf(SettingsEffect.Message("Token cleared")), effects)
        }

    @Test
    fun clear_dialog_visibility_is_stored() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.ClearDialogChanged(true))
        assertTrue(viewModel.state.value.showClearDialog)

        viewModel.onIntent(SettingsIntent.ClearDialogChanged(false))
        assertFalse(viewModel.state.value.showClearDialog)
    }

    @Test
    fun clear_library_clears_data_hides_dialog_and_reports() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()
            val effects = collectEffects(viewModel)

            viewModel.onIntent(SettingsIntent.ClearDialogChanged(true))
            viewModel.onIntent(SettingsIntent.ClearLibrary)
            advanceUntilIdle()

            assertEquals(1, libraryRepository.clearCount)
            assertFalse(viewModel.state.value.showClearDialog)
            assertEquals(listOf(SettingsEffect.Message("Favorites and history cleared")), effects)
        }

    @Test
    fun device_provider_failure_leaves_device_summary_empty() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()

            assertNull(viewModel.state.value.device)
        }

    @Test
    fun refresh_device_retries_the_provider() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(SettingsIntent.RefreshDevice)
        advanceUntilIdle()

        assertEquals(2, deviceProvider.calls)
        assertNull(viewModel.state.value.device)
    }

    private fun kotlinx.coroutines.test.TestScope.collectEffects(
        viewModel: SettingsViewModel,
    ): List<SettingsEffect> {
        val effects = mutableListOf<SettingsEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }
        runCurrent()
        return effects
    }

    private class FakeSettingsRepository : SettingsRepository {
        val settingsFlow = MutableStateFlow(AppSettings())
        override val settings: Flow<AppSettings> = settingsFlow
        var lastThemeMode: ThemeMode? = null
        var lastToken: String? = null

        override suspend fun setThemeMode(mode: ThemeMode) {
            lastThemeMode = mode
        }

        override suspend fun setApiToken(token: String) {
            lastToken = token
        }
    }

    private class FakeLibraryRepository : LibraryRepository {
        override val favorites: Flow<List<Track>> = flowOf(emptyList())
        override val favoriteIds: Flow<Set<String>> = flowOf(emptySet())
        override val recentlyPlayed: Flow<List<Track>> = flowOf(emptyList())
        var clearCount = 0

        override suspend fun toggleFavorite(track: Track) = Unit

        override suspend fun isFavorite(trackId: String): Boolean = false

        override suspend fun addRecentlyPlayed(track: Track) = Unit

        override suspend fun clearLibrary() {
            clearCount++
        }
    }

    private class FakeDeviceSnapshotProvider : DeviceSnapshotProvider {
        var calls = 0

        override suspend fun snapshot(): DeviceSnapshot {
            calls++
            throw IllegalStateException("device unavailable")
        }
    }
}
