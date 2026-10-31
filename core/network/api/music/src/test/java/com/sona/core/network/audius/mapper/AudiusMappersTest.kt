package com.sona.core.network.audius.mapper

import com.sona.core.network.audius.dto.ArtworkDto
import com.sona.core.network.audius.dto.PlaylistDto
import com.sona.core.network.audius.dto.TrackDto
import com.sona.core.network.audius.dto.UserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AudiusMappersTest {

    @Test
    fun track_without_id_is_dropped() {
        assertNull(TrackDto(title = "No id").toDomainOrNull())
    }

    @Test
    fun track_maps_artist_artwork_stats_and_stream_url() {
        val dto = TrackDto(
            id = "D7KyD",
            title = "Night Drive",
            user = UserDto(id = "u1", handle = "sona", name = "Sona"),
            artwork = ArtworkDto(small = "small", medium = "medium", large = "large"),
            duration = 185,
            playCount = 1_200,
            favoriteCount = 42,
            isStreamable = null,
        )

        val track = dto.toDomainOrNull()!!

        assertEquals("D7KyD", track.id)
        assertEquals("Sona", track.artistName)
        assertEquals("large", track.artworkUrl)
        assertEquals(185, track.durationSeconds)
        assertEquals(1_200L, track.playCount)
        assertTrue(track.isStreamable)
        assertEquals(
            "https://api.audius.co/v1/tracks/D7KyD/stream?app_name=Sona",
            track.streamUrl,
        )
    }

    @Test
    fun blank_title_and_missing_artist_use_defaults() {
        val track = TrackDto(id = "t1", title = "  ").toDomainOrNull()!!

        assertEquals("Untitled", track.title)
        assertEquals("Audius", track.artistName)
    }

    @Test
    fun artist_name_falls_back_to_handle() {
        val artist = UserDto(id = "u1", handle = "lofi_dj", name = "").toDomain()

        assertEquals("lofi_dj", artist.displayName)
        assertEquals(0L, artist.followerCount)
    }

    @Test
    fun artwork_picks_best_available_size() {
        val artwork = ArtworkDto(small = "small", medium = "medium").toDomain()

        assertEquals("medium", artwork?.bestUrl())
    }

    @Test
    fun playlist_falls_back_to_playlist_id_and_counts_valid_tracks() {
        val dto = PlaylistDto(
            id = null,
            playlistId = "PL1",
            playlistName = "",
            tracks = listOf(
                TrackDto(id = "t1", title = "One"),
                TrackDto(title = "Broken"),
            ),
        )

        val playlist = dto.toDomain()

        assertEquals("PL1", playlist.id)
        assertEquals("Untitled playlist", playlist.name)
        assertEquals(2, playlist.trackCount)
        assertEquals(1, playlist.tracks.size)
        assertEquals("t1", playlist.tracks.first().id)
    }
}
