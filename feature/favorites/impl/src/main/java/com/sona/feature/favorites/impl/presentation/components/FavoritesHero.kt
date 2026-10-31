package com.sona.feature.favorites.impl.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sona.core.ui.component.GradientCircleButton
import com.sona.core.ui.format.formatDuration

/** Gradient summary card at the top of the favorites screen. */
@Composable
fun FavoritesHero(
    count: Int,
    totalDuration: Int,
    onPlayAll: () -> Unit,
    enabled: Boolean,
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
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.size(10.dp))
                Text(
                    text = "YOUR LIBRARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Loved\nby you",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (count == 0) "No tracks yet" else "$count tracks  ·  ${formatDuration(totalDuration)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f),
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(18.dp))
            if (enabled) {
                GradientCircleButton(
                    icon = Icons.Rounded.PlayArrow,
                    contentDescription = "Play all favorites",
                    onClick = onPlayAll,
                    size = 54.dp,
                )
            }
        }
    }
}
