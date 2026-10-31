package com.sona.feature.favorites.impl.presentation

import com.sona.core.audio.AudioPlayer
import com.sona.core.audio.PlayerState
import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.domain.usecase.ClearLibrary
import com.sona.core.common.domain.usecase.ObserveFavorites
import com.sona.core.common.domain.usecase.ToggleFavorite
import com.sona.core.common.model.Artist
import com.sona.core.common.model.Track
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
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val artist = Artist(id = "u1", handle = "sona", name = "Sona")
    private val tracks = listOf(
        Track(id = "t1", title = "One", artist = artist, durationSeconds = 100),
        Track(id = "t2", title = "Two", artist = artist, durationSeconds = 200),
    )

    private val libraryRepository = FakeLibraryRepository()
    private val audioPlayer = FakeAudioPlayer()

    private fun viewModel() = FavoritesViewModel(
        observeFavorites = ObserveFavorites(libraryRepository),
        toggleFavorite = ToggleFavorite(libraryRepository),
        clearLibrary = ClearLibrary(libraryRepository),
        audioPlayer = audioPlayer,
    )

    @Test
    fun favorites_flow_fills_state_and_total_duration() = runTest(mainDispatcherRule.dispatcher) {
        libraryRepository.favoritesFlow.value = tracks
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(tracks, state.tracks)
        assertEquals(setOf("t1", "t2"), state.favoriteIds)
        assertEquals(300, state.totalDurationSeconds)
    }

    @Test
    fun play_all_starts_from_first_track() = runTest(mainDispatcherRule.dispatcher) {
        libraryRepository.favoritesFlow.value = tracks
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(FavoritesIntent.PlayAll)

        assertEquals(tracks, audioPlayer.lastQueue)
        assertEquals(0, audioPlayer.lastStartIndex)
    }

    @Test
    fun play_all_does_nothing_for_empty_library() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(FavoritesIntent.PlayAll)

        assertNull(audioPlayer.lastQueue)
    }

    @Test
    fun play_track_starts_queue_at_its_index() = runTest(mainDispatcherRule.dispatcher) {
        libraryRepository.favoritesFlow.value = tracks
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(FavoritesIntent.PlayTrack(tracks[1]))

        assertEquals(tracks, audioPlayer.lastQueue)
        assertEquals(1, audioPlayer.lastStartIndex)
    }

    @Test
    fun playing_current_track_toggles_play_pause() = runTest(mainDispatcherRule.dispatcher) {
        libraryRepository.favoritesFlow.value = tracks
        val viewModel = viewModel()
        audioPlayer.state.value = PlayerState(queue = tracks, currentIndex = 0, isPlaying = true)
        advanceUntilIdle()

        viewModel.onIntent(FavoritesIntent.PlayTrack(tracks[0]))

        assertEquals(1, audioPlayer.toggleCount)
        assertNull(audioPlayer.lastQueue)
        assertTrue(viewModel.state.value.isPlaying)
        assertEquals("t1", viewModel.state.value.currentTrackId)
    }

    @Test
    fun toggle_favorite_delegates_and_emits_message() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        val effects = mutableListOf<FavoritesEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }
        runCurrent()

        viewModel.onIntent(FavoritesIntent.ToggleFavorite(tracks[0]))
        advanceUntilIdle()

        assertEquals(tracks[0], libraryRepository.lastToggled)
        assertEquals(listOf(FavoritesEffect.Message("Removed from favorites")), effects)
    }

    @Test
    fun clear_favorites_clears_library_and_emits_message() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        val effects = mutableListOf<FavoritesEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }
        runCurrent()

        viewModel.onIntent(FavoritesIntent.ClearFavorites)
        advanceUntilIdle()

        assertEquals(1, libraryRepository.clearCount)
        assertEquals(listOf(FavoritesEffect.Message("Library cleared")), effects)
    }

    private class FakeLibraryRepository : LibraryRepository {
        val favoritesFlow = MutableStateFlow<List<Track>>(emptyList())
        override val favorites: Flow<List<Track>> = favoritesFlow
        override val favoriteIds: Flow<Set<String>> = flowOf(emptySet())
        override val recentlyPlayed: Flow<List<Track>> = flowOf(emptyList())
        var lastToggled: Track? = null
        var clearCount = 0

        override suspend fun toggleFavorite(track: Track) {
            lastToggled = track
        }

        override suspend fun isFavorite(trackId: String): Boolean = false

        override suspend fun addRecentlyPlayed(track: Track) = Unit

        override suspend fun clearLibrary() {
            clearCount++
        }
    }

    private class FakeAudioPlayer : AudioPlayer {
        override val state = MutableStateFlow(PlayerState())
        var lastQueue: List<Track>? = null
        var lastStartIndex: Int? = null
        var toggleCount = 0

        override fun playQueue(tracks: List<Track>, startIndex: Int) {
            lastQueue = tracks
            lastStartIndex = startIndex
        }

        override fun togglePlayPause() {
            toggleCount++
        }

        override fun play() = Unit
        override fun pause() = Unit
        override fun next() = Unit
        override fun previous() = Unit
        override fun seekTo(positionMs: Long) = Unit
        override fun selectQueueItem(index: Int) = Unit
        override fun addToQueue(track: Track) = Unit
        override fun setShuffle(enabled: Boolean) = Unit
        override fun cycleRepeatMode() = Unit
    }
}
