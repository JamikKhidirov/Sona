package com.sona.core.audio

import com.sona.core.common.model.RepeatMode
import com.sona.core.common.model.Track
import kotlinx.coroutines.flow.StateFlow

data class PlayerState(
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val error: String? = null,
) {
    val currentTrack: Track? get() = queue.getOrNull(currentIndex)
    val hasNext: Boolean get() = currentIndex < queue.lastIndex || repeatMode == RepeatMode.ALL
    val hasPrevious: Boolean get() = currentIndex > 0 || positionMs > 3_000L
    val progress: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

interface AudioPlayer {
    val state: StateFlow<PlayerState>

    fun playQueue(tracks: List<Track>, startIndex: Int = 0)
    fun togglePlayPause()
    fun play()
    fun pause()
    fun next()
    fun previous()
    fun seekTo(positionMs: Long)
    fun selectQueueItem(index: Int)
    fun addToQueue(track: Track)
    fun setShuffle(enabled: Boolean)
    fun cycleRepeatMode()
}
