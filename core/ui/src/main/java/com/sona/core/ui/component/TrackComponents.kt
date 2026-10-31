package com.sona.core.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sona.core.common.model.Artist
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.Track
import com.sona.core.ui.format.formatCount
import com.sona.core.ui.format.formatDuration

@Composable
fun PlayingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val transition = rememberInfiniteTransition(label = "equalizer")
    val bars = listOf(0, 1, 2).map { index ->
        transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 420 + index * 110),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "bar$index",
        )
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        bars.forEach { bar ->
            Box(
                Modifier
                    .width(3.dp)
                    .height((4 + bar.value * 11).dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
fun TrackRow(
    track: Track,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    isFavorite: Boolean = false,
    showFavorite: Boolean = true,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onFavorite: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
                else Color.Transparent,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Artwork(
            url = track.artworkUrl,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(14.dp),
            contentDescription = track.title,
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive && isPlaying) {
                    PlayingIndicator(modifier = Modifier.padding(end = 7.dp))
                }
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = buildString {
                    append(track.artistName)
                    track.genre?.takeIf { it.isNotBlank() }?.let {
                        append("  ·  ")
                        append(it)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = formatDuration(track.durationSeconds),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (showFavorite && onFavorite != null) {
            IconButton(onClick = onFavorite, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                )
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isActive && isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isActive && isPlaying) "Pause" else "Play",
                tint = if (isActive) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
fun TrackTile(
    track: Track,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onPlay: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(156.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 4.dp),
    ) {
        Box {
            Artwork(
                url = track.artworkUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp),
                shape = RoundedCornerShape(20.dp),
                contentDescription = track.title,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(9.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isActive && isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play ${track.title}",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            if (isActive) {
                PlayingIndicator(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
        Spacer(Modifier.height(9.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.titleSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = track.artistName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun ArtistTile(
    artist: Artist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(94.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Artwork(
            url = artist.avatarUrl,
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            contentDescription = artist.displayName,
        )
        Spacer(Modifier.height(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = artist.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (artist.isVerified) {
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = "Verified",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .size(13.dp),
                )
            }
        }
        Text(
            text = "${formatCount(artist.followerCount)} followers",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
fun ArtistRow(
    artist: Artist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Artwork(
            url = artist.avatarUrl,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            contentDescription = artist.displayName,
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = artist.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (artist.isVerified) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(15.dp),
                    )
                }
            }
            Text(
                text = "@${artist.handle}  ·  ${formatCount(artist.followerCount)} followers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun PlaylistTile(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(164.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 4.dp),
    ) {
        Artwork(
            url = playlist.artworkUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp),
            shape = RoundedCornerShape(20.dp),
            contentDescription = playlist.name,
        )
        Spacer(Modifier.height(9.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${playlist.trackCount} tracks",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PlaylistRow(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Artwork(
            url = playlist.artworkUrl,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(14.dp),
            contentDescription = playlist.name,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    playlist.ownerName?.let { append(it).append("  ·  ") }
                    append(playlist.trackCount).append(" tracks")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun TrackMetaRow(
    track: Track,
    modifier: Modifier = Modifier,
    showArtist: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showArtist) {
            Text(
                text = track.artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "·",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = formatCount(track.playCount) + " plays",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
