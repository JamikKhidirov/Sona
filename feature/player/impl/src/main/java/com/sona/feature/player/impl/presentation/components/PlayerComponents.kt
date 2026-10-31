package com.sona.feature.player.impl.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sona.core.common.model.RepeatMode
import com.sona.core.common.model.Track
import com.sona.core.ui.component.TrackRow

/** Shuffle / previous / play / next / repeat transport controls. */
@Composable
fun PlayerControls(
    isPlaying: Boolean,
    shuffle: Boolean,
    repeatMode: RepeatMode,
    onToggleShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        IconButton(
            onClick = onToggleShuffle,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (shuffle) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    else Color.Transparent,
                ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffle) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(52.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(34.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                        ),
                    ),
                )
                .clickable(onClick = onTogglePlayPause),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp),
            )
        }
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(52.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(34.dp),
            )
        }
        IconButton(
            onClick = onCycleRepeat,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (repeatMode != RepeatMode.OFF) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    } else {
                        Color.Transparent
                    },
                ),
        ) {
            Icon(
                imageVector = if (repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne
                else Icons.Rounded.Repeat,
                contentDescription = "Repeat",
                tint = if (repeatMode != RepeatMode.OFF) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

/** Bottom sheet listing the current playback queue. */
@Composable
fun QueueSheet(
    queue: List<Track>,
    currentIndex: Int,
    isPlaying: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .navigationBarsPadding(),
    ) {
        Text(
            text = "Up next",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${queue.size} tracks in queue",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(queue, key = { _, track -> track.id }) { index, track ->
                TrackRow(
                    track = track,
                    isActive = index == currentIndex,
                    isPlaying = isPlaying,
                    isFavorite = false,
                    showFavorite = false,
                    onClick = { onSelect(index) },
                    onPlay = { onSelect(index) },
                )
            }
        }
    }
}
