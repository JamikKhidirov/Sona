package com.sona.feature.detailmusic.impl.presentation.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sona.core.common.model.Track
import com.sona.core.ui.component.Artwork
import com.sona.core.ui.component.OutlinePill

/** Artwork, title, artist and genre pills shown at the top of the track page. */
@Composable
fun TrackDetailHero(
    track: Track,
    onOpenArtist: (id: String, name: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                            Color.Transparent,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Artwork(
                url = track.artworkUrl,
                modifier = Modifier
                    .size(250.dp)
                    .shadow(24.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                contentDescription = track.title,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onOpenArtist(track.artist.id, track.artistName) }
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Artwork(
                url = track.artist.avatarUrl,
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                contentDescription = track.artistName,
            )
            Text(
                text = track.artistName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            if (track.artist.isVerified) {
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = "Verified artist",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        if (track.genre != null || track.mood != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                track.genre?.let { OutlinePill(it) }
                track.mood?.let { OutlinePill(it) }
            }
        }
    }
}
