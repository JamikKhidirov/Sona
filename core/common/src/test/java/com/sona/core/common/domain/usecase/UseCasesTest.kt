package com.sona.core.common.domain.usecase

import com.sona.core.common.domain.model.AppSettings
import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.common.domain.repository.SettingsRepository
import com.sona.core.common.domain.usecase.library.AddRecentlyPlayed
import com.sona.core.common.domain.usecase.library.ClearLibrary
import com.sona.core.common.domain.usecase.library.ObserveFavoriteIds
import com.sona.core.common.domain.usecase.library.ObserveFavorites
import com.sona.core.common.domain.usecase.library.ObserveRecentlyPlayed
import com.sona.core.common.domain.usecase.library.ToggleFavorite
import com.sona.core.common.domain.usecase.music.GetCurrentUser
import com.sona.core.common.domain.usecase.settings.SetApiToken
import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.SearchResult
import com.sona.core.common.model.ThemeMode
import com.sona.core.common.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class UseCasesTest {

    private val artist = Artist(id = "u1", handle = "sona", name = "Sona")
    private val track = Track(id = "t1", title = "Night Drive", artist = artist)

    // region MusicUseCases

    @Test
    fun trending_use_case_forwards_every_argument() = runTest {
        val repository = FakeMusicRepository()
        repository.trendingResult = Result.success(listOf(track))
        val useCase = GetTrendingTracks(repository)

        val result = useCase(genre = "Lo-Fi", time = "month", limit = 12)

        assertEquals(listOf(track), result.getOrThrow())
        assertEquals("Lo-Fi", repository.lastGenre)
        assertEquals("month", repository.lastTime)
        assertEquals(12, repository.lastLimit)
    }

    @Test
    fun trending_use_case_uses_defaults() = runTest {
        val repository = FakeMusicRepository()

        GetTrendingTracks(repository)()

        assertEquals(null, repository.lastGenre)
        assertEquals("week", repository.lastTime)
        assertEquals(50, repository.lastLimit)
    }

    @Test
    fun search_use_case_forwards_query_and_limit() = runTest {
        val repository = FakeMusicRepository()
        val expected = SearchResult(tracks = listOf(track))
        repository.searchResult = Result.success(expected)

        val result = SearchCatalog(repository)(query = "night", limit = 7)

        assertSame(expected, result.getOrThrow())
        assertEquals("night", repository.lastQuery)
        assertEquals(7, repository.lastLimit)
    }

    @Test
    fun track_artist_playlist_and_user_use_cases_delegate() = runTest {
        val repository = FakeMusicRepository()
        val playlist = Playlist(id = "p1", name = "Mix")
        repository.trackResult = Result.success(track)
        repository.artistResult = Result.success(artist)
        repository.userTracksResult = Result.success(listOf(track))
        repository.playlistResult = Result.success(playlist)
        repository.playlistTracksResult = Result.success(listOf(track))
        repository.currentUserResult = Result.success(artist)

        assertSame(track, GetTrack(repository)("t1").getOrThrow())
        assertSame(artist, GetArtist(repository)("u1").getOrThrow())
        assertEquals(listOf(track), GetArtistTracks(repository)("u1", limit = 10).getOrThrow())
        assertSame(playlist, GetPlaylist(repository)("p1").getOrThrow())
        assertEquals(listOf(track), GetPlaylistTracks(repository)("p1").getOrThrow())
        assertSame(artist, GetCurrentUser(repository)().getOrThrow())

        assertEquals("t1", repository.lastTrackId)
        assertEquals("u1", repository.lastUserId)
        assertEquals(10, repository.lastLimit)
        assertEquals("p1", repository.lastPlaylistId)
    }

    @Test
    fun use_cases_propagate_failures() = runTest {
        val repository = FakeMusicRepository()
        val failure = IllegalStateException("offline")
        repository.trendingResult = Result.failure(failure)

        val result = GetTrendingTracks(repository)()

        assertEquals(failure, result.exceptionOrNull())
    }

    // endregion

    // region LibraryUseCases

    @Test
    fun observe_use_cases_expose_repository_flows() = runTest {
        val repository = FakeLibraryRepository()

        assertEquals(listOf("t1"), ObserveFavorites(repository)().first().map(Track::id))
        assertEquals(setOf("t1"), ObserveFavoriteIds(repository)().first())
        assertEquals(listOf("t1"), ObserveRecentlyPlayed(repository)().first().map(Track::id))
    }

    @Test
    fun library_mutations_delegate_to_repository() = runTest {
        val repository = FakeLibraryRepository()

        ToggleFavorite(repository)(track)
        AddRecentlyPlayed(repository)(track)
        ClearLibrary(repository)()

        assertEquals(track, repository.lastToggled)
        assertEquals(track, repository.lastRecentlyPlayed)
        assertEquals(1, repository.clearCount)
    }

    // endregion

    // region SettingsUseCases

    @Test
    fun settings_use_cases_expose_and_update_settings() = runTest {
        val repository = FakeSettingsRepository()
        val settings = AppSettings(themeMode = ThemeMode.DARK, apiToken = "token")

        assertEquals(settings, ObserveSettings(repository)().first())

        SetThemeMode(repository)(ThemeMode.LIGHT)
        SetApiToken(repository)("new-token")

        assertEquals(ThemeMode.LIGHT, repository.lastThemeMode)
        assertEquals("new-token", repository.lastToken)
    }

    // endregion

    private class FakeMusicRepository : MusicRepository {
        var trendingResult: Result<List<Track>> = Result.success(emptyList())
        var searchResult: Result<SearchResult> = Result.success(SearchResult())
        var trackResult: Result<Track>? = null
        var artistResult: Result<Artist>? = null
        var userTracksResult: Result<List<Track>>? = null
        var playlistResult: Result<Playlist>? = null
        var playlistTracksResult: Result<List<Track>>? = null
        var currentUserResult: Result<Artist>? = null

        var lastGenre: String? = null
        var lastTime: String? = null
        var lastLimit: Int? = null
        var lastQuery: String? = null
        var lastTrackId: String? = null
        var lastUserId: String? = null
        var lastPlaylistId: String? = null

        override suspend fun getTrending(
            genre: String?,
            time: String,
            limit: Int,
            offset: Int,
        ): Result<List<Track>> {
            lastGenre = genre
            lastTime = time
            lastLimit = limit
            return trendingResult
        }

        override suspend fun search(query: String, limit: Int): Result<SearchResult> {
            lastQuery = query
            lastLimit = limit
            return searchResult
        }

        override suspend fun getTrack(trackId: String): Result<Track> {
            lastTrackId = trackId
            return trackResult ?: Result.success(
                Track(id = trackId, title = "Track", artist = Artist("u1", "sona", "Sona")),
            )
        }

        override suspend fun getUser(userId: String): Result<Artist> {
            lastUserId = userId
            return artistResult ?: Result.success(Artist(userId, "sona", "Sona"))
        }

        override suspend fun getUserTracks(userId: String, limit: Int, offset: Int): Result<List<Track>> {
            lastUserId = userId
            lastLimit = limit
            return userTracksResult ?: Result.success(emptyList())
        }

        override suspend fun getPlaylist(playlistId: String): Result<Playlist> {
            lastPlaylistId = playlistId
            return playlistResult ?: Result.success(Playlist(playlistId, "Playlist"))
        }

        override suspend fun getPlaylistTracks(playlistId: String): Result<List<Track>> {
            lastPlaylistId = playlistId
            return playlistTracksResult ?: Result.success(emptyList())
        }

        override suspend fun getCurrentUser(): Result<Artist> =
            currentUserResult ?: Result.success(Artist("u1", "sona", "Sona"))
    }

    private class FakeLibraryRepository : LibraryRepository {
        override val favorites: Flow<List<Track>> = flowOf(
            listOf(Track("t1", "One", Artist("u1", "sona", "Sona"))),
        )
        override val favoriteIds: Flow<Set<String>> = flowOf(setOf("t1"))
        override val recentlyPlayed: Flow<List<Track>> = flowOf(
            listOf(Track("t1", "One", Artist("u1", "sona", "Sona"))),
        )

        var lastToggled: Track? = null
        var lastRecentlyPlayed: Track? = null
        var clearCount = 0

        override suspend fun toggleFavorite(track: Track) {
            lastToggled = track
        }

        override suspend fun isFavorite(trackId: String): Boolean = false

        override suspend fun addRecentlyPlayed(track: Track) {
            lastRecentlyPlayed = track
        }

        override suspend fun clearLibrary() {
            clearCount++
        }
    }

    private class FakeSettingsRepository : SettingsRepository {
        override val settings: Flow<AppSettings> = flowOf(
            AppSettings(themeMode = ThemeMode.DARK, apiToken = "token"),
        )

        var lastThemeMode: ThemeMode? = null
        var lastToken: String? = null

        override suspend fun setThemeMode(mode: ThemeMode) {
            lastThemeMode = mode
        }

        override suspend fun setApiToken(token: String) {
            lastToken = token
        }
    }
}
