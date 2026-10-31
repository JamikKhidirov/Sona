package com.sona.feature.profile.impl.presentation.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sona.core.common.model.Track
import com.sona.core.ui.component.Artwork
import com.sona.core.ui.component.TrackTile
import com.sona.core.ui.format.formatCount
import com.sona.feature.profile.impl.presentation.ProfileUiState

/** Gradient identity card with avatar and counters. */
@Composable
fun ProfileHero(
    state: ProfileUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                    ),
                ),
            )
            .padding(22.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.user?.avatarUrl != null) {
                    Artwork(
                        url = state.user.avatarUrl,
                        modifier = Modifier.size(78.dp),
                        shape = CircleShape,
                        contentDescription = state.displayName,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp),
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (state.user?.isVerified == true) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = "Verified",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(start = 5.dp)
                                    .size(18.dp),
                            )
                        }
                    }
                    Text(
                        text = state.handle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = if (state.isSignedIn) Icons.Rounded.CloudDone
                            else Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = if (state.isSignedIn) "Connected to Audius" else "Guest listener",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Row {
                ProfileStat(
                    value = state.favorites.size.toString(),
                    label = "Liked",
                    modifier = Modifier.weight(1f),
                )
                ProfileStat(
                    value = state.recentlyPlayed.size.toString(),
                    label = "Recent",
                    modifier = Modifier.weight(1f),
                )
                ProfileStat(
                    value = if (state.isSignedIn) formatCount(state.followerCount) else "—",
                    label = "Followers",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun ProfileStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.72f),
        )
    }
}

/** Prompt shown to guest listeners so they can add an Audius token. */
@Composable
fun ConnectAudiusCard(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = "Make it yours",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Add an Audius API token in Settings to load your profile, " +
                    "followers and account details. Browsing works without one.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Open settings",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onOpenSettings)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

/** Horizontal shelf of track tiles used by the profile sections. */
@Composable
fun ProfileTracksRow(
    tracks: List<Track>,
    currentTrackId: String?,
    isPlaying: Boolean,
    onOpenTrack: (String) -> Unit,
    onPlay: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(tracks, key = { it.id }) { track ->
            TrackTile(
                track = track,
                isActive = currentTrackId == track.id,
                isPlaying = isPlaying,
                onClick = { onOpenTrack(track.id) },
                onPlay = { onPlay(track) },
            )
        }
    }
}

/** Rounded menu row with leading icon and trailing chevron. */
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
