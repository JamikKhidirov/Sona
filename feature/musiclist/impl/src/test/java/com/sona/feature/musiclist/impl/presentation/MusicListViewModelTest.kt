package com.sona.feature.musiclist.impl.presentation

import com.sona.core.audio.AudioPlayer
import com.sona.core.audio.PlayerState
import com.sona.core.common.domain.repository.DiscoveryRepository
import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.domain.usecase.music.GetArtistTracks
import com.sona.core.common.domain.usecase.music.GetPlaylistTracks
import com.sona.core.common.domain.usecase.music.GetTrendingTracks
import com.sona.core.common.domain.usecase.music.GetUndergroundTracks
import com.sona.core.common.domain.usecase.library.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.library.ToggleFavorite
import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.SearchResult
import com.sona.core.common.model.Track
import intent.MusicListIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import model.MusicListEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MusicListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val artist = Artist(id = "u1", handle = "sona", name = "Sona")
    private val tracks = listOf(
        Track(id = "t1", title = "One", artist = artist, artworkUrl = "art", durationSeconds = 90),
        Track(id = "t2", title = "Two", artist = artist, durationSeconds = 30),
    )

    private val musicRepository = FakeMusicRepository()
    private val discoveryRepository = FakeDiscoveryRepository()
    private val libraryRepository = FakeLibraryRepository()
    private val audioPlayer = FakeAudioPlayer()

    private fun viewModel() = MusicListViewModel(
        getTrendingTracks = GetTrendingTracks(musicRepository),
        getArtistTracks = GetArtistTracks(musicRepository),
        getPlaylistTracks = GetPlaylistTracks(musicRepository),
        getUndergroundTracks = GetUndergroundTracks(discoveryRepository),
        observeFavoriteIds = ObserveFavoriteIds(libraryRepository),
        toggleFavorite = ToggleFavorite(libraryRepository),
        audioPlayer = audioPlayer,
    )

    @Test
    fun trending_load_fills_tracks_and_hero() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)
        val viewModel = viewModel()

        viewModel.onIntent(MusicListIntent.Load("trending", "", "Trending"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Trending", state.title)
        assertEquals("trending", state.source)
        assertEquals(tracks, state.tracks)
        assertEquals("art", state.heroArtwork)
        assertEquals(120, state.totalDurationSeconds)
        assertNull(state.error)
        assertNull(musicRepository.lastGenre)
        assertEquals(100, musicRepository.lastLimit)
    }

    @Test
    fun genre_load_passes_genre_filter() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)
        val viewModel = viewModel()

        viewModel.onIntent(MusicListIntent.Load("genre", "Lo-Fi", "Lo-Fi"))
        advanceUntilIdle()

        assertEquals("Lo-Fi", musicRepository.lastGenre)
    }

    @Test
    fun artist_load_uses_artist_tracks() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.userTracks = Result.success(tracks)
        val viewModel = viewModel()

        viewModel.onIntent(MusicListIntent.Load("artist", "u1", "Sona"))
        advanceUntilIdle()

        assertEquals("u1", musicRepository.lastUserId)
        assertEquals(tracks, viewModel.state.value.tracks)
    }

    @Test
    fun playlist_load_uses_playlist_tracks() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.playlistTracks = Result.success(tracks)
        val viewModel = viewModel()

        viewModel.onIntent(MusicListIntent.Load("playlist", "p1", "Mix"))
        advanceUntilIdle()

        assertEquals("p1", musicRepository.lastPlaylistId)
        assertEquals(tracks, viewModel.state.value.tracks)
    }

    @Test
    fun underground_load_uses_discovery_repository() = runTest(mainDispatcherRule.dispatcher) {
        discoveryRepository.underground = Result.success(tracks)
        val viewModel = viewModel()

        viewModel.onIntent(MusicListIntent.Load("underground", "all", "Андеграунд"))
        advanceUntilIdle()

        assertEquals(tracks, viewModel.state.value.tracks)
        assertEquals(100, discoveryRepository.lastUndergroundLimit)
    }

    @Test
    fun unknown_source_falls_back_to_trending() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)
        val viewModel = viewModel()

        viewModel.onIntent(MusicListIntent.Load("mystery", "id", "Mystery"))
        advanceUntilIdle()

        assertEquals(tracks, viewModel.state.value.tracks)
    }

    @Test
    fun empty_result_sets_empty_message() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(emptyList())
        val viewModel = viewModel()

        viewModel.onIntent(MusicListIntent.Load("trending", "", "Trending"))
        advanceUntilIdle()

        assertEquals("Nothing here yet.", viewModel.state.value.error)
    }

    @Test
    fun failure_sets_message_and_retry_reloads_same_collection() =
        runTest(mainDispatcherRule.dispatcher) {
            musicRepository.trending = Result.failure(IllegalStateException("collection down"))
            val viewModel = viewModel()
            viewModel.onIntent(MusicListIntent.Load("genre", "Pop", "Pop"))
            advanceUntilIdle()

            assertEquals("collection down", viewModel.state.value.error)

            musicRepository.trending = Result.success(tracks)
            viewModel.onIntent(MusicListIntent.Retry)
            advanceUntilIdle()

            assertEquals(tracks, viewModel.state.value.tracks)
            assertEquals("Pop", musicRepository.lastGenre)
        }

    @Test
    fun failure_without_message_uses_default_text() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.failure(IllegalStateException())
        val viewModel = viewModel()

        viewModel.onIntent(MusicListIntent.Load("trending", "", "Trending"))
        advanceUntilIdle()

        assertEquals("Couldn't load this collection.", viewModel.state.value.error)
    }

    @Test
    fun loading_same_key_twice_is_ignored() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)
        val viewModel = viewModel()

        viewModel.onIntent(MusicListIntent.Load("trending", "", "Trending"))
        advanceUntilIdle()
        viewModel.onIntent(MusicListIntent.Load("trending", "", "Trending"))
        advanceUntilIdle()

        assertEquals(1, musicRepository.trendingCalls)
    }

    @Test
    fun play_all_and_play_track_use_loaded_queue() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)
        val viewModel = viewModel()
        viewModel.onIntent(MusicListIntent.Load("trending", "", "Trending"))
        advanceUntilIdle()

        viewModel.onIntent(MusicListIntent.PlayAll)
        assertEquals(0, audioPlayer.lastStartIndex)

        viewModel.onIntent(MusicListIntent.PlayTrack(tracks[1]))
        assertEquals(tracks, audioPlayer.lastQueue)
        assertEquals(1, audioPlayer.lastStartIndex)
    }

    @Test
    fun playing_current_track_toggles() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)
        val viewModel = viewModel()
        viewModel.onIntent(MusicListIntent.Load("trending", "", "Trending"))
        audioPlayer.state.value = PlayerState(queue = tracks, currentIndex = 0, isPlaying = true)
        advanceUntilIdle()

        viewModel.onIntent(MusicListIntent.PlayTrack(tracks[0]))

        assertEquals(1, audioPlayer.toggleCount)
    }

    @Test
    fun toggle_favorite_emits_saved_or_removed() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.trending = Result.success(tracks)
        val viewModel = viewModel()
        viewModel.onIntent(MusicListIntent.Load("trending", "", "Trending"))
        advanceUntilIdle()

        val effects = mutableListOf<MusicListEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }
        runCurrent()

        viewModel.onIntent(MusicListIntent.ToggleFavorite(tracks[0]))
        advanceUntilIdle()
        assertEquals(MusicListEffect.Message("Saved to favorites"), effects.last())

        libraryRepository.favoriteIdsFlow.value = setOf("t1")
        advanceUntilIdle()
        viewModel.onIntent(MusicListIntent.ToggleFavorite(tracks[0]))
        advanceUntilIdle()
        assertEquals(MusicListEffect.Message("Removed from favorites"), effects.last())
    }

    private class FakeMusicRepository : MusicRepository {
        var trending: Result<List<Track>> = Result.success(emptyList())
        var userTracks: Result<List<Track>> = Result.success(emptyList())
        var playlistTracks: Result<List<Track>> = Result.success(emptyList())
        var trendingCalls = 0
        var lastGenre: String? = null
        var lastLimit: Int? = null
        var lastUserId: String? = null
        var lastPlaylistId: String? = null

        override suspend fun getTrending(
            genre: String?,
            time: String,
            limit: Int,
            offset: Int,
        ): Result<List<Track>> {
            trendingCalls++
            lastGenre = genre
            lastLimit = limit
            return trending
        }

        override suspend fun search(query: String, limit: Int): Result<SearchResult> = error("unused")

        override suspend fun getTrack(trackId: String): Result<Track> = error("unused")

        override suspend fun getUser(userId: String): Result<Artist> = error("unused")

        override suspend fun getUserTracks(userId: String, limit: Int, offset: Int): Result<List<Track>> {
            lastUserId = userId
            return userTracks
        }

        override suspend fun getPlaylist(playlistId: String): Result<Playlist> = error("unused")

        override suspend fun getPlaylistTracks(playlistId: String): Result<List<Track>> {
            lastPlaylistId = playlistId
            return playlistTracks
        }

        override suspend fun getCurrentUser(): Result<Artist> = error("unused")
    }

    private class FakeDiscoveryRepository : DiscoveryRepository {
        var underground: Result<List<Track>> = Result.success(emptyList())
        var lastUndergroundLimit: Int? = null

        override suspend fun getUnderground(limit: Int, offset: Int): Result<List<Track>> {
            lastUndergroundLimit = limit
            return underground
        }

        override suspend fun getTrendingPlaylists(
            time: String,
            limit: Int,
            offset: Int,
        ): Result<List<Playlist>> = error("unused")

        override suspend fun getRelatedTracks(
            trackId: String,
            limit: Int,
            offset: Int,
        ): Result<List<Track>> = error("unused")

        override suspend fun getRemixes(
            trackId: String,
            limit: Int,
            offset: Int,
        ): Result<List<Track>> = error("unused")

        override suspend fun getStems(trackId: String): Result<List<Track>> =
            error("unused")

        override suspend fun getPlaylistFavorites(
            playlistId: String,
            limit: Int,
            offset: Int,
        ): Result<List<Artist>> = error("unused")

        override suspend fun favoritePlaylist(playlistId: String): Result<Unit> =
            error("unused")

        override suspend fun unfavoritePlaylist(playlistId: String): Result<Unit> =
            error("unused")

        override suspend fun getAutocomplete(query: String, limit: Int): Result<List<String>> =
            error("unused")
    }

    private class FakeLibraryRepository : LibraryRepository {
        val favoriteIdsFlow = MutableStateFlow<Set<String>>(emptySet())
        override val favorites: Flow<List<Track>> = flowOf(emptyList())
        override val favoriteIds: Flow<Set<String>> = favoriteIdsFlow
        override val recentlyPlayed: Flow<List<Track>> = flowOf(emptyList())

        override suspend fun toggleFavorite(track: Track) = Unit

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
