package com.sona.feature.searchmusic.impl.presentation

import com.sona.core.audio.AudioPlayer
import com.sona.core.audio.PlayerState
import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.domain.usecase.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.SearchCatalog
import com.sona.core.common.domain.usecase.ToggleFavorite
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
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val artist = Artist(id = "u1", handle = "sona", name = "Sona")
    private val track = Track(id = "t1", title = "Night Drive", artist = artist)
    private val result = SearchResult(
        tracks = listOf(track),
        artists = listOf(artist),
        playlists = listOf(Playlist(id = "p1", name = "Mix")),
    )

    private val musicRepository = FakeMusicRepository()
    private val libraryRepository = FakeLibraryRepository()
    private val audioPlayer = FakeAudioPlayer()

    private fun viewModel() = SearchViewModel(
        searchCatalog = SearchCatalog(musicRepository),
        observeFavoriteIds = ObserveFavoriteIds(libraryRepository),
        toggleFavorite = ToggleFavorite(libraryRepository),
        audioPlayer = audioPlayer,
    )

    @Test
    fun short_query_clears_results_without_calling_api() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.QueryChanged("n"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("n", state.query)
        assertTrue(state.tracks.isEmpty())
        assertFalse(state.isLoading)
        assertEquals(0, musicRepository.searchCalls)
    }

    @Test
    fun query_is_trimmed_and_searched_after_debounce() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.result = Result.success(result)
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.QueryChanged("  night  "))
        advanceUntilIdle()

        assertEquals("night", musicRepository.lastQuery)
        assertEquals(25, musicRepository.lastLimit)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf(track), state.tracks)
        assertEquals(listOf(artist), state.artists)
        assertEquals("Mix", state.playlists.single().name)
        assertTrue(state.hasResults)
    }

    @Test
    fun search_failure_sets_message_and_keeps_previous_results() =
        runTest(mainDispatcherRule.dispatcher) {
            musicRepository.result = Result.success(result)
            val viewModel = viewModel()
            viewModel.onIntent(SearchIntent.QueryChanged("night"))
            advanceUntilIdle()

            musicRepository.result = Result.failure(IllegalStateException("search down"))
            viewModel.onIntent(SearchIntent.QueryChanged("night drive"))
            advanceUntilIdle()

            assertEquals("search down", viewModel.state.value.error)
            assertEquals(listOf(track), viewModel.state.value.tracks)
        }

    @Test
    fun failure_without_message_uses_default_text() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.result = Result.failure(IllegalStateException())
        val viewModel = viewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("night"))
        advanceUntilIdle()

        assertEquals("Search failed. Try again.", viewModel.state.value.error)
    }

    @Test
    fun tab_selection_is_stored() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(SearchIntent.TabSelected(SearchTab.PLAYLISTS))

        assertEquals(SearchTab.PLAYLISTS, viewModel.state.value.selectedTab)
    }

    @Test
    fun browsing_genre_sets_query_and_searches() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.result = Result.success(SearchResult())
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.BrowseGenre("Lo-Fi"))
        advanceUntilIdle()

        assertEquals("Lo-Fi", viewModel.state.value.query)
        assertEquals("Lo-Fi", musicRepository.lastQuery)
    }

    @Test
    fun clear_query_resets_query_and_results() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.result = Result.success(result)
        val viewModel = viewModel()
        viewModel.onIntent(SearchIntent.QueryChanged("night"))
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.ClearQuery)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("", state.query)
        assertTrue(state.tracks.isEmpty())
        assertTrue(state.artists.isEmpty())
        assertTrue(state.playlists.isEmpty())
        assertFalse(state.hasQuery)
    }

    @Test
    fun retry_repeats_current_query() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.result = Result.success(result)
        val viewModel = viewModel()
        viewModel.onIntent(SearchIntent.QueryChanged("night"))
        advanceUntilIdle()

        musicRepository.result = Result.success(SearchResult(tracks = listOf(track, track.copy(id = "t2"))))
        viewModel.onIntent(SearchIntent.Retry)
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.tracks.size)
        assertEquals(2, musicRepository.searchCalls)
    }

    @Test
    fun playing_track_uses_results_as_queue() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.result = Result.success(result)
        val viewModel = viewModel()
        viewModel.onIntent(SearchIntent.QueryChanged("night"))
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.PlayTrack(track))

        assertEquals(listOf(track), audioPlayer.lastQueue)
        assertEquals(0, audioPlayer.lastStartIndex)
    }

    @Test
    fun playing_current_track_toggles_play_pause() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        audioPlayer.state.value = PlayerState(queue = listOf(track), currentIndex = 0, isPlaying = true)
        advanceUntilIdle()

        viewModel.onIntent(SearchIntent.PlayTrack(track))

        assertEquals(1, audioPlayer.toggleCount)
        assertNull(audioPlayer.lastQueue)
        assertTrue(viewModel.state.value.isPlaying)
    }

    @Test
    fun toggle_favorite_emits_message() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        val effects = mutableListOf<SearchEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }
        runCurrent()

        viewModel.onIntent(SearchIntent.ToggleFavorite(track))
        advanceUntilIdle()

        assertEquals(listOf(SearchEffect.Message("Saved to favorites")), effects)
        assertEquals(track, libraryRepository.lastToggled)
    }

    private class FakeMusicRepository : MusicRepository {
        var result: Result<SearchResult> = Result.success(SearchResult())
        var searchCalls = 0
        var lastQuery: String? = null
        var lastLimit: Int? = null

        override suspend fun getTrending(
            genre: String?,
            time: String,
            limit: Int,
            offset: Int,
        ): Result<List<Track>> = error("unused")

        override suspend fun search(query: String, limit: Int): Result<SearchResult> {
            searchCalls++
            lastQuery = query
            lastLimit = limit
            return result
        }

        override suspend fun getTrack(trackId: String): Result<Track> = error("unused")

        override suspend fun getUser(userId: String): Result<Artist> = error("unused")

        override suspend fun getUserTracks(userId: String, limit: Int, offset: Int): Result<List<Track>> =
            error("unused")

        override suspend fun getPlaylist(playlistId: String): Result<Playlist> = error("unused")

        override suspend fun getPlaylistTracks(playlistId: String): Result<List<Track>> = error("unused")

        override suspend fun getCurrentUser(): Result<Artist> = error("unused")
    }

    private class FakeLibraryRepository : LibraryRepository {
        override val favorites: Flow<List<Track>> = flowOf(emptyList())
        override val favoriteIds: Flow<Set<String>> = flowOf(emptySet())
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
