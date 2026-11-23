package com.sona.feature.home.impl.presentation

import com.sona.core.audio.AudioPlayer
import com.sona.core.audio.PlayerState
import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.domain.usecase.music.GetTrendingTracks
import com.sona.core.common.domain.usecase.library.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.library.ToggleFavorite
import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.SearchResult
import com.sona.core.common.model.Track
import intent.HomeIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import model.HomeEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val artist = Artist(id = "u1", handle = "sona", name = "Sona")
    private val tracks = listOf(
        Track(id = "t1", title = "One", artist = artist),
        Track(id = "t2", title = "Two", artist = artist),
        Track(id = "t3", title = "Three", artist = artist),
    )

    private val musicRepository = FakeMusicRepository()
    private val libraryRepository = FakeLibraryRepository()
    private val audioPlayer = FakeAudioPlayer()

    private fun viewModel() = HomeViewModel(
        getTrendingTracks = GetTrendingTracks(musicRepository),
        observeFavoriteIds = ObserveFavoriteIds(libraryRepository),
        toggleFavorite = ToggleFavorite(libraryRepository),
        audioPlayer = audioPlayer,
    )

    @Test
    fun initial_load_fills_state_and_derived_sections() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(tracks, state.tracks)
        assertEquals(tracks.first(), state.featured)
        assertEquals(tracks.drop(1), state.trending)
        assertEquals(tracks.drop(1).take(10), state.freshDrops)
        assertEquals(listOf(artist), state.artists)
        assertNull(state.error)
        assertNull(musicRepository.lastGenre)
        assertEquals(50, musicRepository.lastLimit)
    }

    @Test
    fun empty_result_shows_empty_message() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(emptyList())

        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(
            "Пока не найдено ни одного трека, соответствующего этой атмосфере.",
            viewModel.state.value.error,
        )
        assertNull(viewModel.state.value.featured)
    }

    @Test
    fun failure_uses_throwable_message_and_falls_back_to_default() =
        runTest(mainDispatcherRule.dispatcher) {
            musicRepository.trending = Result.failure(IllegalStateException("network down"))

            val viewModel = viewModel()
            advanceUntilIdle()

            assertEquals("network down", viewModel.state.value.error)
            assertFalse(viewModel.state.value.isLoading)

            musicRepository.trending = Result.failure(IllegalStateException())
            viewModel.onIntent(HomeIntent.Retry)
            advanceUntilIdle()

            assertEquals(
                "Не удалось дозвониться до Audius. Проверьте соединение.",
                viewModel.state.value.error,
            )
        }

    @Test
    fun selecting_genre_reloads_and_selecting_same_genre_clears_it() =
        runTest(mainDispatcherRule.dispatcher) {
            musicRepository.trending = Result.success(tracks)
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onIntent(HomeIntent.SelectGenre("Lo-Fi"))
            advanceUntilIdle()

            assertEquals("Lo-Fi", viewModel.state.value.selectedGenre)
            assertEquals("Lo-Fi", musicRepository.lastGenre)

            viewModel.onIntent(HomeIntent.SelectGenre("Lo-Fi"))
            advanceUntilIdle()

            assertNull(viewModel.state.value.selectedGenre)
            assertNull(musicRepository.lastGenre)
        }

    @Test
    fun playing_track_queues_all_tracks_from_its_position() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.PlayTrack(tracks[1]))

        assertEquals(tracks, audioPlayer.lastQueue)
        assertEquals(1, audioPlayer.lastStartIndex)
    }

    @Test
    fun playing_current_track_toggles_play_pause() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)
        val viewModel = viewModel()
        advanceUntilIdle()
        audioPlayer.state.value = PlayerState(queue = tracks, currentIndex = 0, isPlaying = true)
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.PlayTrack(tracks[0]))

        assertEquals(1, audioPlayer.toggleCount)
        assertTrue(viewModel.state.value.isPlaying)
        assertEquals("t1", viewModel.state.value.currentTrackId)
    }

    @Test
    fun play_featured_uses_the_first_track() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.PlayFeatured)

        assertEquals(tracks, audioPlayer.lastQueue)
        assertEquals(0, audioPlayer.lastStartIndex)
    }

    @Test
    fun toggle_favorite_emits_message_and_updates_repository() =
        runTest(mainDispatcherRule.dispatcher) {
            musicRepository.trending = Result.success(tracks)
            libraryRepository.favoriteIdsFlow.value = emptySet()
            val viewModel = viewModel()
            advanceUntilIdle()

            val effects = mutableListOf<HomeEffect>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.effects.collect { effects += it }
            }
            runCurrent()

            viewModel.onIntent(HomeIntent.ToggleFavorite(tracks[0]))
            advanceUntilIdle()

            assertEquals(tracks[0], libraryRepository.lastToggled)
            assertEquals(listOf(HomeEffect.Message("Сохранено в избранном")), effects)

            libraryRepository.favoriteIdsFlow.value = setOf("t1")
            advanceUntilIdle()
            viewModel.onIntent(HomeIntent.ToggleFavorite(tracks[0]))
            advanceUntilIdle()

            assertEquals(HomeEffect.Message("Удалено из избранного"), effects.last())
        }

    @Test
    fun retry_reloads_current_genre() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.SelectGenre("Pop"))
        advanceUntilIdle()
        viewModel.onIntent(HomeIntent.Retry)
        advanceUntilIdle()

        assertEquals("Pop", musicRepository.lastGenre)
    }

    private class FakeMusicRepository : MusicRepository {
        var trending: Result<List<Track>> = Result.success(emptyList())
        var lastGenre: String? = null
        var lastLimit: Int? = null

        override suspend fun getTrending(
            genre: String?,
            time: String,
            limit: Int,
            offset: Int,
        ): Result<List<Track>> {
            lastGenre = genre
            lastLimit = limit
            return trending
        }

        override suspend fun search(query: String, limit: Int): Result<SearchResult> = error("unused")

        override suspend fun getTrack(trackId: String): Result<Track> = error("unused")

        override suspend fun getUser(userId: String): Result<Artist> = error("unused")

        override suspend fun getUserTracks(userId: String, limit: Int, offset: Int): Result<List<Track>> =
            error("unused")

        override suspend fun getPlaylist(playlistId: String): Result<Playlist> = error("unused")

        override suspend fun getPlaylistTracks(playlistId: String): Result<List<Track>> = error("unused")

        override suspend fun getCurrentUser(): Result<Artist> = error("unused")
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
