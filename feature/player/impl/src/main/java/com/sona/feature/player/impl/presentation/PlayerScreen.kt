package com.sona.feature.player.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sona.core.ui.component.Artwork
import com.sona.core.ui.component.CircleIconButton
import com.sona.core.ui.component.EmptyState
import com.sona.core.ui.format.formatDuration
import com.sona.core.ui.util.shareTrack
import com.sona.feature.player.impl.presentation.components.PlayerControls
import com.sona.feature.player.impl.presentation.components.QueueSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showQueue by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf<Float?>(null) }

    val track = state.track
    val duration = state.player.durationMs.coerceAtLeast(1L)
    val progress = dragProgress ?: state.player.progress

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        if (track == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                Row(Modifier.padding(8.dp)) {
                    CircleIconButton(
                        icon = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Close player",
                        onClick = onBack,
                    )
                }
                EmptyState(
                    title = "Nothing playing",
                    message = "Pick a track from Home, Search or your favorites.",
                    actionText = "Go back",
                    onAction = onBack,
                    modifier = Modifier.weight(1f),
                )
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircleIconButton(
                    icon = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Close player",
                    onClick = onBack,
                )
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Sona radio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.weight(1f))
                CircleIconButton(
                    icon = Icons.Rounded.QueueMusic,
                    contentDescription = "Open queue",
                    onClick = { showQueue = true },
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Artwork(
                    url = track.artworkUrl,
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .aspectRatio(1f)
                        .shadow(28.dp, RoundedCornerShape(30.dp)),
                    shape = RoundedCornerShape(30.dp),
                    contentDescription = track.title,
                )
            }

            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = track.artistName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            state.player.error?.let { message ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                )
            }

            Spacer(Modifier.height(16.dp))
            Slider(
                value = progress.coerceIn(0f, 1f),
                onValueChange = { dragProgress = it },
                onValueChangeFinished = {
                    dragProgress?.let { viewModel.onIntent(PlayerIntent.SeekTo((it * duration).toLong())) }
                    dragProgress = null
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatDuration(((progress * duration) / 1000).toInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatDuration((duration / 1000).toInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))
            PlayerControls(
                isPlaying = state.player.isPlaying,
                shuffle = state.player.shuffle,
                repeatMode = state.player.repeatMode,
                onToggleShuffle = { viewModel.onIntent(PlayerIntent.ToggleShuffle) },
                onPrevious = { viewModel.onIntent(PlayerIntent.Previous) },
                onTogglePlayPause = { viewModel.onIntent(PlayerIntent.TogglePlayPause) },
                onNext = { viewModel.onIntent(PlayerIntent.Next) },
                onCycleRepeat = { viewModel.onIntent(PlayerIntent.CycleRepeat) },
            )

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = { viewModel.onIntent(PlayerIntent.ToggleFavorite) }) {
                    Icon(
                        imageVector = if (state.isFavorite) Icons.Rounded.Favorite
                        else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (state.isFavorite) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    onClick = {
                        context.shareTrack(
                            title = track.title,
                            artist = track.artistName,
                            url = track.shareUrl,
                        )
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share track",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    text = if (state.player.isBuffering) "Buffering…" else "Streaming from Audius",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showQueue) {
        ModalBottomSheet(
            onDismissRequest = { showQueue = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            QueueSheet(
                queue = state.player.queue,
                currentIndex = state.player.currentIndex,
                isPlaying = state.player.isPlaying,
                onSelect = { index ->
                    viewModel.onIntent(PlayerIntent.SelectQueueItem(index))
                    showQueue = false
                },
            )
        }
    }
}
