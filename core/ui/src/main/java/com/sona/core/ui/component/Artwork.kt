package com.sona.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun Artwork(
    url: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF2C2244),
                        Color(0xFF17131F),
                        Color(0xFF241A33),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.28f),
            modifier = Modifier.size(30.dp),
        )
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(220)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}
