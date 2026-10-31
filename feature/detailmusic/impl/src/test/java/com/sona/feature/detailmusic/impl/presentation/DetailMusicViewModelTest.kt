package com.sona.feature.detailmusic.impl.presentation

import com.sona.core.audio.AudioPlayer
import com.sona.core.audio.PlayerState
import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.domain.usecase.GetTrack
import com.sona.core.common.domain.usecase.GetTrendingTracks
import com.sona.core.common.domain.usecase.library.ObserveFavorites
import com.sona.core.common.domain.usecase.library.ToggleFavorite
import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.SearchResult
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
class DetailMusicViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val artist = Artist(id = "u1", handle = "sona", name = "Sona")
    private val track = Track(id = "t1", title = "One", artist = artist, genre = "Lo-Fi")
    private val related = listOf(
        Track(id = "t2", title = "Two", artist = artist),
        Track(id = "t1", title = "One", artist = artist),
        Track(id = "t3", title = "Three", artist = artist),
    )

    private val musicRepository = FakeMusicRepository()
    private val libraryRepository = FakeLibraryRepository()
    private val audioPlayer = FakeAudioPlayer()

    private fun viewModel() = DetailMusicViewModel(
        getTrack = GetTrack(musicRepository),
        getTrendingTracks = GetTrendingTracks(musicRepository),
        observeFavorites = ObserveFavorites(libraryRepository),
        toggleFavorite = ToggleFavorite(libraryRepository),
        audioPlayer = audioPlayer,
    )

    @Test
    fun load_sets_track_and_related_without_duplicates() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.track = Result.success(track)
        musicRepository.trending = Result.success(related)

        val viewModel = viewModel()
        viewModel.onIntent(DetailMusicIntent.Load("t1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(track, state.track)
        assertEquals(listOf("t2", "t3"), state.related.map(Track::id))
        assertNull(state.error)
        assertEquals("t1", musicRepository.lastTrackId)
        assertEquals("Lo-Fi", musicRepository.lastGenre)
        assertEquals(12, musicRepository.lastLimit)
    }

    @Test
    fun load_failure_shows_message_and_retry_reloads() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.track = Result.failure(IllegalStateException("not found"))

        val viewModel = viewModel()
        viewModel.onIntent(DetailMusicIntent.Load("t1"))
        advanceUntilIdle()

        assertEquals("not found", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)

        musicRepository.track = Result.success(track)
        musicRepository.trending = Result.success(emptyList())
        viewModel.onIntent(DetailMusicIntent.Retry)
        advanceUntilIdle()

        assertEquals(track, viewModel.state.value.track)
        assertEquals(2, musicRepository.trackCalls)
    }

    @Test
    fun load_failure_without_message_uses_default_text() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.track = Result.failure(IllegalStateException())

        val viewModel = viewModel()
        viewModel.onIntent(DetailMusicIntent.Load("t1"))
        advanceUntilIdle()

        assertEquals("Couldn't load this track.", viewModel.state.value.error)
    }

    @Test
    fun loading_the_same_track_twice_is_ignored() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.track = Result.success(track)
        musicRepository.trending = Result.success(emptyList())
        val viewModel = viewModel()

        viewModel.onIntent(DetailMusicIntent.Load("t1"))
        advanceUntilIdle()
        viewModel.onIntent(DetailMusicIntent.Load("t1"))
        advanceUntilIdle()

        assertEquals(1, musicRepository.trackCalls)
    }

    @Test
    fun favorites_flow_marks_track_as_favorite() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.track = Result.success(track)
        musicRepository.trending = Result.success(emptyList())
        val viewModel = viewModel()
        viewModel.onIntent(DetailMusicIntent.Load("t1"))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isFavorite)

        libraryRepository.favoritesFlow.value = listOf(track)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isFavorite)
    }

    @Test
    fun play_starts_queue_with_track_then_related_tracks() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.track = Result.success(track)
        musicRepository.trending = Result.success(related)
        val viewModel = viewModel()
        viewModel.onIntent(DetailMusicIntent.Load("t1"))
        advanceUntilIdle()

        viewModel.onIntent(DetailMusicIntent.Play(track))

        assertEquals(listOf("t1", "t2", "t3"), audioPlayer.lastQueue?.map(Track::id))
        assertEquals(0, audioPlayer.lastStartIndex)
        assertEquals(0, audioPlayer.toggleCount)
    }

    @Test
    fun playing_the_current_track_toggles_instead_of_restarting() =
        runTest(mainDispatcherRule.dispatcher) {
            musicRepository.track = Result.success(track)
            musicRepository.trending = Result.success(emptyList())
            val viewModel = viewModel()
            viewModel.onIntent(DetailMusicIntent.Load("t1"))
            advanceUntilIdle()
            audioPlayer.state.value = PlayerState(queue = listOf(track), currentIndex = 0, isPlaying = true)
            advanceUntilIdle()

            viewModel.onIntent(DetailMusicIntent.Play(track))

            assertEquals(1, audioPlayer.toggleCount)
            assertNull(audioPlayer.lastQueue)
            assertTrue(viewModel.state.value.isCurrent)
        }

    @Test
    fun toggle_favorite_emits_saved_or_removed_message() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.track = Result.success(track)
        musicRepository.trending = Result.success(emptyList())
        val viewModel = viewModel()
        viewModel.onIntent(DetailMusicIntent.Load("t1"))
        advanceUntilIdle()

        val effects = mutableListOf<DetailMusicEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }
        runCurrent()

        viewModel.onIntent(DetailMusicIntent.ToggleFavorite(track))
        advanceUntilIdle()
        assertEquals(DetailMusicEffect.Message("Saved to favorites"), effects.last())

        libraryRepository.favoritesFlow.value = listOf(track)
        advanceUntilIdle()
        viewModel.onIntent(DetailMusicIntent.ToggleFavorite(track))
        advanceUntilIdle()
        assertEquals(DetailMusicEffect.Message("Removed from favorites"), effects.last())
        assertEquals(2, libraryRepository.toggleCount)
    }

    @Test
    fun add_to_queue_calls_player_and_emits_message() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        val effects = mutableListOf<DetailMusicEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }
        runCurrent()

        viewModel.onIntent(DetailMusicIntent.AddToQueue(track))
        advanceUntilIdle()

        assertEquals(track, audioPlayer.lastQueued)
        assertEquals(listOf(DetailMusicEffect.Message("Added to queue")), effects)
    }

    private class FakeMusicRepository : MusicRepository {
        var track: Result<Track> = Result.failure(IllegalStateException("not stubbed"))
        var trending: Result<List<Track>> = Result.success(emptyList())
        var trackCalls = 0
        var lastTrackId: String? = null
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

        override suspend fun getTrack(trackId: String): Result<Track> {
            trackCalls++
            lastTrackId = trackId
            return track
        }

        override suspend fun getUser(userId: String): Result<Artist> = error("unused")

        override suspend fun getUserTracks(userId: String, limit: Int, offset: Int): Result<List<Track>> =
            error("unused")

        override suspend fun getPlaylist(playlistId: String): Result<Playlist> = error("unused")

        override suspend fun getPlaylistTracks(playlistId: String): Result<List<Track>> = error("unused")

        override suspend fun getCurrentUser(): Result<Artist> = error("unused")
    }

    private class FakeLibraryRepository : LibraryRepository {
        val favoritesFlow = MutableStateFlow<List<Track>>(emptyList())
        override val favorites: Flow<List<Track>> = favoritesFlow
        override val favoriteIds: Flow<Set<String>> = flowOf(emptySet())
        override val recentlyPlayed: Flow<List<Track>> = flowOf(emptyList())
        var toggleCount = 0

        override suspend fun toggleFavorite(track: Track) {
            toggleCount++
        }

        override suspend fun isFavorite(trackId: String): Boolean = false

        override suspend fun addRecentlyPlayed(track: Track) = Unit

        override suspend fun clearLibrary() = Unit
    }

    private class FakeAudioPlayer : AudioPlayer {
        override val state = MutableStateFlow(PlayerState())
        var lastQueue: List<Track>? = null
        var lastStartIndex: Int? = null
        var lastQueued: Track? = null
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
        override fun addToQueue(track: Track) {
            lastQueued = track
        }

        override fun setShuffle(enabled: Boolean) = Unit
        override fun cycleRepeatMode() = Unit
    }
}
