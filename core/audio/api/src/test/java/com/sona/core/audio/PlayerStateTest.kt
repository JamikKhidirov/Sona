package com.sona.core.audio

import com.sona.core.common.model.Artist
import com.sona.core.common.model.RepeatMode
import com.sona.core.common.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStateTest {

    private val artist = Artist(id = "u1", handle = "sona", name = "Sona")
    private val tracks = listOf(
        Track(id = "t1", title = "One", artist = artist),
        Track(id = "t2", title = "Two", artist = artist),
        Track(id = "t3", title = "Three", artist = artist),
    )

    @Test
    fun empty_state_has_no_track_and_no_navigation() {
        val state = PlayerState()

        assertNull(state.currentTrack)
        assertFalse(state.hasNext)
        assertFalse(state.hasPrevious)
        assertEquals(0f, state.progress)
        assertEquals(0, state.currentIndex)
        assertEquals(RepeatMode.OFF, state.repeatMode)
    }

    @Test
    fun current_track_is_taken_from_queue_index() {
        val state = PlayerState(queue = tracks, currentIndex = 1)

        assertEquals("t2", state.currentTrack?.id)
    }

    @Test
    fun out_of_bounds_index_returns_null_track() {
        val state = PlayerState(queue = tracks, currentIndex = 99)

        assertNull(state.currentTrack)
    }

    @Test
    fun has_next_is_true_until_last_track() {
        assertTrue(PlayerState(queue = tracks, currentIndex = 0).hasNext)
        assertTrue(PlayerState(queue = tracks, currentIndex = 1).hasNext)
        assertFalse(PlayerState(queue = tracks, currentIndex = 2).hasNext)
    }

    @Test
    fun repeat_all_keeps_next_available_on_last_track() {
        val state = PlayerState(
            queue = tracks,
            currentIndex = 2,
            repeatMode = RepeatMode.ALL,
        )

        assertTrue(state.hasNext)
    }

    @Test
    fun repeat_one_does_not_mark_last_track_as_having_next() {
        val state = PlayerState(
            queue = tracks,
            currentIndex = 2,
            repeatMode = RepeatMode.ONE,
        )

        assertFalse(state.hasNext)
    }

    @Test
    fun has_previous_is_true_after_index_zero_or_three_seconds() {
        assertFalse(PlayerState(queue = tracks, currentIndex = 0, positionMs = 0).hasPrevious)
        assertFalse(PlayerState(queue = tracks, currentIndex = 0, positionMs = 3_000).hasPrevious)
        assertTrue(PlayerState(queue = tracks, currentIndex = 0, positionMs = 3_001).hasPrevious)
        assertTrue(PlayerState(queue = tracks, currentIndex = 1, positionMs = 0).hasPrevious)
    }

    @Test
    fun progress_is_clamped_between_zero_and_one() {
        assertEquals(0.5f, PlayerState(positionMs = 500, durationMs = 1_000).progress)
        assertEquals(0f, PlayerState(positionMs = -50, durationMs = 1_000).progress)
        assertEquals(1f, PlayerState(positionMs = 2_000, durationMs = 1_000).progress)
        assertEquals(0f, PlayerState(positionMs = 500, durationMs = 0).progress)
    }

    @Test
    fun copy_keeps_queue_but_changes_flags() {
        val state = PlayerState(queue = tracks)
            .copy(isPlaying = true, isBuffering = true, shuffle = true, error = "boom")

        assertTrue(state.isPlaying)
        assertTrue(state.isBuffering)
        assertTrue(state.shuffle)
        assertEquals("boom", state.error)
        assertEquals(tracks, state.queue)
    }
}
