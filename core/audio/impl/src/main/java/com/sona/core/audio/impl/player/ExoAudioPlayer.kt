package com.sona.core.audio.impl.player

import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sona.core.audio.AudioPlayer
import com.sona.core.audio.PlayerState
import com.sona.core.common.domain.usecase.AddRecentlyPlayed
import com.sona.core.common.model.RepeatMode
import com.sona.core.common.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExoAudioPlayer @Inject constructor(
    private val player: ExoPlayer,
    private val addRecentlyPlayed: AddRecentlyPlayed,
) : AudioPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var ticker: Job? = null
    private var lastRecordedTrackId: String? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncState()
                recordCurrentTrack()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) = syncState()

            override fun onPlaybackStateChanged(playbackState: Int) = syncState()

            override fun onPlayerError(error: PlaybackException) {
                _state.update {
                    it.copy(error = error.localizedMessage ?: "Playback failed", isBuffering = false)
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = syncState()

            override fun onRepeatModeChanged(repeatMode: Int) = syncState()
        })
    }

    override fun playQueue(tracks: List<Track>, startIndex: Int) {
        val playable = tracks.filter { it.isStreamable && it.streamUrl.isNotBlank() }
        if (playable.isEmpty()) {
            _state.update { it.copy(error = "This track can't be streamed") }
            return
        }
        val selectedId = tracks.getOrNull(startIndex)?.id
        val safeIndex = playable.indexOfFirst { it.id == selectedId }.takeIf { it >= 0 } ?: 0
        val track = playable[safeIndex]

        _state.update {
            it.copy(
                queue = playable,
                currentIndex = safeIndex,
                positionMs = 0L,
                durationMs = track.durationSeconds * 1000L,
                isBuffering = true,
                error = null,
                shuffle = player.shuffleModeEnabled,
                repeatMode = player.repeatMode.toRepeatMode(),
            )
        }

        player.setMediaItems(playable.map(::toMediaItem), safeIndex, 0L)
        player.prepare()
        player.playWhenReady = true
        startTicker()
        recordCurrentTrack(force = true)
    }

    override fun togglePlayPause() {
        if (player.mediaItemCount == 0) return
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        if (player.isPlaying) player.pause() else player.play()
    }

    override fun play() {
        if (player.mediaItemCount == 0) return
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        player.play()
    }

    override fun pause() = player.pause()

    override fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else if (player.repeatMode == Player.REPEAT_MODE_ALL && player.mediaItemCount > 0) {
            player.seekTo(0, 0L)
            player.play()
        }
    }

    override fun previous() {
        if (player.currentPosition > 3_000L) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(0L)
        }
    }

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0L))
        syncState()
    }

    override fun selectQueueItem(index: Int) {
        if (index !in 0 until player.mediaItemCount) return
        player.seekToDefaultPosition(index)
        player.play()
    }

    override fun addToQueue(track: Track) {
        if (!track.isStreamable || track.streamUrl.isBlank()) return
        if (_state.value.queue.isEmpty()) {
            playQueue(listOf(track))
            return
        }
        _state.update { it.copy(queue = it.queue + track) }
        player.addMediaItem(toMediaItem(track))
    }

    override fun setShuffle(enabled: Boolean) {
        player.shuffleModeEnabled = enabled
        _state.update { it.copy(shuffle = enabled) }
    }

    override fun cycleRepeatMode() {
        val next = when (player.repeatMode) {
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_ALL
        }
        player.repeatMode = next
        _state.update { it.copy(repeatMode = next.toRepeatMode()) }
    }

    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = scope.launch {
            while (isActive) {
                if (player.isPlaying || player.playbackState == Player.STATE_BUFFERING) {
                    syncPosition()
                }
                delay(500L)
            }
        }
    }

    private fun syncState() {
        _state.update { current ->
            current.copy(
                currentIndex = player.currentMediaItemIndex.coerceAtLeast(0),
                isPlaying = player.isPlaying,
                isBuffering = player.playbackState == Player.STATE_BUFFERING,
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = player.duration.takeIf { it > 0L && it != C.TIME_UNSET }
                    ?: current.durationMs,
                shuffle = player.shuffleModeEnabled,
                repeatMode = player.repeatMode.toRepeatMode(),
            )
        }
        if (player.isPlaying) startTicker()
    }

    private fun syncPosition() {
        _state.update { current ->
            current.copy(
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = player.duration.takeIf { it > 0L && it != C.TIME_UNSET }
                    ?: current.durationMs,
                isBuffering = player.playbackState == Player.STATE_BUFFERING,
            )
        }
    }

    private fun recordCurrentTrack(force: Boolean = false) {
        val track = _state.value.queue.getOrNull(player.currentMediaItemIndex.coerceAtLeast(0))
            ?: return
        if (!force && track.id == lastRecordedTrackId) return
        lastRecordedTrackId = track.id
        scope.launch { addRecentlyPlayed(track) }
    }

    private fun toMediaItem(track: Track): MediaItem = MediaItem.Builder()
        .setMediaId(track.id)
        .setUri(track.streamUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artistName)
                .apply { track.artworkUrl?.let { setArtworkUri(it.toUri()) } }
                .build(),
        )
        .build()

    private fun Int.toRepeatMode(): RepeatMode = when (this) {
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        else -> RepeatMode.OFF
    }
}
