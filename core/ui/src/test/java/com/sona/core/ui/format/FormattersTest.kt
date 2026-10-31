package com.sona.core.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun duration_formats_minutes_and_hours() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:59", formatDuration(59))
        assertEquals("1:00", formatDuration(60))
        assertEquals("3:25", formatDuration(205))
        assertEquals("1:00:00", formatDuration(3_600))
        assertEquals("1:01:01", formatDuration(3_661))
    }

    @Test
    fun count_uses_short_suffixes() {
        assertEquals("999", formatCount(999))
        assertEquals("1K", formatCount(1_000))
        assertEquals("1.5K", formatCount(1_500))
        assertEquals("2M", formatCount(2_000_000))
        assertEquals("1B", formatCount(1_000_000_000))
    }
}
