package com.sona.core.ui.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormattersTest {

    @Test
    fun duration_formats_minutes_and_hours() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:59", formatDuration(59))
        assertEquals("1:00", formatDuration(60))
        assertEquals("3:25", formatDuration(205))
        assertEquals("59:59", formatDuration(3_599))
        assertEquals("1:00:00", formatDuration(3_600))
        assertEquals("1:01:01", formatDuration(3_661))
        assertEquals("23:59:59", formatDuration(86_399))
    }

    @Test
    fun duration_never_returns_negative_values() {
        assertEquals("0:00", formatDuration(-1))
        assertEquals("0:00", formatDuration(-3_600))
    }

    @Test
    fun count_uses_short_suffixes() {
        assertEquals("0", formatCount(0))
        assertEquals("999", formatCount(999))
        assertEquals("1K", formatCount(1_000))
        assertEquals("1.5K", formatCount(1_500))
        assertEquals("1.5K", formatCount(1_499))
        assertEquals("2M", formatCount(2_000_000))
        assertEquals("1.1M", formatCount(1_050_000))
        assertEquals("1B", formatCount(1_000_000_000))
        assertEquals("2.5B", formatCount(2_500_000_000))
    }

    @Test
    fun count_rounds_to_one_decimal_and_drops_trailing_zero() {
        assertEquals("1K", formatCount(1_000))
        assertEquals("1.2K", formatCount(1_240))
        assertEquals("1.3K", formatCount(1_250))
    }

    @Test
    fun greeting_maps_every_hour_bucket() {
        assertEquals("Late night sounds", greetingForHour(0))
        assertEquals("Late night sounds", greetingForHour(4))
        assertEquals("Good morning", greetingForHour(5))
        assertEquals("Good morning", greetingForHour(11))
        assertEquals("Good afternoon", greetingForHour(12))
        assertEquals("Good afternoon", greetingForHour(17))
        assertEquals("Good evening", greetingForHour(18))
        assertEquals("Good evening", greetingForHour(22))
        assertEquals("Late night sounds", greetingForHour(23))
    }

    @Test
    fun greeting_for_now_is_one_of_the_known_buckets() {
        val greeting = greetingForNow()

        assertTrue(
            greeting in setOf(
                "Good morning",
                "Good afternoon",
                "Good evening",
                "Late night sounds",
            ),
        )
    }
}
