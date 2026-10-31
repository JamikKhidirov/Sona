package com.sona.core.ui.format

import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return "0:00"
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes >= 60) {
        val hours = minutes / 60
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes % 60, secs)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, secs)
    }
}

fun formatCount(value: Long): String = when {
    value >= 1_000_000_000L -> trim(value / 1_000_000_000.0) + "B"
    value >= 1_000_000L -> trim(value / 1_000_000.0) + "M"
    value >= 1_000L -> trim(value / 1_000.0) + "K"
    else -> value.toString()
}

private fun trim(value: Double): String {
    val rounded = (value * 10).roundToLong() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString()
    else String.format(Locale.US, "%.1f", rounded)
}

fun greetingForNow(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        in 18..22 -> "Good evening"
        else -> "Late night sounds"
    }
}
