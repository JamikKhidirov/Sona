package com.sona.core.common.model

import com.sona.core.common.domain.model.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {

    private val artist = Artist(id = "u1", handle = "sona", name = "Sona")

    @Test
    fun track_uses_audius_stream_endpoint_by_default() {
        val track = Track(id = "t1", title = "Night Drive", artist = artist)

        assertEquals(
            "https://api.audius.co/v1/tracks/t1/stream?app_name=Sona",
            track.streamUrl,
        )
        assertEquals("Sona", track.artistName)
        assertTrue(track.isStreamable)
    }

    @Test
    fun track_share_url_expands_relative_permalink() {
        val track = Track(
            id = "t1",
            title = "Night Drive",
            artist = artist,
            permalink = "/sona/night-drive",
        )

        assertEquals("https://audius.co/sona/night-drive", track.shareUrl)
    }

    @Test
    fun track_share_url_adds_slash_to_bare_permalink() {
        val track = Track(
            id = "t1",
            title = "Night Drive",
            artist = artist,
            permalink = "sona/night-drive",
        )

        assertEquals("https://audius.co/sona/night-drive", track.shareUrl)
    }

    @Test
    fun track_share_url_keeps_absolute_permalink() {
        val track = Track(
            id = "t1",
            title = "Night Drive",
            artist = artist,
            permalink = "https://audius.co/sona/night-drive",
        )

        assertEquals("https://audius.co/sona/night-drive", track.shareUrl)
    }

    @Test
    fun track_share_url_keeps_http_permalink() {
        val track = Track(
            id = "t1",
            title = "Night Drive",
            artist = artist,
            permalink = "http://audius.co/sona/night-drive",
        )

        assertEquals("http://audius.co/sona/night-drive", track.shareUrl)
    }

    @Test
    fun track_share_url_ignores_blank_permalink() {
        val track = Track(
            id = "t1",
            title = "Night Drive",
            artist = artist,
            permalink = "   ",
        )

        assertEquals(track.streamUrl, track.shareUrl)
    }

    @Test
    fun track_share_url_falls_back_to_stream_url_without_permalink() {
        val track = Track(id = "t1", title = "Night Drive", artist = artist)

        assertEquals(track.streamUrl, track.shareUrl)
    }

    @Test
    fun artist_display_name_falls_back_to_handle() {
        assertEquals("Sona", artist.displayName)
        assertEquals("lofi_dj", artist.copy(handle = "lofi_dj", name = "").displayName)
        assertEquals("lofi_dj", artist.copy(handle = "lofi_dj", name = "   ").displayName)
    }

    @Test
    fun artwork_best_url_prefers_large_then_medium_then_small() {
        assertEquals(
            "large",
            Artwork(small = "small", medium = "medium", large = "large").bestUrl(),
        )
        assertEquals("medium", Artwork(small = "small", medium = "medium").bestUrl())
        assertEquals("small", Artwork(small = "small").bestUrl())
        assertNull(Artwork().bestUrl())
    }

    @Test
    fun audius_config_builds_stream_url_and_picks_artwork() {
        assertEquals("Sona", AudiusConfig.APP_NAME)
        assertEquals("https://api.audius.co/v1/", AudiusConfig.API_BASE_URL)
        assertEquals(
            "https://api.audius.co/v1/tracks/abc/stream?app_name=Sona",
            AudiusConfig.streamUrl("abc"),
        )
        assertEquals("large", AudiusConfig.artworkUrl(Artwork(large = "large")))
        assertNull(AudiusConfig.artworkUrl(null))
    }

    @Test
    fun genres_are_unique_and_looked_up_case_insensitively() {
        val names = Genres.all.map { it.name }

        assertEquals(names.size, names.distinct().size)
        assertEquals(12, Genres.all.size)
        assertEquals("Lo-Fi", Genres.byName("lo-fi")?.name)
        assertEquals("Lo-Fi", Genres.byName("LO-FI")?.name)
        assertEquals("Lo-Fi", Genres.byName("lo-fi")?.apiName)
        assertNull(Genres.byName("Polka"))
        assertNull(Genres.byName(null))
        assertTrue(Genres.all.all { it.emoji.isNotBlank() })
        assertTrue(Genres.all.all { it.displayName.isNotBlank() })
        // The API value stays English while the user sees the Russian label.
        assertEquals("Электроника", Genres.byApiName("electronic")?.displayName)
        assertEquals("Поп", Genres.displayNameOf("Pop"))
        assertEquals("Хип-хоп/Рэп", Genres.byName("Хип-хоп/Рэп")?.displayName)
        assertEquals("Hip-Hop/Rap", Genres.byName("Хип-хоп/Рэп")?.apiName)
        assertNull(Genres.byApiName("Поп"))
    }

    @Test
    fun search_result_knows_when_it_is_empty() {
        assertTrue(SearchResult().isEmpty)
        assertFalse(SearchResult(tracks = listOf(Track("t1", "One", artist))).isEmpty)
        assertFalse(SearchResult(artists = listOf(artist)).isEmpty)
        assertFalse(SearchResult(playlists = listOf(Playlist(id = "p1", name = "Mix"))).isEmpty)
    }

    @Test
    fun app_settings_reports_signed_in_only_for_non_blank_token() {
        assertFalse(AppSettings().isSignedIn)
        assertFalse(AppSettings(apiToken = "   ").isSignedIn)
        assertTrue(AppSettings(apiToken = "token").isSignedIn)
        assertEquals(ThemeMode.SYSTEM, AppSettings().themeMode)
    }

    @Test
    fun enums_keep_expected_values() {
        assertEquals(listOf("SYSTEM", "LIGHT", "DARK"), ThemeMode.entries.map { it.name })
        assertEquals(listOf("OFF", "ALL", "ONE"), RepeatMode.entries.map { it.name })
        assertEquals(
            listOf("TRENDING", "GENRE", "ARTIST", "PLAYLIST"),
            CollectionSource.entries.map { it.name },
        )
    }
}
