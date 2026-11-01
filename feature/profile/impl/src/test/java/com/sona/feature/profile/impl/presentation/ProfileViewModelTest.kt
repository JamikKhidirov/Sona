package com.sona.feature.profile.impl.presentation

import com.sona.core.audio.AudioPlayer
import com.sona.core.audio.PlayerState
import com.sona.core.common.domain.model.AppSettings
import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.domain.repository.SettingsRepository
import com.sona.core.common.domain.usecase.music.GetCurrentUser
import com.sona.core.common.domain.usecase.library.ObserveFavorites
import com.sona.core.common.domain.usecase.library.ObserveRecentlyPlayed
import com.sona.core.common.domain.usecase.ObserveSettings
import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.SearchResult
import com.sona.core.common.model.Track
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val artist = Artist(
        id = "u1",
        handle = "sona",
        name = "Sona",
        followerCount = 42,
    )
    private val tracks = listOf(
        Track(id = "t1", title = "One", artist = artist),
        Track(id = "t2", title = "Two", artist = artist),
    )

    private val musicRepository = FakeMusicRepository()
    private val libraryRepository = FakeLibraryRepository()
    private val settingsRepository = FakeSettingsRepository()
    private val audioPlayer = FakeAudioPlayer()

    private fun viewModel() = ProfileViewModel(
        getCurrentUser = GetCurrentUser(musicRepository),
        observeFavorites = ObserveFavorites(libraryRepository),
        observeRecentlyPlayed = ObserveRecentlyPlayed(libraryRepository),
        observeSettings = ObserveSettings(settingsRepository),
        audioPlayer = audioPlayer,
    )

    @Test
    fun signed_out_profile_has_local_defaults_and_skips_api() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isSignedIn)
            assertFalse(state.isLoading)
            assertNull(state.user)
            assertEquals("Sona listener", state.displayName)
            assertEquals("Local profile", state.handle)
            assertEquals(0L, state.followerCount)
            assertEquals(0, musicRepository.currentUserCalls)
        }

    @Test
    fun signed_in_profile_loads_current_user() = runTest(mainDispatcherRule.dispatcher) {
        musicRepository.currentUser = Result.success(artist)
        settingsRepository.settingsFlow.value = AppSettings(apiToken = "token")

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isSignedIn)
        assertFalse(state.isLoading)
        assertEquals(artist, state.user)
        assertEquals("Sona", state.displayName)
        assertEquals("@sona", state.handle)
        assertEquals(42L, state.followerCount)
        assertEquals(1, musicRepository.currentUserCalls)
    }

    @Test
    fun signed_in_failure_sets_error_and_defaults_message() =
        runTest(mainDispatcherRule.dispatcher) {
            musicRepository.currentUser = Result.failure(IllegalStateException("profile down"))
            settingsRepository.settingsFlow.value = AppSettings(apiToken = "token")

            val viewModel = viewModel()
            advanceUntilIdle()

            assertEquals("profile down", viewModel.state.value.error)

            musicRepository.currentUser = Result.failure(IllegalStateException())
            viewModel.onIntent(ProfileIntent.Refresh)
            advanceUntilIdle()

            assertEquals("Couldn't load your Audius profile.", viewModel.state.value.error)
        }

    @Test
    fun signing_out_clears_user_and_allows_reload_after_sign_in() =
        runTest(mainDispatcherRule.dispatcher) {
            musicRepository.currentUser = Result.success(artist)
            settingsRepository.settingsFlow.value = AppSettings(apiToken = "token")
            val viewModel = viewModel()
            advanceUntilIdle()

            settingsRepository.settingsFlow.value = AppSettings()
            advanceUntilIdle()

            assertNull(viewModel.state.value.user)
            assertFalse(viewModel.state.value.isSignedIn)

            settingsRepository.settingsFlow.value = AppSettings(apiToken = "token")
            advanceUntilIdle()

            assertEquals(artist, viewModel.state.value.user)
            assertEquals(2, musicRepository.currentUserCalls)
        }

    @Test
    fun refresh_while_signed_out_does_nothing() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(ProfileIntent.Refresh)
        advanceUntilIdle()

        assertEquals(0, musicRepository.currentUserCalls)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun library_flows_fill_favorites_and_recently_played() = runTest(mainDispatcherRule.dispatcher) {
        libraryRepository.favoritesFlow.value = tracks
        libraryRepository.recentlyPlayedFlow.value = tracks.take(1)
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(tracks, viewModel.state.value.favorites)
        assertEquals(tracks.take(1), viewModel.state.value.recentlyPlayed)
    }

    @Test
    fun play_track_prefers_recently_played_queue() = runTest(mainDispatcherRule.dispatcher) {
        libraryRepository.recentlyPlayedFlow.value = tracks
        libraryRepository.favoritesFlow.value = listOf(tracks[0])
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(ProfileIntent.PlayTrack(tracks[1]))

        assertEquals(tracks, audioPlayer.lastQueue)
        assertEquals(1, audioPlayer.lastStartIndex)
    }

    @Test
    fun play_track_falls_back_to_favorites_then_single_track() =
        runTest(mainDispatcherRule.dispatcher) {
            libraryRepository.favoritesFlow.value = tracks
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onIntent(ProfileIntent.PlayTrack(tracks[1]))
            assertEquals(tracks, audioPlayer.lastQueue)

            libraryRepository.favoritesFlow.value = emptyList()
            advanceUntilIdle()
            viewModel.onIntent(ProfileIntent.PlayTrack(tracks[1]))
            assertEquals(listOf(tracks[1]), audioPlayer.lastQueue)
            assertEquals(0, audioPlayer.lastStartIndex)
        }

    @Test
    fun playing_current_track_toggles_play_pause() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        audioPlayer.state.value = PlayerState(queue = tracks, currentIndex = 0, isPlaying = true)
        advanceUntilIdle()

        viewModel.onIntent(ProfileIntent.PlayTrack(tracks[0]))

        assertEquals(1, audioPlayer.toggleCount)
        assertNull(audioPlayer.lastQueue)
        assertTrue(viewModel.state.value.isPlaying)
        assertEquals("t1", viewModel.state.value.currentTrackId)
    }

    private class FakeMusicRepository : MusicRepository {
        var currentUser: Result<Artist> = Result.failure(IllegalStateException("not signed in"))
        var currentUserCalls = 0

        override suspend fun getTrending(
            genre: String?,
            time: String,
            limit: Int,
            offset: Int,
        ): Result<List<Track>> = error("unused")

        override suspend fun search(query: String, limit: Int): Result<SearchResult> = error("unused")

        override suspend fun getTrack(trackId: String): Result<Track> = error("unused")

        override suspend fun getUser(userId: String): Result<Artist> = error("unused")

        override suspend fun getUserTracks(userId: String, limit: Int, offset: Int): Result<List<Track>> =
            error("unused")

        override suspend fun getPlaylist(playlistId: String): Result<Playlist> = error("unused")

        override suspend fun getPlaylistTracks(playlistId: String): Result<List<Track>> = error("unused")

        override suspend fun getCurrentUser(): Result<Artist> {
            currentUserCalls++
            return currentUser
        }
    }

    private class FakeLibraryRepository : LibraryRepository {
        val favoritesFlow = MutableStateFlow<List<Track>>(emptyList())
        val recentlyPlayedFlow = MutableStateFlow<List<Track>>(emptyList())
        override val favorites: Flow<List<Track>> = favoritesFlow
        override val favoriteIds: Flow<Set<String>> = flowOf(emptySet())
        override val recentlyPlayed: Flow<List<Track>> = recentlyPlayedFlow

        override suspend fun toggleFavorite(track: Track) = Unit

        override suspend fun isFavorite(trackId: String): Boolean = false

        override suspend fun addRecentlyPlayed(track: Track) = Unit

        override suspend fun clearLibrary() = Unit
    }

    private class FakeSettingsRepository : SettingsRepository {
        val settingsFlow = MutableStateFlow(AppSettings())
        override val settings: Flow<AppSettings> = settingsFlow

        override suspend fun setThemeMode(mode: com.sona.core.common.model.ThemeMode) = Unit

        override suspend fun setApiToken(token: String) = Unit
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
