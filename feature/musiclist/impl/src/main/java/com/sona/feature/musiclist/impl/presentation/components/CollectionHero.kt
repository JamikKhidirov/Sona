package com.sona.feature.musiclist.impl.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sona.core.ui.component.Artwork
import com.sona.core.ui.component.GradientCircleButton
import com.sona.core.ui.format.formatDuration

/** Header card shared by every collection list (genre, artist, trending…). */
@Composable
fun CollectionHero(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    showPlayAll: Boolean,
    onPlayAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.deepen(),
                        MaterialTheme.colorScheme.secondary.deepen(),
                    ),
                ),
            ),
    ) {
        if (artworkUrl != null) {
            Artwork(
                url = artworkUrl,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(30.dp),
                contentDescription = title,
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.9f),
                            ),
                        ),
                    ),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = title.ifBlank { "Collection" },
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                maxLines = 2,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(14.dp))
            if (showPlayAll) {
                GradientCircleButton(
                    icon = Icons.Rounded.PlayArrow,
                    contentDescription = "Play all",
                    onClick = onPlayAll,
                    size = 54.dp,
                )
            }
        }
    }
}

private fun Color.deepen(): Color = copy(red = red * 0.62f, green = green * 0.62f, blue = blue * 0.62f)
