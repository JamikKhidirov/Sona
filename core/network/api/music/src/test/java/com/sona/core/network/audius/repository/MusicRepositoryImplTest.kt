package com.sona.core.network.audius.repository

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.sona.core.common.coroutines.DispatcherProvider
import com.sona.core.common.model.Track
import com.sona.core.network.audius.api.PlaylistApi
import com.sona.core.network.audius.api.TrackApi
import com.sona.core.network.audius.api.UserApi
import com.sona.core.network.audius.dto.AudiusEnvelope
import com.sona.core.network.audius.dto.CommentDto
import com.sona.core.network.audius.dto.CommentResponse
import com.sona.core.network.audius.dto.CreateCommentRequest
import com.sona.core.network.audius.dto.PlaylistDto
import com.sona.core.network.audius.dto.TrackDto
import com.sona.core.network.audius.dto.UpdatePlaylistRequest
import com.sona.core.network.audius.dto.UpdateTrackRequest
import com.sona.core.network.audius.dto.UserDto
import com.sona.core.network.audius.model.FilterTracks
import com.sona.core.network.audius.model.FilterTracksLimited
import com.sona.core.network.audius.model.SortAi
import com.sona.core.network.audius.model.SortDirection
import com.sona.core.network.audius.model.SortMethod
import com.sona.core.network.audius.model.SortMethodUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MusicRepositoryImplTest {

    private val gson = Gson()
    private val dispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }

    private val trackApi = FakeTrackApi()
    private val userApi = FakeUserApi()
    private val playlistApi = FakePlaylistApi()
    private val repository = MusicRepositoryImpl(
        trackApi = trackApi,
        playlistApi = playlistApi,
        userApi = userApi,
        gson = gson,
        dispatchers = dispatchers,
    )

    @Test
    fun get_trending_maps_valid_tracks_and_skips_broken_ones() = runTest {
        trackApi.trending = AudiusEnvelope(
            data = listOf(
                TrackDto(id = "t1", title = "One", duration = 120),
                TrackDto(title = "Broken"),
                TrackDto(id = "t2", title = "Two"),
            ),
        )

        val result = repository.getTrending(genre = "Lo-Fi", time = "month", limit = 10, offset = 5)

        assertEquals(listOf("t1", "t2"), result.getOrThrow().map(Track::id))
        assertEquals("Lo-Fi", trackApi.lastGenre)
        assertEquals("month", trackApi.lastTime)
        assertEquals(10, trackApi.lastLimit)
        assertEquals(5, trackApi.lastOffset)
        assertEquals("Sona", trackApi.lastAppName)
    }

    @Test
    fun get_trending_wraps_api_errors_in_failure() = runTest {
        trackApi.trendingError = IllegalStateException("offline")

        val result = repository.getTrending()

        assertTrue(result.isFailure)
        assertEquals("offline", result.exceptionOrNull()?.message)
    }

    @Test
    fun get_trending_treats_missing_data_as_empty_list() = runTest {
        trackApi.trending = AudiusEnvelope(data = null)

        assertEquals(emptyList<Track>(), repository.getTrending().getOrThrow())
    }

    @Test
    fun search_merges_tracks_artists_and_playlists() = runTest {
        trackApi.search = AudiusEnvelope(data = listOf(TrackDto(id = "t1", title = "One")))
        userApi.search = AudiusEnvelope(data = listOf(UserDto(id = "u1", handle = "sona", name = "Sona")))
        playlistApi.search = AudiusEnvelope(data = listOf(PlaylistDto(id = "p1", playlistName = "Mix")))

        val result = repository.search(query = "night", limit = 5).getOrThrow()

        assertEquals(listOf("t1"), result.tracks.map(Track::id))
        assertEquals(listOf("u1"), result.artists.map { it.id })
        assertEquals(listOf("p1"), result.playlists.map { it.id })
        assertEquals("night", trackApi.lastQuery)
        assertEquals(5, trackApi.lastLimit)
    }

    @Test
    fun search_succeeds_when_only_some_sources_fail() = runTest {
        trackApi.search = AudiusEnvelope(data = listOf(TrackDto(id = "t1", title = "One")))
        userApi.searchError = IllegalStateException("users down")
        playlistApi.searchError = IllegalStateException("playlists down")

        val result = repository.search("night", limit = 5)

        assertTrue(result.isSuccess)
        assertEquals(listOf("t1"), result.getOrThrow().tracks.map(Track::id))
        assertTrue(result.getOrThrow().artists.isEmpty())
        assertTrue(result.getOrThrow().playlists.isEmpty())
    }

    @Test
    fun search_fails_only_when_every_source_fails() = runTest {
        trackApi.searchError = IllegalStateException("tracks down")
        userApi.searchError = IllegalStateException("users down")
        playlistApi.searchError = IllegalStateException("playlists down")

        val result = repository.search("night", limit = 5)

        assertTrue(result.isFailure)
        assertEquals("tracks down", result.exceptionOrNull()?.message)
    }

    @Test
    fun get_track_maps_dto_and_fails_on_missing_data() = runTest {
        trackApi.track = AudiusEnvelope(data = TrackDto(id = "t1", title = "One"))

        assertEquals("t1", repository.getTrack("t1").getOrThrow().id)
        assertEquals("t1", trackApi.lastTrackId)
        assertEquals("Sona", trackApi.lastAppName)

        trackApi.track = AudiusEnvelope(data = null)
        assertTrue(repository.getTrack("t1").isFailure)
    }

    @Test
    fun get_track_fails_when_dto_has_no_id() = runTest {
        trackApi.track = AudiusEnvelope(data = TrackDto(title = "Broken"))

        assertTrue(repository.getTrack("t1").isFailure)
    }

    @Test
    fun get_user_maps_dto_and_fails_on_missing_data() = runTest {
        userApi.user = AudiusEnvelope(data = UserDto(id = "u1", handle = "sona", name = "Sona"))

        assertEquals("Sona", repository.getUser("u1").getOrThrow().displayName)
        assertEquals("u1", userApi.lastUserId)

        userApi.user = AudiusEnvelope(data = null)
        assertTrue(repository.getUser("u1").isFailure)
    }

    @Test
    fun get_user_tracks_maps_and_skips_broken_dtos() = runTest {
        userApi.userTracks = AudiusEnvelope(
            data = listOf(
                TrackDto(id = "t1", title = "One"),
                TrackDto(title = "Broken"),
            ),
        )

        val tracks = repository.getUserTracks("u1", limit = 20, offset = 10).getOrThrow()

        assertEquals(listOf("t1"), tracks.map(Track::id))
        assertEquals(20, userApi.lastLimit)
        assertEquals(10, userApi.lastOffset)
    }

    @Test
    fun get_playlist_handles_array_and_object_payloads() = runTest {
        playlistApi.playlist = AudiusEnvelope(
            data = gson.toJsonTree(listOf(PlaylistDto(id = "p1", playlistName = "Array mix"))),
        )

        val arrayPlaylist = repository.getPlaylist("p1").getOrThrow()
        assertEquals("p1", arrayPlaylist.id)
        assertEquals("Array mix", arrayPlaylist.name)

        playlistApi.playlist = AudiusEnvelope(
            data = gson.toJsonTree(PlaylistDto(id = "p2", playlistName = "Object mix")),
        )

        val objectPlaylist = repository.getPlaylist("p2").getOrThrow()
        assertEquals("p2", objectPlaylist.id)
        assertEquals("Object mix", objectPlaylist.name)
    }

    @Test
    fun get_playlist_fails_on_empty_array_or_missing_data() = runTest {
        playlistApi.playlist = AudiusEnvelope(data = gson.toJsonTree(emptyList<PlaylistDto>()))
        assertTrue(repository.getPlaylist("p1").isFailure)

        playlistApi.playlist = AudiusEnvelope(data = null)
        assertTrue(repository.getPlaylist("p1").isFailure)
    }

    @Test
    fun get_playlist_tracks_uses_limit_hundred_and_maps_tracks() = runTest {
        playlistApi.playlistTracks = AudiusEnvelope(
            data = listOf(TrackDto(id = "t1", title = "One")),
        )

        val tracks = repository.getPlaylistTracks("p1").getOrThrow()

        assertEquals(listOf("t1"), tracks.map(Track::id))
        assertEquals(100, playlistApi.lastLimit)
        assertEquals("p1", playlistApi.lastPlaylistId)
    }

    @Test
    fun get_current_user_maps_dto_and_fails_without_data() = runTest {
        userApi.currentUser = AudiusEnvelope(data = UserDto(id = "u1", handle = "me", name = "Me"))

        assertEquals("Me", repository.getCurrentUser().getOrThrow().displayName)

        userApi.currentUser = AudiusEnvelope(data = null)
        assertTrue(repository.getCurrentUser().isFailure)
    }

    // region fakes

    private class FakeTrackApi : TrackApi {
        var trending: AudiusEnvelope<List<TrackDto>> = AudiusEnvelope(emptyList())
        var trendingError: Throwable? = null
        var search: AudiusEnvelope<List<TrackDto>> = AudiusEnvelope(emptyList())
        var searchError: Throwable? = null
        var track: AudiusEnvelope<TrackDto> = AudiusEnvelope()
        var lastAppName: String? = null
        var lastGenre: String? = null
        var lastTime: String? = null
        var lastLimit: Int? = null
        var lastOffset: Int? = null
        var lastQuery: String? = null
        var lastTrackId: String? = null

        override suspend fun getTrendingTracks(
            appName: String,
            genre: String?,
            time: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<TrackDto>> {
            lastAppName = appName
            lastGenre = genre
            lastTime = time
            lastLimit = limit
            lastOffset = offset
            trendingError?.let { throw it }
            return trending
        }

        override suspend fun getUndergroundTrendingTracks(
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<TrackDto>> = error("unused")

        override suspend fun getTrendingTrackIds(
            appName: String,
            genre: String?,
            time: String,
            limit: Int,
        ): AudiusEnvelope<JsonElement> = error("unused")

        override suspend fun getTracks(
            appName: String,
            ids: List<String>,
        ): AudiusEnvelope<List<TrackDto>> = error("unused")

        override suspend fun getTrack(
            trackId: String,
            appName: String,
        ): AudiusEnvelope<TrackDto> {
            lastTrackId = trackId
            lastAppName = appName
            return track
        }

        override suspend fun searchTracks(
            query: String,
            appName: String,
            genre: String?,
            mood: String?,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<TrackDto>> {
            lastQuery = query
            lastAppName = appName
            lastLimit = limit
            lastOffset = offset
            searchError?.let { throw it }
            return search
        }

        override suspend fun getRelatedTracks(
            trackId: String,
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<TrackDto>> = error("unused")

        override suspend fun getTrackRemixes(
            trackId: String,
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<TrackDto>> = error("unused")

        override suspend fun getTrackStems(
            trackId: String,
            appName: String,
        ): AudiusEnvelope<List<TrackDto>> = error("unused")

        override suspend fun getTrackComments(
            trackId: String,
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<CommentDto>> = error("unused")

        override suspend fun postTrackComment(
            trackId: String,
            appName: String,
            body: CreateCommentRequest,
        ): CommentResponse = error("unused")

        override suspend fun getTrackAccessInfo(
            trackId: String,
            appName: String,
        ): AudiusEnvelope<JsonElement> = error("unused")

        override suspend fun streamTrack(trackId: String, appName: String): ResponseBody =
            error("unused")

        override suspend fun downloadTrack(trackId: String, appName: String): ResponseBody =
            error("unused")

        override suspend fun favoriteTrack(trackId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun unfavoriteTrack(trackId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun repostTrack(trackId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun unrepostTrack(trackId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun updateTrack(
            trackId: String,
            appName: String,
            body: UpdateTrackRequest,
        ): Response<Unit> = error("unused")

        override suspend fun deleteTrack(trackId: String, appName: String): Response<Unit> =
            error("unused")
    }

    private class FakeUserApi : UserApi {
        var search: AudiusEnvelope<List<UserDto>> = AudiusEnvelope(emptyList())
        var searchError: Throwable? = null
        var user: AudiusEnvelope<UserDto> = AudiusEnvelope()
        var userTracks: AudiusEnvelope<List<TrackDto>> = AudiusEnvelope(emptyList())
        var currentUser: AudiusEnvelope<UserDto> = AudiusEnvelope()
        var lastUserId: String? = null
        var lastLimit: Int? = null
        var lastOffset: Int? = null

        override suspend fun getUsers(appName: String, ids: List<String>): AudiusEnvelope<List<UserDto>> =
            error("unused")

        override suspend fun getUser(userId: String, appName: String): AudiusEnvelope<UserDto> {
            lastUserId = userId
            return user
        }

        override suspend fun getUserByHandle(handle: String, appName: String): AudiusEnvelope<UserDto> =
            error("unused")

        override suspend fun searchUsers(
            query: String,
            appName: String,
            genre: List<String>,
            sortMethod: SortMethodUser?,
            isVerified: String?,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<UserDto>> {
            lastLimit = limit
            lastOffset = offset
            searchError?.let { throw it }
            return search
        }

        override suspend fun getCurrentUser(appName: String): AudiusEnvelope<UserDto> = currentUser

        override suspend fun getAccount(appName: String): AudiusEnvelope<JsonElement> = error("unused")

        override suspend fun getUserTracks(
            userId: String,
            appName: String,
            limit: Int,
            offset: Int,
            sortMethod: SortMethod?,
            sortDirection: SortDirection?,
            filterTracks: FilterTracks?,
            query: String?,
        ): AudiusEnvelope<List<TrackDto>> {
            lastUserId = userId
            lastLimit = limit
            lastOffset = offset
            return userTracks
        }

        override suspend fun getUserTracksByHandle(
            handle: String,
            appName: String,
            limit: Int,
            offset: Int,
            sortMethod: SortMethod?,
            sortDirection: SortDirection?,
            filterTracks: FilterTracks?,
            query: String?,
        ): AudiusEnvelope<List<TrackDto>> = error("unused")

        override suspend fun getAiAttributedTracks(
            userId: String,
            appName: String,
            sort: SortAi?,
            sortMethod: SortMethod?,
            sortDirection: SortDirection?,
            filterTracks: FilterTracksLimited?,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<TrackDto>> = error("unused")

        override suspend fun getUserPlaylists(
            userId: String,
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<PlaylistDto>> = error("unused")

        override suspend fun getUserFavorites(
            userId: String,
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<TrackDto>> = error("unused")

        override suspend fun getUserReposts(
            userId: String,
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<JsonElement> = error("unused")

        override suspend fun getFollowers(
            userId: String,
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<UserDto>> = error("unused")

        override suspend fun getFollowing(
            userId: String,
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<UserDto>> = error("unused")

        override suspend fun getRelatedUsers(
            userId: String,
            appName: String,
            limit: Int,
        ): AudiusEnvelope<List<UserDto>> = error("unused")

        override suspend fun getUserTags(userId: String, appName: String): AudiusEnvelope<JsonElement> =
            error("unused")

        override suspend fun getUserHistory(
            userId: String,
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<JsonElement> = error("unused")

        override suspend fun followUser(userId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun unfollowUser(userId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun muteUser(userId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun unmuteUser(userId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun verifyToken(appName: String): AudiusEnvelope<JsonElement> = error("unused")
    }

    private class FakePlaylistApi : PlaylistApi {
        var search: AudiusEnvelope<List<PlaylistDto>> = AudiusEnvelope(emptyList())
        var searchError: Throwable? = null
        var playlist: AudiusEnvelope<JsonElement> = AudiusEnvelope()
        var playlistTracks: AudiusEnvelope<List<TrackDto>> = AudiusEnvelope(emptyList())
        var lastPlaylistId: String? = null
        var lastLimit: Int? = null

        override suspend fun getPlaylists(
            appName: String,
            ids: List<String>,
        ): AudiusEnvelope<List<PlaylistDto>> = error("unused")

        override suspend fun getPlaylist(
            playlistId: String,
            appName: String,
        ): AudiusEnvelope<JsonElement> {
            lastPlaylistId = playlistId
            return playlist
        }

        override suspend fun getPlaylistTracks(
            playlistId: String,
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<TrackDto>> {
            lastPlaylistId = playlistId
            lastLimit = limit
            return playlistTracks
        }

        override suspend fun searchPlaylists(
            query: String,
            appName: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<PlaylistDto>> {
            lastLimit = limit
            searchError?.let { throw it }
            return search
        }

        override suspend fun getTrendingPlaylists(
            appName: String,
            time: String,
            limit: Int,
            offset: Int,
        ): AudiusEnvelope<List<PlaylistDto>> = error("unused")

        override suspend fun getPlaylistFavorites(
            playlistId: String,
            appName: String,
        ): AudiusEnvelope<List<UserDto>> = error("unused")

        override suspend fun getPlaylistAccessInfo(
            playlistId: String,
            appName: String,
        ): AudiusEnvelope<JsonElement> = error("unused")

        override suspend fun favoritePlaylist(playlistId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun unfavoritePlaylist(playlistId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun repostPlaylist(playlistId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun unrepostPlaylist(playlistId: String, appName: String): Response<Unit> =
            error("unused")

        override suspend fun updatePlaylist(
            playlistId: String,
            appName: String,
            body: UpdatePlaylistRequest,
        ): Response<Unit> = error("unused")

        override suspend fun deletePlaylist(playlistId: String, appName: String): Response<Unit> =
            error("unused")
    }

    // endregion
}
