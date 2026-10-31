package com.sona.feature.player.impl.presentation

import com.sona.core.audio.AudioPlayer
import com.sona.core.audio.PlayerState
import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.domain.usecase.library.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.library.ToggleFavorite
import com.sona.core.common.model.Artist
import com.sona.core.common.model.RepeatMode
import com.sona.core.common.model.Track
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val artist = Artist(id = "u1", handle = "sona", name = "Sona")
    private val tracks = listOf(
        Track(id = "t1", title = "One", artist = artist),
        Track(id = "t2", title = "Two", artist = artist),
    )

    private val audioPlayer = FakeAudioPlayer()
    private val libraryRepository = FakeLibraryRepository()

    private fun viewModel() = PlayerViewModel(
        audioPlayer = audioPlayer,
        observeFavoriteIds = ObserveFavoriteIds(libraryRepository),
        toggleFavorite = ToggleFavorite(libraryRepository),
    )

    @Test
    fun ui_state_derives_track_and_favorite_flag() {
        val state = PlayerUiState(
            player = PlayerState(queue = tracks, currentIndex = 1),
            favoriteIds = setOf("t2"),
        )

        assertEquals("t2", state.track?.id)
        assertTrue(state.isFavorite)

        assertFalse(state.copy(favoriteIds = emptySet()).isFavorite)
        assertFalse(PlayerUiState().isFavorite)
    }

    @Test
    fun state_combines_player_and_favorites_while_subscribed() =
        runTest(mainDispatcherRule.dispatcher) {
            audioPlayer.state.value = PlayerState(queue = tracks, currentIndex = 0)
            val viewModel = viewModel()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect { }
            }
            advanceUntilIdle()

            audioPlayer.state.value = PlayerState(queue = tracks, currentIndex = 1, isPlaying = true)
            libraryRepository.favoriteIdsFlow.value = setOf("t2")
            advanceUntilIdle()

            assertEquals("t2", viewModel.state.value.track?.id)
            assertTrue(viewModel.state.value.player.isPlaying)
            assertTrue(viewModel.state.value.isFavorite)
        }

    @Test
    fun transport_intents_delegate_to_audio_player() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(PlayerIntent.TogglePlayPause)
        viewModel.onIntent(PlayerIntent.Next)
        viewModel.onIntent(PlayerIntent.Previous)
        viewModel.onIntent(PlayerIntent.SeekTo(1_500L))
        viewModel.onIntent(PlayerIntent.CycleRepeat)
        viewModel.onIntent(PlayerIntent.SelectQueueItem(2))

        assertEquals(1, audioPlayer.toggleCount)
        assertEquals(1, audioPlayer.nextCount)
        assertEquals(1, audioPlayer.previousCount)
        assertEquals(1_500L, audioPlayer.lastSeek)
        assertEquals(1, audioPlayer.repeatCount)
        assertEquals(2, audioPlayer.lastSelectedIndex)
    }

    @Test
    fun shuffle_toggles_the_current_flag() = runTest(mainDispatcherRule.dispatcher) {
        audioPlayer.state.value = PlayerState(shuffle = false)
        val viewModel = viewModel()

        viewModel.onIntent(PlayerIntent.ToggleShuffle)
        assertEquals(true, audioPlayer.lastShuffle)

        audioPlayer.state.value = PlayerState(shuffle = true)
        val shuffledViewModel = viewModel()
        shuffledViewModel.onIntent(PlayerIntent.ToggleShuffle)
        assertEquals(false, audioPlayer.lastShuffle)
    }

    @Test
    fun toggle_favorite_uses_current_track() = runTest(mainDispatcherRule.dispatcher) {
        audioPlayer.state.value = PlayerState(queue = tracks, currentIndex = 0)
        val viewModel = viewModel()

        viewModel.onIntent(PlayerIntent.ToggleFavorite)
        advanceUntilIdle()

        assertEquals(tracks[0], libraryRepository.lastToggled)
    }

    @Test
    fun toggle_favorite_does_nothing_without_track() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(PlayerIntent.ToggleFavorite)
        advanceUntilIdle()

        assertEquals(null, libraryRepository.lastToggled)
    }

    @Test
    fun repeat_mode_is_exposed_from_player_state() {
        val state = PlayerUiState(
            player = PlayerState(queue = tracks, repeatMode = RepeatMode.ALL),
        )

        assertEquals(RepeatMode.ALL, state.player.repeatMode)
        assertTrue(state.player.hasNext)
    }

    private class FakeLibraryRepository : LibraryRepository {
        val favoriteIdsFlow = MutableStateFlow<Set<String>>(emptySet())
        override val favorites: Flow<List<Track>> = flowOf(emptyList())
        override val favoriteIds: Flow<Set<String>> = favoriteIdsFlow
        override val recentlyPlayed: Flow<List<Track>> = flowOf(emptyList())
        var lastToggled: Track? = null

        override suspend fun toggleFavorite(track: Track) {
            lastToggled = track
        }

        override suspend fun isFavorite(trackId: String): Boolean = false

        override suspend fun addRecentlyPlayed(track: Track) = Unit

        override suspend fun clearLibrary() = Unit
    }

    private class FakeAudioPlayer : AudioPlayer {
        override val state = MutableStateFlow(PlayerState())
        var toggleCount = 0
        var nextCount = 0
        var previousCount = 0
        var repeatCount = 0
        var lastSeek: Long? = null
        var lastShuffle: Boolean? = null
        var lastSelectedIndex: Int? = null

        override fun playQueue(tracks: List<Track>, startIndex: Int) = Unit

        override fun togglePlayPause() {
            toggleCount++
        }

        override fun play() = Unit
        override fun pause() = Unit

        override fun next() {
            nextCount++
        }

        override fun previous() {
            previousCount++
        }

        override fun seekTo(positionMs: Long) {
            lastSeek = positionMs
        }

        override fun selectQueueItem(index: Int) {
            lastSelectedIndex = index
        }

        override fun addToQueue(track: Track) = Unit

        override fun setShuffle(enabled: Boolean) {
            lastShuffle = enabled
        }

        override fun cycleRepeatMode() {
            repeatCount++
        }
    }
}
